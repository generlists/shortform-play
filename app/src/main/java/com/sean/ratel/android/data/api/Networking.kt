package com.sean.ratel.android.data.api

import com.google.gson.GsonBuilder
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.api.interceptor.AuthInterceptor
import com.sean.ratel.android.data.api.interceptor.FirebaseTokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Networking {
    private const val NETWORK_CALL_TIMEOUT = 60
    private val gson = GsonBuilder().setLenient().create()

    fun <T> createService(
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

    fun createOkHttpClient() =
        OkHttpClient
            .Builder()
            .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()

    fun createAuthOkHttpClient(
        authInterceptor: AuthInterceptor,
        firebaseTokenInterceptor: FirebaseTokenInterceptor,
    ) = OkHttpClient
        .Builder()
        // 바깥쪽: 로그인 API 에만 X-Firebase-Token 을 붙인다. 안쪽 AuthInterceptor 는 Integrity 토큰만 다룬다
        .addInterceptor(firebaseTokenInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(getHttpLoggingInterceptor())
        .readTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(NETWORK_CALL_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .build()

    private fun getHttpLoggingInterceptor() =
        HttpLoggingInterceptor()
            .apply {
                // 로그인 토큰은 디버그 로그에도 남기지 않는다
                redactHeader(FirebaseTokenInterceptor.HEADER)
                level =
                    if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
            }
}
