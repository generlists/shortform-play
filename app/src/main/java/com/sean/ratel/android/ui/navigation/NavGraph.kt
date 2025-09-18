package com.sean.ratel.android.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.end.YouTubeContentEnd
import com.sean.ratel.android.ui.end.YouTubeContentEndViewModel
import com.sean.ratel.android.ui.home.main.GridItemMoreView
import com.sean.ratel.android.ui.home.main.ListItemMoreView
import com.sean.ratel.android.ui.home.main.Main
import com.sean.ratel.android.ui.home.main.MainMoreViewModel
import com.sean.ratel.android.ui.home.main.MainVideoViewModel
import com.sean.ratel.android.ui.home.setting.Setting
import com.sean.ratel.android.ui.home.setting.SettingViewModel
import com.sean.ratel.android.ui.home.setting.SettingsAppManager
import com.sean.ratel.android.ui.home.shortform.ShortForm
import com.sean.ratel.android.ui.home.shortform.ShortFormViewModel
import com.sean.ratel.android.ui.splash.Splash
import com.sean.ratel.android.ui.splash.SplashViewModel
import com.sean.ratel.android.ui.toolbox.AppManagerView
import com.sean.ratel.android.ui.toolbox.AppManagerViewModel
import kotlinx.coroutines.flow.combine

@Suppress("ktlint:standard:function-naming")
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Destination.Splash.route,
    navigator: Navigator,
    finish: () -> Unit = {},
) {
    // val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val activity = LocalContext.current as MainActivity
    val mainViewModel: MainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
    val adViewModel: AdViewModel = ViewModelProvider(activity)[AdViewModel::class.java]
    val mainVideoModel: MainVideoViewModel =
        ViewModelProvider(activity)[MainVideoViewModel::class.java]
    val splashViewModel: SplashViewModel = hiltViewModel(key = SplashViewModel.TAG)

    LaunchedEffect(Unit) {
        combine(
            splashViewModel.shortformList,
            splashViewModel.trendsShortsList,
            splashViewModel.mainTrendShortsList,
        ) { mainData, trendsShortsData, mainTrendShorts ->

            Triple(mainData, trendsShortsData, mainTrendShorts)
        }.collect { combinedResult ->
            val (main, trends, mainTrends) = combinedResult
            mainViewModel.mainShortsData(main, trends, mainTrends)
        }
    }

    NavHandler(
        navController = navController,
        navigator = navigator,
        finish = finish,
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        // Splash
        composable(Destination.Splash.route) {
            Splash(splashViewModel, adViewModel)
        }

        // Home
        navigation(
            route = Destination.Home.route,
            startDestination = Destination.Home.Main.route,
        ) {
            composable(
                Destination.Home.Main.route,
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                Main(modifier, mainVideoModel, mainViewModel, adViewModel)
            }

            composable(
                Destination.Home.ShortForm.route,
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                val viewModel: ShortFormViewModel = hiltViewModel(key = ShortFormViewModel.TAG)
                val mainData = splashViewModel.shortformList.collectAsState().value

                viewModel.mainVideoData(mainData.first)
                ShortForm(modifier, mainViewModel, viewModel, adViewModel)
            }
            composable(
                Destination.Setting.route,
                enterTransition = { EnterTransition.None },
            ) {
                val viewModel: SettingViewModel = hiltViewModel(key = SettingViewModel.TAG)
                Setting(viewModel, mainViewModel, adViewModel)
            }
        }
        // End
        composable(
            route = Destination.YouTube.route,
            arguments = Destination.YouTube.navArguments,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) { _ ->

            val viewModel: YouTubeContentEndViewModel =
                hiltViewModel(key = YouTubeContentEndViewModel.TAG)

            YouTubeContentEnd(mainViewModel, viewModel)
        }

        composable(
            Destination.Home.Main.PoplarShortFormMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.EditorPickMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.RecommendMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.RankingChannelMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.RankingSubscriptionMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.RankingSubscriptionUpMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.RecentlyWatchMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(adViewModel, mainViewModel, viewModel)
        }
        composable(
            Destination.Home.Main.TrendShortsMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(adViewModel, mainViewModel, viewModel)
        }

        composable(
            Destination.AppManager.route,
        ) {
            val appManagerViewModel: AppManagerViewModel =
                hiltViewModel(key = AppManagerViewModel.TAG)

            AppManagerView(modifier, appManagerViewModel, mainViewModel, adViewModel)
        }

        composable(Destination.SettingAppManagerDetail.route) {
            val viewModel: SettingViewModel = hiltViewModel(key = SettingViewModel.TAG)
            SettingsAppManager(viewModel, mainViewModel, adViewModel)
        }
    }
}
