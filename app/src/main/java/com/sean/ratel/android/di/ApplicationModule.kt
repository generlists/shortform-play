package com.sean.ratel.android.di

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.size.Precision
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.android.permission.PermissionManager
import com.sean.ratel.android.data.android.permission.PermissionProvider
import com.sean.ratel.android.data.common.AppAdsConfig
import com.sean.ratel.android.data.common.AppPushConfig
import com.sean.ratel.android.data.common.AppReviewConfig
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.di.qualifier.AdOpenUnitId
import com.sean.ratel.android.di.qualifier.AdaptiveBannerUnitId
import com.sean.ratel.android.di.qualifier.AdmobUnitId
import com.sean.ratel.android.di.qualifier.AllowReviewOnResume
import com.sean.ratel.android.di.qualifier.ApiUrl
import com.sean.ratel.android.di.qualifier.AppId
import com.sean.ratel.android.di.qualifier.AppVersion
import com.sean.ratel.android.di.qualifier.BannerUnitId
import com.sean.ratel.android.di.qualifier.DebugMode
import com.sean.ratel.android.di.qualifier.DeviceModel
import com.sean.ratel.android.di.qualifier.EmotionScoreThreshold
import com.sean.ratel.android.di.qualifier.InAppSuccessRateThreshold
import com.sean.ratel.android.di.qualifier.InterstitialUnitId
import com.sean.ratel.android.di.qualifier.MaxCustomPromptsPerYear
import com.sean.ratel.android.di.qualifier.MinAttemptsBeforeCustom
import com.sean.ratel.android.di.qualifier.MinDaysBetweenCustomPrompts
import com.sean.ratel.android.di.qualifier.MinDaysBetweenInAppAttempts
import com.sean.ratel.android.di.qualifier.MinDaysSinceInstall
import com.sean.ratel.android.di.qualifier.MinLaunchCount
import com.sean.ratel.android.di.qualifier.NativeAdUnitId
import com.sean.ratel.android.di.qualifier.Region
import com.sean.ratel.android.di.qualifier.RemoteIntervalTime
import com.sean.ratel.android.di.qualifier.TestHashId
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.player.core.domain.api.UserAgentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import so.smartlab.common.ad.admob.data.repository.AdsConfigProvider
import so.smartlab.common.push.fcm.data.repository.PushConfigProvider
import so.smartlab.common.review.ReviewConfig
import so.smartlab.common.review.data.FirebaseReviewAnalytics
import so.smartlab.common.review.data.ReviewAnalytics
import java.util.Locale
import java.util.Optional
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
    fun providePermissionManager(
        @ApplicationContext context: Context,
    ): PermissionProvider = PermissionManager(context)

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
            .fullscreen(1) // enable full screen button
            .build()

    @Provides
    fun provideYouTubePlayerTracker(): YouTubePlayerTracker = YouTubePlayerTracker()

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .memoryCache {
                MemoryCache
                    .Builder(context)
                    .maxSizePercent(0.25) // 앱 가용 메모리의 25%까지 캐시 허용
                    .build()
            }.diskCache {
                DiskCache
                    .Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            }.components {
                add(VideoFrameDecoder.Factory())
            }.respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .precision(Precision.INEXACT)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .crossfade(500)
            .allowHardware(true)
            .build()

    @Provides
    @Singleton
    fun provideADSdkConfigProvider(
        @ApplicationContext context: Context,
        @AdmobUnitId admobId: String,
        @BannerUnitId banner: String,
        @AdaptiveBannerUnitId adaptiveUnitId: String,
        @NativeAdUnitId nativeUnitId: String,
        @AdOpenUnitId openId: String,
        @InterstitialUnitId interstitialId: String,
    ): AdsConfigProvider =
        AppAdsConfig(
            admobId,
            "12345",
            banner,
            adaptiveUnitId,
            nativeUnitId,
            openId,
            interstitialId,
        )

    // 옵셔널 추가로 필요
    @Provides
    @Singleton
    fun provideOptionalAdsConfigProvider(provider: AdsConfigProvider): Optional<AdsConfigProvider> = Optional.of(provider)

    @Provides
    @Singleton
    @AdmobUnitId
    fun provideAdMobId(): String = BuildConfig.admobAppId

    @Provides
    @Singleton
    @BannerUnitId
    fun provideBannerId(): String = BuildConfig.BANNER_UNIT_ID

    @Provides
    @Singleton
    @AdaptiveBannerUnitId
    fun provideAdaptiveBannerId(): String = BuildConfig.ADAPTIVE_BANNER_UNIT_ID

    @Provides
    @Singleton
    @AdOpenUnitId
    fun provideAdOpenUnitId(): String = BuildConfig.Ad_OPEN_UNIT_ID

    @Provides
    @Singleton
    @NativeAdUnitId
    fun provideNativeBannerId(): String = BuildConfig.NATIVE_AD_UNIT_ID

    @Provides
    @Singleton
    @InterstitialUnitId
    fun provideInterstitialAdId(): String = BuildConfig.INTERSTITIALAd_UNIT_ID

    @Provides
    @Singleton
    @TestHashId
    fun provideTestHashId(): String = STRINGS.TEST_DEVICE_HASHED_ID

    @Provides
    @Singleton
    fun providePushSDKConfigProvider(
        @AppId appId: String,
        @ApiUrl apiUrl: String,
        @Region region: String,
        @DeviceModel deviceModel: String,
        @AppVersion appVersion: String,
    ): PushConfigProvider =
        AppPushConfig(
            id = appId,
            url = apiUrl,
            location = region,
            model = deviceModel,
            version = appVersion,
        )

    @Provides
    @Singleton
    @AppId
    fun provideAppId(): String = STRINGS.APP_NAME

    @Provides
    @Singleton
    @DeviceModel
    fun provideDeviceModel(): String = Build.MODEL

    @Provides
    @Singleton
    @Region
    fun provideRegion(): String = Locale.getDefault().country

    @Provides
    @Singleton
    @AppVersion
    fun provideAppVersion(
        @ApplicationContext context: Context,
    ): String = PhoneUtil.getAppVersionName(context)

    @Provides
    @Singleton
    @ApiUrl
    fun provideAppUrl(): String = BuildConfig.SHORTFORM_PLAY_BASE_URL

    @Provides
    @Singleton
    fun provideReviewConfigProvider(
        @MinLaunchCount minLaunchCount: Int,
        @MinDaysSinceInstall minDaysSinceInstall: Int,
        @MinDaysBetweenInAppAttempts minDaysBetweenInAppAttempts: Int,
        @MinDaysBetweenCustomPrompts minDaysBetweenCustomPrompts: Int,
        @MaxCustomPromptsPerYear maxCustomPromptsPerYear: Int,
        @InAppSuccessRateThreshold inAppSuccessRateThrehold: Float,
        @MinAttemptsBeforeCustom minAttemptsBeforeCustom: Int,
        @EmotionScoreThreshold emitionScroeThreshold: Int,
        @AllowReviewOnResume allowReviewOnResume: Boolean,
        @DebugMode debugMode: Boolean,
    ): ReviewConfig =
        AppReviewConfig(
            minLaunchCount,
            minDaysSinceInstall,
            minDaysBetweenInAppAttempts,
            minDaysBetweenCustomPrompts,
            maxCustomPromptsPerYear,
            inAppSuccessRateThrehold,
            minAttemptsBeforeCustom,
            emitionScroeThreshold,
            allowReviewOnResume,
            debugMode,
        )

    @Provides
    @Singleton
    fun provideAnalyticsProvider(): ReviewAnalytics = FirebaseReviewAnalytics(Firebase.analytics)

    /**
     * In-App Review 시도 최소 간격 (일)
     */
    @Provides
    @Singleton
    @MinLaunchCount
    fun provideMinLaunchCount(): Int = 5

    /**
     * 커스텀 다이얼로그 최소 간격 (일)
     */
    @Provides
    @Singleton
    @MinDaysSinceInstall
    fun provideMinDaysSinceInstall(): Int = 3

    /**
     * 커스텀 다이얼로그 고려 전 최소 시도 횟수
     */
    @Provides
    @Singleton
    @MinDaysBetweenInAppAttempts
    fun provideMinDaysBetweenInAppAttempts(): Int = 7

    @Provides
    @Singleton
    @MinDaysBetweenCustomPrompts
    fun provideMinDaysBetweenCustomPrompts(): Int = 60

    /**
     * 연간 최대 커스텀 프롬프트 횟수
     */
    @Provides
    @Singleton
    @MaxCustomPromptsPerYear
    fun provideMaxCustomPromptsPerYear(): Int = 2

    /**
     * In-App Review 성공률 임계값 (이하면 커스텀 다이얼로그 고려)
     */
    @Provides
    @Singleton
    @InAppSuccessRateThreshold
    fun provideInAppSuccessRateThreshold(): Float = 0.2f

    @Provides
    @Singleton
    @MinAttemptsBeforeCustom
    fun provideMinAttemptsBeforeCustom(): Int = 5

    /**
     * 감정 점수 기반 트리거 임계값
     */
    @Provides
    @Singleton
    @EmotionScoreThreshold
    fun provideEmotionScoreThreshold(): Int = 100

    /**
     * 앱이 포그라운드로 돌아올 때 리뷰 요청 허용 여부
     */
    @Provides
    @Singleton
    @AllowReviewOnResume
    fun provideAllowReviewOnResume(): Boolean = true

    /**
     * 디버그 모드 (로그 출력)
     */
    @Provides
    @Singleton
    @DebugMode
    fun provideDebugMode(): Boolean = BuildConfig.DEBUG
}
