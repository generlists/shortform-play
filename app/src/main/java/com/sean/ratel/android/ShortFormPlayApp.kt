package com.sean.ratel.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.LoadBanner
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.home.HomeBottomBar
import com.sean.ratel.android.ui.home.HomeTopBar
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.NavGraph
import com.sean.ratel.android.ui.progress.LoadingPlaceholder
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormPlayApp(
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    finish: () -> Unit,
) {
    RatelappTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        val isTopViewVisible by mainViewModel.isTopViewVisible.collectAsState() // recomposition 이 일어나지않으면 값이 안바뀌므로 stateFlow 로 선언

        val adMobInitialComplete by adViewModel.adMobInitialComplete.collectAsState()

        val isHomeVisible by mainViewModel.isHomeVisible.collectAsState()

        val currentRoute = navBackStackEntry?.destination?.route ?: Destination.Splash.route
        val itemClick by remember { mainViewModel.itemClicked }
        val context = LocalContext.current as MainActivity
        val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                if (isTopViewVisible) {
                    HomeTopBar(
                        Modifier.background(Color.Transparent).padding(top = insetPaddingValue),
                        mainViewModel,
                        currentRoute,
                        historyBack = {
                            mainViewModel.runNavigationBack(Destination.YouTube.route)
                        },
                        privacyOptionClick = { mainViewModel.runPrivacyOptionMenu(context) },
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

            Column(modifier = Modifier.fillMaxSize().background(APP_BACKGROUND)) {
                NavGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPaddingModifier),
                    navigator = mainViewModel.navigator,
                    finish = finish,
                )
            }

            if ((
                    currentRoute != Destination.Splash.route &&
                        currentRoute != Destination.Home.Main.route &&
                        currentRoute != Destination.Home.ShortForm.route &&
                        // 구글정책상 한페이지에 하나의 광고만허용
                        currentRoute != Destination.YouTube.route &&
                        currentRoute != Destination.Notices.route &&
                        currentRoute != Destination.Regal.route
                ) &&
                adMobInitialComplete &&
                RemoteConfig.getRemoteConfigBooleanValue(RemoteConfig.BANNER_AD_VISIBILITY)
            ) {
                LoadBanner(currentRoute, adViewModel)
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
                currentRoute == Destination.Home.Main.RecentlyWatchMore.route
            ) {
                LoadingPlaceholder(loading = isHomeVisible)
            }
        }
    }
}
