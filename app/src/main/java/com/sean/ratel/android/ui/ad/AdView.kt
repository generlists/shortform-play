package com.sean.ratel.android.ui.ad

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.log.GAKeys.AD_PROMOTION_BUTTON_TYPE
import com.sean.ratel.android.data.log.GAKeys.MAIN_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdBannerLocation.BOTTOM
import com.sean.ratel.android.ui.ad.AdBannerLocation.TOP
import com.sean.ratel.android.ui.common.findActivity
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.PremiumPopup
import so.smartlab.common.ad.admob.data.model.AdMobBannerState
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.ad.admob.ui.kind.AdaptiveInLineBannerView
import so.smartlab.common.ad.admob.ui.kind.FixedBannerView
import so.smartlab.common.utils.log.RLog

const val TAG = "ADView"

@Suppress("ktlint:standard:function-naming")
@Composable
fun AdBannerView(
    activity: Activity?,
    currentRoute: String,
    adBannerLocation: AdBannerLocation = BOTTOM,
    billingViewModel: BillingViewModel,
    homeMainViewModel: MainViewModel,
    adViewModel: AdViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val adMobInitState by homeMainViewModel.adMobinitState.collectAsState()
    val adFixedBannerState by homeMainViewModel.fixedBannerState.collectAsState()
    var bottomBarHeight = adViewModel.bottomBarHeight.value
    var adSize by remember { mutableStateOf(64) }
    var initAdMob by remember { mutableStateOf(false) }
    val isRemoveAds by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
    var onRemoveAdsClick by remember { mutableStateOf(false) }
    val premiumSheetData by billingViewModel.premiumData.collectAsStateWithLifecycle(
        initialValue = UiState.Idle,
    )

    //   RLog.d("KKKKKKK", "currentRoute : $currentRoute , initAdMob : $initAdMob")
    if (isRemoveAds) return

    if (adMobInitState == AdMobInitState.InitComplete) {
        initAdMob = true
    }
    RLog.d("AdView", "currentRoute : $currentRoute , initAdMob : $initAdMob")

    LaunchedEffect(initAdMob, activity) {
        activity?.let {
            RLog.d("AdView", "requestBannerAdView : $currentRoute")
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
                    .fillMaxSize(),
            contentAlignment = alignment,
        ) {
            Column {
                when (val state = premiumSheetData) {
                    is UiState.Success -> {
                        RemoveAdsChip(
                            promotionTitle = state.data.promoTitle,
                            isSaleActive = state.data.isPromotionActive,
                            discountPercent = state.data.discountPercent ?: "39",
                            onClick = {
                                onRemoveAdsClick = true
                                billingViewModel.sendGALog(
                                    screenName = GASplashAnalytics.SCREEN_NAME.get(MAIN_SCREEN) ?: "",
                                    eventName = GASplashAnalytics.Event.SELECT_MAIN_AD_PROMOTION_ITEM_CLICK,
                                    actionName = GASplashAnalytics.Action.CLICK,
                                    parameter = mapOf(),
                                )
                            },
                        )
                    }

                    else -> {
                        Unit
                    }
                }
                FixedBannerView(
                    Color.Black,
                    Color.Black,
                    APP_TEXT_COLOR,
                    adFixedBannerState,
                )
            }
            if (onRemoveAdsClick) {
                PremiumPopup(
                    billingViewModel,
                    premiumSheetData,
                    forceDonotMessageRow = true,
                    show = { isShow, promotionButtonType ->
                        onRemoveAdsClick = isShow
                        billingViewModel.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME.get(MAIN_SCREEN) ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_AD_VIEW_POPUP_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            parameter = mapOf(AD_PROMOTION_BUTTON_TYPE to promotionButtonType.name),
                        )
                    },
                )
            }
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

@Composable
@Suppress("ktlint:standard:function-naming")
private fun RemoveAdsChip(
    promotionTitle: String,
    isSaleActive: Boolean,
    discountPercent: String,
    onClick: () -> Unit,
) {
    val backgroundColor = APP_TEXT_COLOR.copy(alpha = 0.15f)

    val borderColor = APP_TEXT_COLOR.copy(alpha = 0.4f)

    val contentColor = APP_TEXT_COLOR
    // 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "chipPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "chipScale",
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier =
                Modifier
                    .scale(scale)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(14.dp),
                    ).background(backgroundColor)
                    .clickable { onClick() },
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text =
                        if (isSaleActive && discountPercent.isNotEmpty()) {
                            "$promotionTitle · $discountPercent↓"
                        } else {
                            promotionTitle
                        },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
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
