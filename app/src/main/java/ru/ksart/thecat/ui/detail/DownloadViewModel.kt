package ru.ksart.thecat.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.annotation.IntegerRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.ksart.thecat.R
import ru.ksart.thecat.model.repositories.DownloadRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val repository: DownloadRepository
) : ViewModel() {
    private val _isToast = Channel<@IntegerRes Int>()
    val isToast = _isToast.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _saveTo = Channel<String>()
    val saveTo = _saveTo.receiveAsFlow()
    private var saveToUrl = ""

    private val _shareIntent = Channel<Intent?>()
    val shareIntent = _shareIntent.receiveAsFlow()

    private val _isPermissionsGranted = MutableStateFlow(true)
    val isPermissionsGranted = _isPermissionsGranted.asStateFlow()

    fun saveMedia(url: String) {
        viewModelScope.launch {
            if (url.isBlank()) {
                _isToast.send(R.string.no_link_to_media_file)
                return@launch
            }
            _isLoading.value = true
            try {
                repository.saveMedia(url)
                _isToast.send(R.string.file_uploaded_to_gallery)
            } catch (e: Exception) {
                Timber.e(e, "saveMedia error")
                _isToast.send(R.string.file_upload_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAsMedia(url: String) {
        viewModelScope.launch {
            val name = File(url).name
            saveToUrl = url
            _saveTo.send(name)
        }
    }

    fun saveAsMediaTo(uri: Uri?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                uri ?: return@launch
                if (uri.toString().isBlank() || saveToUrl.isBlank()) return@launch
                repository.saveAsMedia(uri, saveToUrl)
                _isToast.send(R.string.file_uploaded)
            } catch (e: Exception) {
                Timber.e(e, "saveAsMedia error")
                _isToast.send(R.string.file_upload_error)
            } finally {
                _isLoading.value = false
                saveToUrl = ""
            }
        }
    }

    fun share(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _shareIntent.send(repository.getIntentToShareFile(url))
            } catch (e: Exception) {
                Timber.e(e, "share error")
                _isToast.send(R.string.share_intent_error)
            }
        }
    }

    fun onPermissions(isGranted: Boolean) {
        Timber.d("isGranted=$isGranted")
        _isPermissionsGranted.value = isGranted
    }
}
