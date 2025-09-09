package com.sean.ratel.android.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme

@Keep
enum class HomeTab(
    @StringRes val title: Int,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val route: String,
    val destRoute: String,
) {
    MAIN(
        R.string.main,
        R.drawable.ic_main_selected,
        R.drawable.ic_main_unselected,
        Destination.Home.Main.route,
        Destination.Home.Main.route,
    ),
    VIDEO(
        R.string.video,
        R.drawable.ic_short_video_selected,
        R.drawable.ic_short_video_unselected,
        Destination.Home.ShortForm.route,
        Destination.Home.ShortForm.route,
    ),
    SETTINGS(
        R.string.setting,
        R.drawable.ic_settings_selected,
        R.drawable.ic_settings_unselected,
        Destination.Setting.route,
        Destination.Setting.route,
    ),
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeBottomBar(
    navController: NavController,
    viewModel: MainViewModel,
    adViewModel: AdViewModel,
) {
    val tabs = remember { HomeTab.entries.toTypedArray().asList() }
    val routes = remember { HomeTab.entries.map { it.route } }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Destination.Splash.route
    val pipClick = viewModel.pipClick.collectAsState(null)

    viewModel.topViewVisibility(
        currentRoute != Destination.Splash.route &&
            currentRoute != Destination.Search.route &&
            currentRoute != Destination.AppManager.route &&
            currentRoute != Destination.Notices.route &&
            currentRoute != Destination.Setting.route &&
            currentRoute != Destination.SettingAppManagerDetail.route &&
            currentRoute != Destination.Home.Main.PoplarShortFormMore.route &&
            currentRoute != Destination.Home.Main.EditorPickMore.route &&
            currentRoute != Destination.Home.Main.RankingChannelMore.route &&
            currentRoute != Destination.Home.Main.RankingSubscriptionMore.route &&
            currentRoute != Destination.Home.Main.RankingSubscriptionUpMore.route &&
            currentRoute != Destination.Home.Main.RecommendMore.route &&
            currentRoute != Destination.Home.Main.RecentlyWatchMore.route &&
            pipClick.value?.first == false,
    )

    HomeBottomBarView(
        tabs = tabs,
        routes = routes,
        currentRoute = currentRoute,
        tabClick = {
            if (it.route != currentRoute) {
                navController.navigate(it.destRoute) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                        inclusive = true
                    }
                    anim {
                        enter = 0
                        exit = 0
                        popEnter = 0
                        popExit = 0
                    }
                    launchSingleTop = true
                    restoreState = true
                }

                viewModel.setTabClicked(currentRoute)
                viewModel.sendGALog(
                    Event.SCREEN_VIEW,
                    it.destRoute,
                )
                //  if(it.route == Destination.Home.Main.route) viewModel.setTabClicked(true)
            }
        },
        adViewModel = adViewModel,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun HomeBottomBarView(
    tabs: List<HomeTab>,
    routes: List<String>,
    currentRoute: String,
    tabClick: (HomeTab) -> Unit,
    adViewModel: AdViewModel,
) {
    val density = LocalDensity.current
    if (currentRoute in routes) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center,
        ) {
            NavigationBar(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsBottomHeight(
                        WindowInsets.navigationBars.add(WindowInsets(bottom = 56.dp)),
                    ).onGloballyPositioned { coordinates ->
                        val bottomVarHeight =
                            with(density) {
                                coordinates.size.height
                                    .toDp()
                                    .value
                            }
                        adViewModel.setBottomBarHeight(bottomVarHeight.toInt())
                    },
                containerColor = APP_BACKGROUND,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painter =
                                        painterResource(
                                            if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        ),
                                    contentDescription = stringResource(id = tab.title),
                                    modifier = Modifier.size(26.dp),
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = tab.title),
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            selected = selected,
                            onClick = { tabClick(tab) },
                            alwaysShowLabel = true,
                            modifier =
                                Modifier
                                    .navigationBarsPadding(),
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun HomeBottomBarLightPreview() {
    RatelappTheme {
//        HomeBottomBarView(
//            tabs = HomeTab.entries.toTypedArray().asList(),
//            routes = HomeTab.entries.map { it.route },
//            currentRoute = HomeTab.MAIN.route,
//        ) {}
    }
}
