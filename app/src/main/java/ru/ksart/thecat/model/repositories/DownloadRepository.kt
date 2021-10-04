package ru.ksart.thecat.model.repositories

import android.content.Intent
import android.net.Uri

interface DownloadRepository {
    suspend fun saveMedia(url: String)

    suspend fun saveAsMedia(uri: Uri, url: String)

    suspend fun getIntentToShareFile(url: String): Intent
}
