@file:Suppress("DEPRECATION")

package com.sean.ratel.android.di

import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.api.AuthApi
import com.sean.ratel.android.data.api.Networking
import com.sean.ratel.android.data.api.interceptor.AuthInterceptor
import com.sean.ratel.android.data.api.youtube.FireBaseApi
import com.sean.ratel.android.data.api.youtube.YouTubeSearchApi
import com.sean.ratel.android.di.qualifier.AuthOKttpClient
import com.sean.ratel.android.di.qualifier.ContentType
import com.sean.ratel.android.di.qualifier.FireBaseBaseUrl
import com.sean.ratel.android.di.qualifier.GoogleCloudProjectNumber
import com.sean.ratel.android.di.qualifier.OKttpClient
import com.sean.ratel.android.di.qualifier.RemoteIntervalTime
import com.sean.ratel.android.di.qualifier.ShortFormPlayBaseUrl
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
    @OKttpClient
    fun provideOkHttpClient(): OkHttpClient = Networking.createOkHttpClient()

    @Provides
    @Singleton
    @AuthOKttpClient
    fun provideAuthOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient = Networking.createAuthOkHttpClient(authInterceptor)

    @Provides
    @Singleton
    fun provideFireBaseApi(
        @FireBaseBaseUrl baseUrl: String,
        @OKttpClient okHttpClient: OkHttpClient,
    ): FireBaseApi =
        Networking.createService(
            baseUrl,
            okHttpClient,
            FireBaseApi::class.java,
        )

    @Provides
    @Singleton
    fun provideShortFormPlayApi(
        @ShortFormPlayBaseUrl baseUrl: String,
        @AuthOKttpClient okHttpClient: OkHttpClient,
    ): YouTubeSearchApi =
        Networking.createService(
            baseUrl,
            okHttpClient,
            YouTubeSearchApi::class.java,
        )

    @Provides
    @Singleton
    fun provideAuthApi(
        @ShortFormPlayBaseUrl baseUrl: String,
        @AuthOKttpClient okHttpClient: OkHttpClient,
    ): AuthApi =
        Networking.createService(
            baseUrl,
            okHttpClient,
            AuthApi::class.java,
        )

    @Provides
    @Singleton
    @FireBaseBaseUrl
    fun provideBaseFireBaseUrl(): String = BuildConfig.FIREBASE_BASE_URL

    @Provides
    @Singleton
    @ShortFormPlayBaseUrl
    fun provideYouTubeSearchBaseUrl(): String = BuildConfig.SHORTFORM_PLAY_BASE_URL

    @Provides
    @Singleton
    @GoogleCloudProjectNumber
    fun provideGoogleCloudNumber(): String = BuildConfig.GOOGLE_CLOUD_PROJECT_NUMBER

    @Provides
    @Singleton
    @ContentType
    fun provideContentType(): String = "application/json"

    @Provides
    @Singleton
    @RemoteIntervalTime
    fun provideRemoteIntervalTime(): Long = 3600
}
