package com.sean.ratel.android.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.android.data.android.permission.PermissionManager
import com.sean.ratel.android.data.android.permission.PermissionProvider
import com.sean.ratel.android.di.qualifier.RemoteIntervalTime
import com.sean.ratel.android.ui.ad.AppOpenAdManager
import com.sean.ratel.android.ui.ad.GoogleMobileAdsConsentManager
import com.sean.ratel.player.core.domain.api.UserAgentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shorform-play")

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun provideUserAgent(): UserAgentProvider =
        object : UserAgentProvider {
            override val userAgent: String
                get() = ""
        }

    @Provides
    @Singleton
    fun provideDataStorePreferences(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun providePermissionManagerr(): PermissionProvider = PermissionManager()

    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun provideGoogleMobileAdsConsentManager(
        @ApplicationContext context: Context,
    ): GoogleMobileAdsConsentManager = GoogleMobileAdsConsentManager(context)

    @Provides
    @OptIn(UnstableApi::class)
    fun providerAppOpenAdManager(googleMobileAdsConsentManager: GoogleMobileAdsConsentManager): AppOpenAdManager =
        AppOpenAdManager(googleMobileAdsConsentManager)

    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun providerGoogleAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun providerRemoteConfig(
        @RemoteIntervalTime interval: Long,
    ): FirebaseRemoteConfig {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings =
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = interval
            }
        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    @Provides
    @Singleton
    fun provideWidthIFramePlayerOption(): IFramePlayerOptions =
        IFramePlayerOptions
            .Builder()
            .controls(0)
            .build()

    @Provides
    fun provideYouTubePlayerTracker(): YouTubePlayerTracker = YouTubePlayerTracker()
}
