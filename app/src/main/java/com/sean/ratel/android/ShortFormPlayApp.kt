package com.sean.ratel.android

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GAKeys.AD_PROMOTION_BUTTON_TYPE
import com.sean.ratel.android.data.log.GAKeys.MAIN_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.data.log.GASplashAnalytics.Param.CAST_VIEW_TYPE
import com.sean.ratel.android.ui.cast.CastControlBar
import com.sean.ratel.android.ui.cast.CastSessionState
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.end.LoadingArea
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
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState
import so.smartlab.common.iap.ui.PremiumSheetColors
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormPlayApp(
    mainViewModel: MainViewModel,
    pushViewModel: PushViewModel,
    billingViewModel: BillingViewModel,
    youTubePlayersManager: YouTubePlayersManager,
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
        val premiumSheetData by billingViewModel.premiumData.collectAsStateWithLifecycle(
            initialValue = UiState.Idle,
        )
        val interstitialDisMissCount by billingViewModel.interstitialAdDisMissCount.collectAsStateWithLifecycle(
            initialValue = 0,
        )
        val castLoading by youTubePlayersManager.castConnectLoading.collectAsStateWithLifecycle()
        val castSession by youTubePlayersManager.castEventManager.castSession.collectAsStateWithLifecycle()
        val settingSoundOff by youTubePlayersManager.getSoundOff().collectAsStateWithLifecycle(initialValue = false)
        val isMute by youTubePlayersManager.mute.collectAsStateWithLifecycle(initialValue = settingSoundOff)
        val settingCaptionOnOff by youTubePlayersManager.getCaptionEnabled().collectAsStateWithLifecycle(initialValue = true)
        val captionOnOff by youTubePlayersManager.captionOnOff.collectAsStateWithLifecycle(initialValue = settingCaptionOnOff)
        val currentVideo by youTubePlayersManager.currentVideo.collectAsStateWithLifecycle()
        val castPlayState by youTubePlayersManager.castPlayState.collectAsStateWithLifecycle()
        val playCaptionRetain by youTubePlayersManager.retainSoundCaption.collectAsStateWithLifecycle()

        RLog.d(
            "SSLLGGGGGG",
            "isMute : $isMute, captionOnOff : $captionOnOff settingCaption = $settingCaptionOnOff",
        )

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (isTopViewVisible) {
                    HomeTopBar(
                        modifier = Modifier.padding(top = insetPaddingValue),
                        mainViewModel = mainViewModel,
                        pushViewModel = pushViewModel,
                        billingViewModel = billingViewModel,
                        playersManager = youTubePlayersManager,
                        castConnectLoading = castLoading,
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
                        castClickRoute = {
                            youTubePlayersManager.setCurrentRoute(it)
                            mainViewModel.sendGALog(
                                screenName = GASplashAnalytics.SCREEN_NAME.get(MAIN_SCREEN) ?: "",
                                eventName = GASplashAnalytics.Event.SELECT_BTN_CAST_CLICK,
                                actionName = GASplashAnalytics.Action.CLICK,
                                mapOf(CAST_VIEW_TYPE to it),
                            )
                        },
                    )
                }
            },
            bottomBar = {
                HomeBottomBar(
                    navController = navController,
                    mainViewModel,
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
                    youTubePlayersManager = youTubePlayersManager,
                    finish = finish,
                )
            }

            when (val state = premiumSheetData) {
                is UiState.Loading, UiState.Idle -> {
                    // 로딩 인디케이터
                    LoadingArea(isLoading = true)
                }

                is UiState.Error -> {
                    if (currentRoute != Destination.Splash.route) {
                        LaunchedEffect(Unit) {
                            RLog.d("KKKKKKK", "errorMessage : ${state.message}")
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                else -> {
                    Unit
                }
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
                (
                    currentRoute == Destination.Home.Main.route &&
                        isSaleActive &&
                        showPromotionPopup &&
                        !isDonotAain
                ) || (interstitialDisMissCount == 3)
            ) {
                PremiumPopup(billingViewModel, premiumSheetData, show = { isShow, buttonType ->
                    RLog.d("In App Purchase", "showPromotionPopup :  $isShow")
                    showPromotionPopup = isShow

                    billingViewModel.sendGALog(
                        screenName = GASplashAnalytics.SCREEN_NAME.get(MAIN_SCREEN) ?: "",
                        eventName = GASplashAnalytics.Event.SELECT_AD_VIEW_POPUP_SHOW,
                        actionName = GASplashAnalytics.Action.VIEW,
                        parameter = mapOf(AD_PROMOTION_BUTTON_TYPE to buttonType.name),
                    )
                    if (interstitialDisMissCount == 3) {
                        billingViewModel.setInterstitialAdDisMissCount(
                            0,
                        )
                    }
                })
            }
            if (castSession is CastSessionState.SessionStart &&
                currentVideo != null && currentRoute != Destination.Splash.route
            ) {
                if (castPlayState is YouTubeStreamPlaybackState.Playing) {
                    LaunchedEffect(Unit) {
                        if (!playCaptionRetain) {
                            RLog.d("SSLLGGGGGG", "LaunchedEffect   isMute : $isMute , captionOnOff : $captionOnOff")
                            youTubePlayersManager.setMute(isMute)
                            youTubePlayersManager.setCaptionOnOff(captionOnOff)
                            // 캐스트 종료때까지 설정 사운드 유지
                            youTubePlayersManager.setRetainSound(true)
                        }
                    }
                }

                CastControlBar(
                    currentRoute = currentRoute,
                    playerManager = youTubePlayersManager,
                    mainViewModel = mainViewModel,
                    onPlayPause = { isPlaying ->
                        youTubePlayersManager.castPlayPause(!isPlaying)
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_PLAY_PAUSE_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to isPlaying.toString()),
                        )
                    },
                    onPrevious = {
                        youTubePlayersManager.castPrevPlay()
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_PREV_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to "prev"),
                        )
                    },
                    onNext = {
                        youTubePlayersManager.castNextPlay()
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_NEXT_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to "next"),
                        )
                    },
                    onToggleMute = {
                        youTubePlayersManager.setMute(isMute)
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_MUTE_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to isMute.toString()),
                        )
                    },
                    onToggleSubtitle = {
                        youTubePlayersManager.setCaptionOnOff(!captionOnOff)
                    },
                    onSpeedUp = {
                        youTubePlayersManager.speedUp()
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_SPEED_UP_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to "speed_up"),
                        )
                    },
                    onSpeedDown = {
                        youTubePlayersManager.speedDown()
                        youTubePlayersManager.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME[currentRoute] ?: "",
                            eventName = GASplashAnalytics.Event.SELECT_SPEED_DOWN_CAST_CLICK,
                            actionName = GASplashAnalytics.Action.CLICK,
                            mapOf(GASplashAnalytics.Param.CAST_PLAYING_TYPE to "speed_down"),
                        )
                    },
                )
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
