package ru.ksart.thecat.model.networking

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadApi {
    @GET
    suspend fun getFile(
        @Url url: String
    ): ResponseBody
}
