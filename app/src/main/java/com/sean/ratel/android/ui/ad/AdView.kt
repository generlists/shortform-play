package com.sean.ratel.android.ui.ad

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.ui.ad.AdBannerLocation.BOTTOM
import com.sean.ratel.android.ui.ad.AdBannerLocation.TOP
import com.sean.ratel.android.ui.common.findActivity
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import so.smartlab.common.ad.admob.data.model.AdMobBannerState
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.ad.admob.ui.kind.AdaptiveInLineBannerView
import so.smartlab.common.ad.admob.ui.kind.FixedBannerView

const val TAG = "ADView"

@Suppress("ktlint:standard:function-naming")
@Composable
fun AdBannerView(
    activity: Activity?,
    currentRoute: String,
    adBannerLocation: AdBannerLocation = BOTTOM,
    homeMainViewModel: MainViewModel = hiltViewModel(),
    adViewModel: AdViewModel = hiltViewModel(),
) {
    val adMobInitState by homeMainViewModel.adMobinitState.collectAsState()
    val adFixedBannerState by homeMainViewModel.fixedBannerState.collectAsState()
    var bottomBarHeight = adViewModel.bottomBarHeight.value
    var adSize by remember { mutableStateOf(64) }
    var initAdMob by remember { mutableStateOf(false) }

    RLog.d("KKKKKKK", "currentRoute : $currentRoute")

    if (adMobInitState == AdMobInitState.InitComplete) {
        initAdMob = true
    }

    LaunchedEffect(initAdMob, activity) {
        activity?.let {
            RLog.d("KKKKKKK", "requestBannerAdView : $currentRoute")
            homeMainViewModel.requestBannerAdView(it, admobBannerId = currentRoute)
        }
    }
    RLog.d("AdView", "adFixedBannerState : $adFixedBannerState activity : $activity")
    when {
        adFixedBannerState is AdMobBannerState.AdError -> {
            bottomBarHeight = 0
        }

        adFixedBannerState is AdMobBannerState.AdLoad -> {
            adSize = (adFixedBannerState as AdMobBannerState.AdLoad).adSize.height
            RLog.d("AdView", "adSize : $adSize")
        }
    }

    val alignment = if (adBannerLocation == TOP) Alignment.TopCenter else Alignment.BottomCenter
    if (initAdMob) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = (
                            if (isBottomBar(currentRoute)) {
                                bottomBarHeight.dp
                            } else if (currentRoute ==
                                Destination.Search.route
                            ) {
                                0.dp
                            } else {
                                adSize.dp
                            }
                        ),
                    ),
            contentAlignment = alignment,
        ) {
            FixedBannerView(
                Color.Black,
                Color.Black,
                APP_TEXT_COLOR,
                adFixedBannerState,
            )
        }

        RLog.d("AdView", "AdView  $adFixedBannerState , requestBanner $activity")
    }
}

private fun isBottomBar(route: String) =
    (
        route == Destination.Home.Main.route ||
            route == Destination.Home.ShortForm.route ||
            route == Destination.Setting.route
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun AdaptiveBanner(
    homeMainViewModel: MainViewModel = hiltViewModel(),
    adViewModel: AdViewModel,
    onHeightChanged: (Int) -> Unit = {},
) {
    RLog.d(TAG, "InLineAdaptiveBanner")
    val adMobInitState by homeMainViewModel.adMobinitState.collectAsState()
    val adaptiveInLineBannerState by homeMainViewModel.adaptiveInlineBannerState.collectAsState()
    var padding by remember { mutableStateOf(64) }
    var adSize by remember { mutableStateOf(64) }
    var initAdMob by remember { mutableStateOf(false) }
    val activity = LocalContext.current.findActivity()
    RLog.d("AdView", "adMobInitState : $adMobInitState")

    if (adMobInitState == AdMobInitState.InitComplete) {
        initAdMob = true
    }

    LaunchedEffect(initAdMob) {
        activity?.let {
            homeMainViewModel.requestInLineBannerAdView(it)
        }
    }
    RLog.d("AdView", "adaptiveInLineBannerState : $adaptiveInLineBannerState")
    when {
        adaptiveInLineBannerState is AdMobBannerState.AdError -> {
            padding = 0
        }

        adaptiveInLineBannerState is AdMobBannerState.AdLoadComplete -> {
            adSize = (adaptiveInLineBannerState as AdMobBannerState.AdLoadComplete).adSize.height
            padding = if (adSize > 0) adSize / 2 else adSize
            RLog.d("AdView", "adSize : $adSize")
        }
    }

    if (initAdMob) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = padding.dp, bottom = padding.dp)
                .onGloballyPositioned { coordinates ->
                    onHeightChanged(coordinates.size.height)
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            RLog.d("AdView", "AdView requestBanner $activity")

            AdaptiveInLineBannerView(
                Color.Black,
                APP_TEXT_COLOR,
                adMobBannerState = adaptiveInLineBannerState,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun NativeAdPreView() {
    RatelappTheme {
    }
}
