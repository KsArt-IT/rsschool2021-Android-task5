package ru.ksart.thecat.model.networking

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // модифицируем Url
        val modifiedUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .build()

        // модифицируем запрос
        val modifiedRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        return chain.proceed(modifiedRequest)
    }

    private companion object {
        const val API_KEY = "73dd02b4-7330-4822-8cc6-0e3fb9cf860d"
    }
}
// https://api.thecatapi.com/v1/images?api_key=73dd02b4-7330-4822-8cc6-0e3fb9cf860d&limit=100&page=1&order=Rand
