package ru.ksart.thecat.model.repositories

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ksart.thecat.model.networking.DownloadApi
import ru.ksart.thecat.utils.DebugHelper
import ru.ksart.thecat.utils.isAndroidQ
import java.io.File
import javax.inject.Inject

class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadApi: DownloadApi,
) : DownloadRepository {

    // сохраняем медиа файл с именем и загружаем его по указанной ссылке
    override suspend fun saveMedia(url: String) {
        require(url.isNotBlank()) { "Error url is blank" }
        withContext(Dispatchers.IO) {
            // проверить тип файла
            checkMimeTypeIsImage(url)
            // получим имя из url
            val name = File(url).name
            // создаем файл на устройстве для записи
            val uri = saveMediaDetails(name)
            try {
                // загружаем файл по ссылке в созданный файл на устройстве
                downloadMedia(url, uri)
                // после загрузки помечаем файл как видимый
                makeMediaVisible(uri)
            } catch (e: Exception) {
                // если ошибка удалить, созданный на устройстве файл
                deleteMediaUri(uri)
                throw e
            }
        }
    }

    override suspend fun saveAsMedia(uri: Uri, url: String) {
        withContext(Dispatchers.IO) {
            // проверить тип файла
            checkMimeTypeIsImage(url)
            try {
                // загружаем файл по ссылке в созданный файл на устройстве
                downloadMedia(url, uri)
            } catch (e: Exception) {
                // если ошибка удалить, созданный на устройстве файл
                deleteMediaUri(uri)
                throw e
            }
        }
    }

    override suspend fun getIntentToShareFile(url: String): Intent = withContext(Dispatchers.IO) {
        DebugHelper.log("PotatoRepositoryImpl|getIntentToShareFile")
        require(url.isNotBlank()) { "Error url is blank" }
        // запишем в кешь
//        val uri = getUriBySaveToCacheDir(url)

        // проверить тип файла
        checkMimeTypeIsImage(url)
        // получим имя из url
        val name = File(url).name
        // создаем файл на устройстве для записи
        val uri = saveMediaDetails(name)
        try {
            // загружаем файл по ссылке в созданный файл на устройстве
            downloadMedia(url, uri)
            // после загрузки помечаем файл как видимый
            makeMediaVisible(uri)

            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                type = context.contentResolver.getType(uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            DebugHelper.log("PotatoRepositoryImpl|getIntentToShareFile Intent")
            Intent.createChooser(intent, null)
        } catch (e: Exception) {
            // если ошибка удалить, созданный на устройстве файл
            deleteMediaUri(uri)
            throw e
        }
    }

    private suspend fun getUriBySaveToCacheDir(url: String): Uri {
        DebugHelper.log("PotatoRepositoryImpl|getUriBySaveToCacheDir")
        require(url.isNotBlank()) { "Url is blank" }
        var file: File? = null
        return try {
            val folder =
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    context.externalCacheDir
                } else {
                    context.cacheDir
                }
            val name = File(url).name
            file = File(folder, name)
            DebugHelper.log("PotatoRepositoryImpl|getUriBySaveToCacheDir file=$file")
            downloadFile(url, file)
            file.toUri()
        } catch (e: Exception) {
            DebugHelper.log("PotatoRepositoryImpl|getUriBySaveToCacheDir error: ${e.localizedMessage}")
            try {
                file?.takeIf { it.exists() }?.delete()
            } catch (notUse: Exception) {
            }
            throw e
        }
    }

    // проверка типа по расширению файла
    private suspend fun checkMimeTypeIsImage(url: String) {
        val type = MimeTypeMap.getFileExtensionFromUrl(url)?.let { fileExtension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)?.takeIf {
                DebugHelper.log("DownloadRepositoryImpl|checkMimeTypeIsImage mediaType=$it")
                it.startsWith("image/")
            }
        }
        requireNotNull(type) { "Download file type is not image" }
    }

    // создаем файл у нас на устройстве и получаем ссылку на него,
    // это не значит что его сразу можно использовать, он еще не загрузился
    private fun saveMediaDetails(name: String): Uri {
        // выберем куда будем сохранять
        val volume = if (isAndroidQ) {
            // VOLUME_EXTERNAL_PRIMARY = "external_primary";
            // для андроид 10 это первая внешняя память
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        } else {
            // VOLUME_EXTERNAL = "external";
            MediaStore.VOLUME_EXTERNAL
        }
        //
        val mediaCollectionUri = MediaStore.Images.Media.getContentUri(volume)
        //
        val mediaDetails = ContentValues().apply {
            // имя файла
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            // тип файла
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            // видимость файла начиная с API 29-Q
            if (isAndroidQ) {
                // файл не отображается, пока мы не изменим это
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        return context.contentResolver.insert(mediaCollectionUri, mediaDetails)!!
    }

    // загрузим файл по ссылке в файл который мы создали на устройстве
    private suspend fun downloadMedia(url: String, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            downloadApi
                .getFile(url)
                .byteStream()
                .use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
        }
    }

    private suspend fun downloadFile(url: String, file: File) {
        DebugHelper.log("PotatoRepositoryImpl|downloadFile file=$url")
        file.outputStream().use { fileOutputStream ->
            downloadApi.getFile(url)
                .byteStream()
                .use { inputStream ->
                    inputStream.copyTo(fileOutputStream)
                }
        }
    }

    // установим видимость нашего файла после загрузки
    private fun makeMediaVisible(mediaUri: Uri) {
        if (isAndroidQ.not()) return
        // только для API 29, выставим что наш файл видим IS_PENDING - false(0)
        val mediaDetails = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        // обновим информацию
        context.contentResolver.update(mediaUri, mediaDetails, null, null)
    }

    // удалить файл с устройства по uri
    private suspend fun deleteMediaUri(uri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.delete(uri, null, null)
        }
    }
}
