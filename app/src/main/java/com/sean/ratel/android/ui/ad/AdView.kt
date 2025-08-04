package com.sean.ratel.android.ui.ad

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.common.STRINGS.MAX_ADAPTIVE_BANNER_SIZE
import com.sean.ratel.android.databinding.NativeAdBinding
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil
import com.sean.ratel.android.utils.UIUtil.adInLineAdaptiveBannerSize
import com.sean.ratel.android.utils.UIUtil.pixelToDp
import kotlinx.coroutines.launch

const val TAG = "ADView"

@Suppress("ktlint:standard:function-naming")
/**
 * 하단 띠배너 생성
 */
@Composable
fun LoadBanner(
    currentRoute: String,
    adViewModel: AdViewModel?,
) {
    RLog.d(TAG, "LoadBanner!! Start $currentRoute")

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var progress by remember { mutableStateOf(true) }
    val adLoadStart = adViewModel?.adLoadStart?.collectAsState()
    var adView by remember { mutableStateOf<AdView?>(null) }
    var isSetting = currentRoute == Destination.Setting.route
    var isMain = currentRoute == Destination.Home.Main.route
    val height =
        if (currentRoute == Destination.AppManager.route ||
            currentRoute == Destination.SettingAppManagerDetail.route ||
            currentRoute == Destination.Home.Main.PoplarShortFormMore.route ||
            currentRoute == Destination.Home.Main.EditorPickMore.route ||
            currentRoute == Destination.Home.Main.RecommendMore.route ||
            currentRoute == Destination.Home.Main.RankingChannelMore.route ||
            currentRoute == Destination.Home.Main.RecentlyWatchMore.route
        ) {
            0
        } else {
            adViewModel?.bottomBarHeight?.value
        }

    // AdView 생성은 ViewModel에서 처리
    LaunchedEffect(Unit) {
        adView =
            adViewModel?.createAdView(
                context,
                UIUtil.adSize(context),
                BuildConfig.BANNER_UNIT_ID,
            ) { b -> progress = b }
    }

    // 생명주기 처리
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView?.resume()
                    Lifecycle.Event.ON_PAUSE -> adView?.pause()
                    Lifecycle.Event.ON_DESTROY -> adView?.destroy()
                    else -> {}
                }
            }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    if (adLoadStart?.value == true) {
        BannerView(adView, height?.dp ?: 56.dp, progress, adViewModel, isSetting, isMain)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BannerView(
    adView: AdView?,
    height: Dp,
    progress: Boolean,
    adViewModel: AdViewModel?,
    isSetting: Boolean,
    isMain: Boolean,
) {
    val adBannerFail = adViewModel?.adBannerFail?.collectAsState()
    val adBannerLoadingComplete = adViewModel?.adBannerLoadingCompleteAndGetAdSize?.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    val adBannerSize =
        if (adBannerLoadingComplete?.value?.second == null ||
            adBannerLoadingComplete.value.second == 0
        ) {
            64.dp // 로딩이 안보여 더미를 보여줌
        } else {
            adBannerLoadingComplete.value.second.dp
        }

    RLog.d(
        "AdView",
        "isSetting : $isSetting , calculateTopPadding :" +
            " ${insetPaddingValue.calculateTopPadding().value.dp} , " +
            "height : ${adBannerLoadingComplete?.value?.second}",
    )
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (isSetting == true) {
                        Modifier.padding(
                            bottom = adBannerSize.value.dp,
                        )
                    } else {
                        Modifier.padding(bottom = insetPaddingValue.calculateTopPadding().value.dp)
                    },
                ).background(Color.Transparent),
        // 하단 바 높이만큼 패딩 추가
        contentAlignment = Alignment.BottomCenter,
    ) {
        val bannerHeight = UIUtil.adSize(LocalContext.current).height
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + STRINGS.REMAIN_AD_MARGIN
        Box(
            Modifier
                .then(
                    if (adBannerFail?.value == null) {
                        Modifier
                            .height(bannerHeight.dp + bottomPadding.value.dp)
                            .padding(bottom = insetPaddingValue.calculateTopPadding())
                            .fillMaxWidth()
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = if (isMain) Alignment.TopCenter else Alignment.BottomCenter,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (adBannerFail?.value == null) {
                            Modifier
                                .height(bannerHeight.dp)
                                .background(APP_BACKGROUND)
                        } else {
                            Modifier
                        },
                    ),
            ) {} // 로딩 배경
            adView?.let {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    // adView 생성
                    factory = { adView },
                    update = { view ->
                        // 뷰 레이아웃을 강제로 다시 요청
                        view.requestLayout()
                    },
                )
            }

            if (progress && adBannerFail?.value == null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(APP_BACKGROUND),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        Modifier
                            .size(18.dp)
                            .padding(1.dp),
                        // 원의 두께 조정
                        strokeWidth = 3.dp,
                        color = APP_TEXT_COLOR,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun InLineAdaptiveBanner(adViewModel: AdViewModel?) {
    RLog.d(TAG, "InLineAdaptiveBanner")
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var progress by remember { mutableStateOf(true) }

    val adSize = adInLineAdaptiveBannerSize(context, MAX_ADAPTIVE_BANNER_SIZE)

    LaunchedEffect(Unit) {
        adView =
            adViewModel?.createAdView(
                context,
                adSize,
                BuildConfig.ADAPTIVE_BANNER_UNIT_ID,
            ) { b -> progress = b }
    }

    InLineAdaptiveBannerView(context, adView, progress, adViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun InLineAdaptiveBannerView(
    context: Context,
    adView: AdView?,
    progress: Boolean,
    adViewModel: AdViewModel?,
) {
    val adInLineBannerFail = adViewModel?.adInLineBannerFail?.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Transparent)
                .padding(top = 7.dp, bottom = 7.dp),
        // Box 내에서 하단 중앙 정렬
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(
                    if (adInLineBannerFail?.value == null) {
                        Modifier
                            .height(
                                pixelToDp(
                                    context,
                                    262f,
                                ).dp,
                            ).background(APP_BACKGROUND)
                    } else {
                        Modifier
                    },
                ),
        ) {} // 로딩 배경
        adView?.let {
            AndroidView(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                factory = { adView },
                update = { view ->
                    view.requestLayout() // 뷰 레이아웃을 강제로 다시 요청
                },
            )
        }

        if (progress && adInLineBannerFail?.value == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    Modifier
                        .size(18.dp)
                        .padding(1.dp),
                    // 원의 두께 조정
                    strokeWidth = 3.dp,
                    color = APP_TEXT_COLOR,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun NativeAdCompose(adViewModel: AdViewModel?) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var progress by remember { mutableStateOf(true) }
    val isDestroy = remember { mutableStateOf(false) }
    val adNative = adViewModel?.adNative?.collectAsState()
    val adNativeFail = adViewModel?.adNativeFail?.collectAsState()
    val coroutine = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }

    // 생명주기 관리를 위한 DisposableEffect
    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> {
                        adNative?.value?.destroy()
                        isDestroy.value = true
                    }

                    else -> {}
                }
            }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    // 광고 로딩 로직
    LaunchedEffect(isLoading) {
        coroutine.launch {
            if (!isLoading.value) {
                adViewModel?.loadNativeAd(context)
                isLoading.value = true
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(
                    if (adNativeFail?.value == null) Modifier.padding(bottom = 10.dp) else Modifier,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(
                    if (adNativeFail?.value == null) {
                        Modifier
                            .height(300.dp)
                            .background(APP_BACKGROUND)
                    } else {
                        Modifier
                    },
                ),
        )

        adNative?.value?.let { nativeAd ->
            BindNativeView(ad = nativeAd)
        }
        progress = if (adNative?.value == null) true else false

        if (progress && adNativeFail?.value == null) {
            CircularProgressIndicator(
                Modifier
                    .size(18.dp)
                    .padding(1.dp),
                strokeWidth = 3.dp,
                color = APP_TEXT_COLOR,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BindNativeView(ad: NativeAd) {
    val context = LocalContext.current

    val adView =
        remember {
            val binding = NativeAdBinding.inflate(LayoutInflater.from(context), null, false)
            binding.adView.mediaView = binding.adMedia

            // 광고 구성 요소들을 바인딩

            binding.adView.headlineView = binding.adHeadline
            binding.adView.bodyView = binding.adBody
            binding.adView.callToActionView = binding.adCallToAction
            binding.adView.iconView = binding.adAppIcon
            binding.adView.priceView = binding.adPrice
            binding.adView.starRatingView = binding.adStars
            binding.adView.storeView = binding.adStore
            binding.adView.advertiserView = binding.adAdvertiser

            // 광고 데이터 연결
            (binding.adView.headlineView as TextView).text = ad.headline

            ad.mediaContent?.let { binding.adMedia.mediaContent = it }

            ad.body?.let { binding.adBody.text = it }
                ?: run { (binding.adBody as? TextView)?.visibility = View.INVISIBLE }

            ad.callToAction?.let { binding.adCallToAction.text = it }
                ?: run { (binding.adCallToAction as? TextView)?.visibility = View.INVISIBLE }

            // 광고 아이콘 설정
            ad.icon?.let {
                (binding.adAppIcon).load(it.uri) {
                    transformations(CircleCropTransformation()) // 원형 이미지 변환 적용
                        .placeholder(R.drawable.ad_circle_shape)
                }
            } ?: run { (binding.adAppIcon as? ImageView)?.visibility = View.INVISIBLE }

            // 기타 선택적 요소 설정
            ad.price?.let { (binding.adPrice as? TextView)?.text = it }
                ?: run { (binding.adPrice as? TextView)?.visibility = View.INVISIBLE }
            ad.store?.let { (binding.adStore as? TextView)?.text = it }
                ?: run { (binding.adStore as? TextView)?.visibility = View.INVISIBLE }
            ad.starRating?.let { (binding.adStars as? RatingBar)?.rating = it.toFloat() }
                ?: run { (binding.adStars as? RatingBar)?.visibility = View.INVISIBLE }
            ad.advertiser?.let { (binding.adAdvertiser as? TextView)?.text = it }
                ?: run { (binding.adAdvertiser as? TextView)?.visibility = View.INVISIBLE }

            // NativeAd 객체 연결
            binding.adView.setNativeAd(ad)

            val mediaContent = ad.mediaContent
            val vc = mediaContent?.videoController

            if (vc != null && mediaContent.hasVideoContent()) {
                vc.videoLifecycleCallbacks =
                    object : VideoController.VideoLifecycleCallbacks() {
                        override fun onVideoEnd() {
                            super.onVideoEnd()
                        }
                    }
            }
            binding.adView
        }
    AndroidView(
        factory = { _ ->
            adView
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .wrapContentHeight(),
    )
}

fun interstitialAd(
    context: Context,
    adViewModel: AdViewModel?,
) {
    var adIsLoading = false
    val interstitialAd = adViewModel?.interstitialAd?.value
    val viewAd = adViewModel?.viewAd?.value

    RLog.d(TAG, "interstitialAd: $interstitialAd, adIsLoading: $adIsLoading viewAd : $viewAd")

    // 광고가 로드 중이거나 이미 로드된 경우 광고 요청을 하지 않음
    if (adIsLoading || interstitialAd != null || viewAd == true) {
        RLog.d(TAG, "Ad is loading or already loaded, skipping request.")
        adViewModel?.setInterstitialAdComplete(true)
        return
    }

    adIsLoading = true
    val adRequest = AdRequest.Builder().build()

    // 광고 로드 시작
    InterstitialAd.load(
        context,
        // 테스트 광고 ID: "ca-app-pub-3940256099942544/1033173712"
        BuildConfig.INTERSTITIALAd_UNIT_ID,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                RLog.d(TAG, "Failed to load ad: ${adError.message}")
                adViewModel?.setInterstitialAd(null)
                adViewModel?.setinterstitialAdFailAdLoadStart(adError)

                adIsLoading = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                RLog.d(TAG, "Ad was loaded successfully.")
                adViewModel?.setInterstitialAd(ad)
                adViewModel?.setInterstitialAdComplete(false)
                adIsLoading = false
            }
        },
    )
}

fun showInterstitialAd(
    context: Context,
    adViewModel: AdViewModel?,
    ad: InterstitialAd?,
) {
    val activity = context as? Activity

    if (activity != null) {
        ad?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    RLog.d(TAG, "Ad was dismissed.")
                    adViewModel?.setInterstitialAd(null)
                    adViewModel?.setInterstitialAdComplete(null)
                    adViewModel?.setViewAd(true)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    RLog.d(TAG, "Ad failed to show: ${adError.message}")
                    adViewModel?.setInterstitialAd(null)
                    adViewModel?.setInterstitialAdComplete(null)
                    adViewModel?.setViewAd(true)
                }

                override fun onAdShowedFullScreenContent() {
                    RLog.d(TAG, "Ad showed fullscreen content.")
                }
            }
        ad?.show(activity)
    } else {
        RLog.d(TAG, "Context is not an Activity, cannot show ad.")
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun NativeAdPreView() {
    RatelappTheme {
        // LoadBanner(Destination.Home.Main.route, null)
        // InLineAdaptiveBanner()
        NativeAdCompose(null)
    }
}
