package ru.ksart.thecat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.ksart.thecat.model.networking.ApiKeyInterceptor
import ru.ksart.thecat.model.networking.CatApi
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @LoggingInterceptorForOkHttpClient
    @Provides
    fun provideLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @ApiKeyInterceptorForOkHttpClient
    @Provides
    fun provideApiKeyInterceptor(): Interceptor = ApiKeyInterceptor()

    @Provides
    fun provideOkHttpClient(
        @LoggingInterceptorForOkHttpClient loggingInterceptor: Interceptor,
        @ApiKeyInterceptorForOkHttpClient apiKeyInterceptor: ApiKeyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(apiKeyInterceptor)
            .addNetworkInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/images/search")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideCatApi(retrofit: Retrofit): CatApi = retrofit.create()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptorForOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiKeyInterceptorForOkHttpClient
