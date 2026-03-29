package com.sean.ratel.android

import androidx.compose.foundation.background
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.ui.ad.AdBannerView
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.home.HomeBottomBar
import com.sean.ratel.android.ui.home.HomeTopBar
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.NavGraph
import com.sean.ratel.android.ui.progress.LoadingPlaceholder
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme
import so.smartlab.common.ad.admob.data.model.AdMobInitState

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormPlayApp(
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    pushViewModel: PushViewModel,
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
        val context = LocalContext.current as MainActivity
        val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (isTopViewVisible) {
                    HomeTopBar(
                        modifier = Modifier.padding(top = insetPaddingValue),
                        mainViewModel,
                        pushViewModel,
                        currentRoute,
                        historyBack = {
                            mainViewModel.runNavigationBack(Destination.YouTube.route)
                        },
                        privacyOptionClick = { mainViewModel.runPrivacyOptionMenu(context) },
                        notificationPage = { pushViewModel.goNotificationPage() },
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
                    currentRoute != Destination.Splash.route &&
                        // currentRoute != Destination.Home.Main.route &&
                        currentRoute != Destination.Home.ShortForm.route &&
                        // 구글정책상 한페이지에 하나의 광고만허용
                        currentRoute != Destination.YouTube.route &&
                        currentRoute != Destination.Notices.route &&
                        currentRoute != Destination.Regal.route
                ) &&
                adMobInitialComplete is AdMobInitState.InitComplete &&
                RemoteConfig.getRemoteConfigBooleanValue(RemoteConfig.BANNER_AD_VISIBILITY)
            ) {
                AdBannerView(context, currentRoute)
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
