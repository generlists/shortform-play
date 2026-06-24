package com.sean.ratel.android

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdBannerView
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.end.YouTubeEndMoreView
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.home.HomeBottomBar
import com.sean.ratel.android.ui.home.HomeTopBar
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.NavGraph
import com.sean.ratel.android.ui.progress.LoadingPlaceholder
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.PremiumPopup
import com.sean.ratel.android.utils.findActivity
import com.sean.ratel.player.ui.ThemeMode
import kotlinx.coroutines.delay
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.iap.ui.PremiumSheetColors
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormPlayApp(
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
    finish: () -> Unit,
) {
    RatelappTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val isTopViewVisible by mainViewModel.isTopViewVisible.collectAsState() // recomposition 이 일어나지않으면 값이 안바뀌므로 stateFlow 로 선언

        val adMobInitialComplete by mainViewModel.adMobinitState.collectAsState()

        val isHomeVisible by mainViewModel.isHomeVisible.collectAsState()

        val currentRoute = navBackStackEntry?.destination?.route ?: Destination.Splash.route
        val itemClick by remember { mainViewModel.itemClicked }
        var endMoreClick by remember { mutableStateOf(false) }
        var currentShorts by remember { mutableStateOf<MainShortsModel?>(null) }
        val context = LocalContext.current
        val activity = context.findActivity()
        val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        var showPromotionPopup by remember { mutableStateOf(true) }
        val isSaleActive by billingViewModel.adSaleActive.collectAsStateWithLifecycle()
        val isDonotAain by billingViewModel.doNotShowAgain.collectAsStateWithLifecycle()
        val isAdRemoved by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
        val toastMessage by billingViewModel.toastMessage.collectAsStateWithLifecycle(initialValue = null)

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (isTopViewVisible) {
                    HomeTopBar(
                        modifier = Modifier.padding(top = insetPaddingValue),
                        mainViewModel = mainViewModel,
                        pushViewModel = pushViewModel,
                        billingViewModel = billingViewModel,
                        isHomeNaviBar = currentRoute,
                        historyBack = {
                            mainViewModel.runNavigationBack(Destination.YouTube.route)
                        },
                        privacyOptionClick = { mainViewModel.runPrivacyOptionMenu(activity) },
                        notificationPage = { pushViewModel.goNotificationPage() },
                        endMoreClick = {
                            endMoreClick = true
                            currentShorts = it
                        },
                    )
                }
            },
            bottomBar = {
                HomeBottomBar(
                    navController = navController,
                    mainViewModel,
                    adViewModel,
                )
            },
            floatingActionButtonPosition = FabPosition.End,
        ) { innerPaddingModifier ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND),
            ) {
                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPaddingModifier),
                    navigator = mainViewModel.navigator,
                    finish = finish,
                )
            }
            ShadowBottomLayer(route = currentRoute)

            if ((
                    currentRoute == Destination.Home.Main.route ||
                        currentRoute == Destination.Setting.route
                ) &&
                adMobInitialComplete is AdMobInitState.InitComplete &&
                RemoteConfig.getRemoteConfigBooleanValue(RemoteConfig.BANNER_AD_VISIBILITY)
            ) {
                AdBannerView(activity, currentRoute, AdBannerLocation.BOTTOM, billingViewModel)
            }

            FullScreenToggleView(currentRoute)
            // 앤드 진입 잔상 없앰
            itemClick?.let {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                )
            }

            if (currentRoute == Destination.Home.Main.route ||
                currentRoute == Destination.Home.Main.PoplarShortFormMore.route ||
                currentRoute == Destination.Home.Main.EditorPickMore.route ||
                currentRoute == Destination.Home.Main.RecommendMore.route ||
                currentRoute == Destination.Home.Main.RankingChannelMore.route ||
                currentRoute == Destination.Home.Main.RankingSubscriptionMore.route ||
                currentRoute == Destination.Home.Main.RankingSubscriptionUpMore.route ||
                currentRoute == Destination.Home.Main.RecentlyWatchMore.route ||
                currentRoute == Destination.Home.Main.TrendShortsMore.route
            ) {
                LoadingPlaceholder(loading = isHomeVisible)
            }

            if (endMoreClick) {
                currentShorts?.let {
                    YouTubeEndMoreView(
                        mainViewModel = mainViewModel,
                        currentShortsModel = it,
                    ) {
                        endMoreClick = false
                    }
                }
            }

            if (
                currentRoute == Destination.Home.Main.route &&
                isSaleActive &&
                showPromotionPopup &&
                !isDonotAain
            ) {
                PremiumPopup(billingViewModel, show = { isShow, buttonType ->
                    RLog.d("In App Purchase", "showPromotionPopup :  $isShow")
                    showPromotionPopup = isShow
                    //                    statisticArgs.sendEvent(
                    //                        EventAction.ViewShow,
                    //                        EventName.MainAdPurchaseShowClick,
                    //                        Screen.Home,
                    //                        mapOf(Parms.AdPromotionButtonType.key to buttonType.name),
                    //                    )
                })
            }

            LaunchedEffect(toastMessage) {
                RLog.e("In App Purchase ", "toastMessage : $toastMessage")
                toastMessage?.let {
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ShadowBottomLayer(route: String) {
    if (
        route == Destination.Home.Main.route ||
        route == Destination.Home.ShortForm.route ||
        route == Destination.Setting.route
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = 0.5f),
                                    ),
                            ),
                    ),
            )
        }
    }
}

@Composable
fun premiumDefault() =
    PremiumSheetColors(
        isDarkTheme = true,
        sheetBackground = APP_BACKGROUND,
        titleTextColor = Color.White,
        subTitleTextColor = Color(0xCCF0F1F4),
        primaryColor = APP_TEXT_COLOR,
        buttonColor = APP_TEXT_COLOR,
        discountBadgeColor = Color(0xFFFF6B35),
        dragHandleColor = Color(0xFFD1D1D6),
        dDayBackgroundColor = APP_TEXT_COLOR.copy(alpha = 0.15f),
        dDayBoarderColor = APP_TEXT_COLOR.copy(alpha = 0.4f),
        dDayContentColor = APP_TEXT_COLOR,
    )
