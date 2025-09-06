package com.sean.ratel.android.ui.ad

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.UIUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * 광고과련 Event ViewModel
 */
@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class AdViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        val youTubeRepository: YouTubeRepository,
        private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager,
        private val appOpenAdManager: AppOpenAdManager,
    ) : ViewModel() {
        private val _adMobLoadStart = MutableStateFlow(false)
        val adLoadStart: StateFlow<Boolean> = _adMobLoadStart

        private val _adMobInitialComplete = MutableStateFlow(false)
        val adMobInitialComplete: StateFlow<Boolean> = _adMobInitialComplete

        private val _bottomBarHeight = mutableStateOf(56)
        val bottomBarHeight = _bottomBarHeight

        private val _adBannerLoadingCompleteAndGetAdSize = MutableStateFlow(Pair(false, 0))
        val adBannerLoadingCompleteAndGetAdSize = _adBannerLoadingCompleteAndGetAdSize

        private val _interstitialAd = MutableStateFlow<InterstitialAd?>(null)
        val interstitialAd: StateFlow<InterstitialAd?> = _interstitialAd

        private val _interstitialAdComplete = MutableStateFlow<Boolean?>(null)
        val interstitialAdComplete: StateFlow<Boolean?> = _interstitialAdComplete

        private val _isPrivacyOptionsRequired = MutableStateFlow(false)
        val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired

        private val isMobileAdsInitializeCalled = AtomicBoolean(false)
        private val gatherConsentFinished = AtomicBoolean(false)

        private val _adNative = MutableStateFlow<NativeAd?>(null)
        val adNative: StateFlow<NativeAd?> = _adNative

        private val _adNativeFail = MutableStateFlow<LoadAdError?>(null)
        val adNativeFail: StateFlow<LoadAdError?> = _adNativeFail

        private val _adBannerFail = MutableStateFlow<LoadAdError?>(null)
        val adBannerFail: StateFlow<LoadAdError?> = _adBannerFail

        private val _adInLineBannerFail = MutableStateFlow<LoadAdError?>(null)
        val adInLineBannerFail: StateFlow<LoadAdError?> = _adInLineBannerFail

        private val _interstitialAdFail = MutableStateFlow<LoadAdError?>(null)
        val interstitialAdFail: StateFlow<LoadAdError?> = _interstitialAdFail

        private val _viewAd = MutableStateFlow(false)
        val viewAd: StateFlow<Boolean> = _viewAd

        private val _forceClearCache = MutableStateFlow(false)
        val forceClearCache: StateFlow<Boolean> = _forceClearCache

        fun setForceClearCache(forceClearCache: Boolean) {
            _forceClearCache.value = forceClearCache
        }

        fun setViewAd(viewAd: Boolean) {
            _viewAd.value = viewAd
        }

        fun setInterstitialAd(interstitialAd: InterstitialAd?) {
            _interstitialAd.value = interstitialAd
        }

        fun setInterstitialAdComplete(adComplete: Boolean?) {
            _interstitialAdComplete.value = adComplete
        }

        fun setBottomBarHeight(height: Int) {
            _bottomBarHeight.value = height
        }

        fun setAdLoadStart(isLoad: Boolean) {
            _adMobLoadStart.value = isLoad
        }

        fun setinterstitialAdFailAdLoadStart(fail: LoadAdError) {
            _interstitialAdFail.value = fail
        }

        suspend fun showInterstitialAds(context: Context) {
            interstitialAd.collect { ad ->
                ad?.let {
                    showInterstitialAd(context = context, this, ad)
                }
            }
        }

        fun initAdMob(context: Context) {
            googleMobileAdsConsentManager.gatherConsent(context as Activity) { consentError ->
                if (consentError != null) {
                    // Consent not obtained in current session.
                    RLog.w(TAG, String.format("%s: %s", consentError.errorCode, consentError.message))
                }
                gatherConsentFinished.set(true)

                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk(context)
                }

                if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                    // Regenerate the options menu to include a privacy setting.
                    _isPrivacyOptionsRequired.value = true
                }
            }

            // This sample attempts to load ads using consent obtained in the previous session.
            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk(context)
            }
        }

        private fun initializeMobileAdsSdk(context: Context) {
            if (isMobileAdsInitializeCalled.getAndSet(true)) {
                return
            }

            // Set your test devices.
            if (BuildConfig.DEBUG) {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration
                        .Builder()
                        .setTestDeviceIds(listOf(STRINGS.TEST_DEVICE_HASHED_ID))
                        .build(),
                )
            }

            viewModelScope.launch {
                // Initialize the Google Mobile Ads SDK on a background thread.
                CoroutineScope(Dispatchers.IO).launch {
                    MobileAds.initialize(
                        context,
                        object : OnInitializationCompleteListener {
                            override fun onInitializationComplete(p0: InitializationStatus) {
                                for ((adapterClassName, adapterStatus) in p0.adapterStatusMap) {
                                    when (adapterStatus.initializationState) {
                                        AdapterStatus.State.READY -> {
                                            // 초기화 성공

                                            (context as Activity).runOnUiThread {
                                                if (adapterClassName == "com.google.android.gms.ads.MobileAds") {
                                                    _adMobInitialComplete.value = true
                                                }
                                                RLog.d(
                                                    TAG,
                                                    "initAdMob init complete ${_adMobInitialComplete.value}",
                                                )
                                            }
                                        }

                                        AdapterStatus.State.NOT_READY -> {
                                            // 초기화 실패 또는 준비되지 않음
                                            (context as Activity).runOnUiThread {
                                                if (adapterClassName == "com.google.android.gms.ads.MobileAds") {
                                                    _adMobInitialComplete.value = false
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }

        fun showAppOpenAd(context: Context) {
            appOpenAdManager.showAdIfAvailable(
                context as Activity,
                object : AppOpenAdManager.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        RLog.d(
                            TAG,
                            "aaaa onShowAdComplete gatherConsentFinished ${gatherConsentFinished.get()}",
                        )
                        // Check if the consent form is currently on screen before moving to the main
                        // activity.
                        if (gatherConsentFinished.get() || !appOpenAdManager.isAdAvailable()) {
                            goMainHome()
                        }
                    }
                },
            )
        }

        fun openAdLoad(context: Context) {
            appOpenAdManager.loadAd(context)
        }

        fun goMainHome() {
            viewModelScope.launch {
                navigator.navigateTo(Destination.Home.route, true)
            }
        }

        fun setAdMobBannerLoadComplete(addBanner: Pair<Boolean, Int>) {
            _adBannerLoadingCompleteAndGetAdSize.value = addBanner
        }

        suspend fun createAdView(
            context: Context,
            adSize: AdSize,
            unitId: String,
            onChange: (Boolean) -> Unit,
        ): AdView =
            suspendCancellableCoroutine { cont ->
                setAdLoadStart(true) // 광고 로드 시작을 false로 설정

                val adView =
                    AdView(context).apply {
                        // AdSize.BANNER
                        adUnitId = unitId // BuildConfig.BANNER_UNIT_ID // 배너 광고 ID ->admob 계정에서 만듦
                        setAdSize(adSize)

                        adListener =
                            object : AdListener() {
                                override fun onAdLoaded() {
                                    // 광고 로드 완료 시, 코루틴을 resume
                                    setAdLoadStart(true)
                                    if (cont.isActive) {
                                        cont.resume(this@apply)
                                    }
                                }

                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    // 광고 로드 실패 시, 코루틴을 예외와 함께 resume
                                    if (cont.isActive) {
                                        RLog.e(TAG, adError.message)
                                    }
                                    setAdMobBannerLoadComplete(Pair(false, 0))
                                    _adBannerFail.value = adError
                                    _adInLineBannerFail.value = adError

                                    when (adError.code) {
                                        AdRequest.ERROR_CODE_INTERNAL_ERROR -> RLog.e(TAG, "Internal error")
                                        AdRequest.ERROR_CODE_INVALID_REQUEST -> RLog.e(TAG, "Invalid request")
                                        AdRequest.ERROR_CODE_NETWORK_ERROR -> RLog.e(TAG, "Network error")
                                        AdRequest.ERROR_CODE_NO_FILL -> RLog.e(TAG, "No fill (No ads available)")
                                    }

                                    onChange(false)
                                }

                                override fun onAdClicked() {
                                    // Code to be executed when the user clicks on an ad.
                                    RLog.d(TAG, "onAdClicked")
                                }

                                override fun onAdClosed() {
                                    // Code to be executed when the user is about to return
                                    // to the app after tapping on an ad.
                                    RLog.d(TAG, "onAdClosed")
                                }

                                override fun onAdImpression() {
                                    // Code to be executed when an impression is recorded
                                    // for an ad.

                                    if (unitId == BuildConfig.BANNER_UNIT_ID) {
                                        setAdMobBannerLoadComplete(Pair(true, UIUtil.adSize(context).height))
                                    }

                                    onChange(false)
                                }

                                override fun onAdOpened() {
                                    // Code to be executed when an ad opens an overlay that
                                    // covers the screen.
                                    RLog.d(TAG, "onAdOpened")
                                }
                            }

                        loadAd(AdRequest.Builder().build())
                    }

                // 코루틴이 취소될 경우 AdView 리소스 정리
                cont.invokeOnCancellation {
                    adView.destroy()
                }
            }

        suspend fun loadNativeAd(context: Context) {
            withContext(Dispatchers.IO) {
                val builder = AdLoader.Builder(context, BuildConfig.NATIVE_AD_UNIT_ID)
                builder.forNativeAd { nativeAd ->

                    RLog.d(TAG, "headline : ${nativeAd.headline}")
                    // nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }
                    RLog.d(TAG, "body : ${nativeAd.body}")
                    RLog.d(TAG, "callToAction : ${nativeAd.callToAction}")
                    RLog.d(TAG, "icon : ${nativeAd.icon}")
                    RLog.d(TAG, "price : ${nativeAd.price}")
                    RLog.d(TAG, "price : ${nativeAd.store}")
                    RLog.d(TAG, "price : ${nativeAd.starRating}")
                    RLog.d(TAG, "advertiser : ${nativeAd.advertiser}")
                    _adNative.value = nativeAd
                }

                val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
                val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
                builder.withNativeAdOptions(adOptions)

                val adLoader =
                    builder
                        .withAdListener(
                            object : AdListener() {
                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    RLog.d(TAG, "Ad failed to load: ${adError.message}")
                                    _adNativeFail.value = adError
                                }
                            },
                        ).build()
                adLoader.loadAd(AdRequest.Builder().build())
            }
        }

        companion object {
            const val TAG: String = "ADVIEW"
        }
    }
