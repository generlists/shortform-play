@file:Suppress("DEPRECATION")

package com.sean.ratel.android.di

import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.api.Networking
import com.sean.ratel.android.data.api.youtube.FireBaseApi
import com.sean.ratel.android.di.qualifier.ContentType
import com.sean.ratel.android.di.qualifier.FireBaseBaseUrl
import com.sean.ratel.android.di.qualifier.FirebaseOKttpClient
import com.sean.ratel.android.di.qualifier.RemoteIntervalTime
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @FirebaseOKttpClient
    fun provideOkHttpClientFireBase(): OkHttpClient = Networking.createOkHttpClientFireBase()

    @Provides
    @Singleton
    fun provideFireBaseApi(
        @FireBaseBaseUrl baseUrl: String,
        @FirebaseOKttpClient okHttpClient: OkHttpClient,
    ): FireBaseApi =
        Networking.createFirBaseService(
            baseUrl,
            okHttpClient,
            FireBaseApi::class.java,
        )

    @Provides
    @Singleton
    @FireBaseBaseUrl
    fun provideBaseFireBaseUrl(): String = BuildConfig.FIREBASE_BASE_URL

    @Provides
    @Singleton
    @ContentType
    fun provideContentType(): String = "application/json"

    @Provides
    @Singleton
    @RemoteIntervalTime
    fun provideRemoteIntervalTime(): Long = 3600
}
