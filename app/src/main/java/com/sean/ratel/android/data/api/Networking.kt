package com.sean.ratel.android.data.api

import com.google.gson.GsonBuilder
import com.sean.ratel.android.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Networking {
    private const val NETWORK_CALL_TIMEOUT = 60
    private val gson = GsonBuilder().setLenient().create()

    fun <T> createFirBaseService(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        service: Class<T>,
    ): T =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(service)

    fun createOkHttpClientFireBase() =
        OkHttpClient
            .Builder()
            .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()

    private fun getHttpLoggingInterceptor() =
        HttpLoggingInterceptor()
            .apply {
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }
}
