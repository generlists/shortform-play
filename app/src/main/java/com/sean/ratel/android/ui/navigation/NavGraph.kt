package com.sean.ratel.android.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.data.log.GAKeys.TOPIC_DETAIL
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.android.ui.end.YouTubeContentEnd
import com.sean.ratel.android.ui.end.YouTubeContentEndViewModel
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.home.main.GridItemMoreView
import com.sean.ratel.android.ui.home.main.ListItemMoreView
import com.sean.ratel.android.ui.home.main.Main
import com.sean.ratel.android.ui.home.main.MainMoreViewModel
import com.sean.ratel.android.ui.home.main.MainVideoViewModel
import com.sean.ratel.android.ui.home.main.TopicDetailScreen
import com.sean.ratel.android.ui.home.setting.Setting
import com.sean.ratel.android.ui.home.setting.SettingOpenSourceLicensesScreen
import com.sean.ratel.android.ui.home.setting.SettingViewModel
import com.sean.ratel.android.ui.home.setting.SettingsAppManager
import com.sean.ratel.android.ui.home.shortform.ShortForm
import com.sean.ratel.android.ui.home.shortform.ShortFormViewModel
import com.sean.ratel.android.ui.my.AccountViewModel
import com.sean.ratel.android.ui.my.FilterEditScreen
import com.sean.ratel.android.ui.my.HiddenVideosScreen
import com.sean.ratel.android.ui.my.LibraryFolderScreen
import com.sean.ratel.android.ui.my.LibraryViewModel
import com.sean.ratel.android.ui.my.MyScreen
import com.sean.ratel.android.ui.onboarding.OnboardingScreen
import com.sean.ratel.android.ui.push.NotificationScreen
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.splash.Splash
import com.sean.ratel.android.ui.splash.SplashViewModel
import com.sean.ratel.android.ui.toolbox.AppManagerView
import com.sean.ratel.android.ui.toolbox.AppManagerViewModel
import kotlinx.coroutines.flow.combine
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Destination.Splash.route,
    navigator: Navigator,
    youTubePlayersManager: YouTubePlayersManager,
    finish: () -> Unit = {},
) {
    // val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

    val activity = LocalActivity.current as MainActivity

    val mainViewModel: MainViewModel = ViewModelProvider(activity)[MainViewModel::class.java]
    val pushViewModel: PushViewModel = ViewModelProvider(activity)[PushViewModel::class.java]
    // 마이 탭과 필터 편집 화면이 같은 상태(로그인, 편집 중인 필터)를 쓴다
    val accountViewModel: AccountViewModel = ViewModelProvider(activity)[AccountViewModel::class.java]
    val billingViewModel: BillingViewModel = ViewModelProvider(activity)[BillingViewModel::class.java]
    // 보관함(폴더 화면과 재생 화면의 저장 시트가 같은 상태를 쓴다)
    val libraryViewModel: LibraryViewModel = ViewModelProvider(activity)[LibraryViewModel::class.java]

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
            Splash(
                splashViewModel = splashViewModel,
                mainViewModel = mainViewModel,
                pushViewModel = pushViewModel,
                billingViewModel = billingViewModel,
                finish = finish,
            )
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
                RLog.d("shortformFilter", "Home")
                Main(modifier, mainVideoModel, mainViewModel, billingViewModel)
            }

            composable(
                route = Destination.Home.Main.ShortForm.route,
                arguments = Destination.Home.Main.ShortForm.navArguments,
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) { backStackEntry ->
                val viewModel: ShortFormViewModel = hiltViewModel(key = ShortFormViewModel.TAG)
                // 내 제외 필터가 적용된 홈 데이터(MainViewModel). splashViewModel.shortformList 는 원본이라 필터가 빠진다
                val mainData = mainViewModel.mainShorts.collectAsState().value

                LaunchedEffect(mainData.first) {
                    viewModel.mainVideoData(mainData.first)
                }

                val filter =
                    backStackEntry.arguments
                        ?.getString("filter")
                        ?.takeUnless { it == "{filter}" }

                RLog.d("NaviGraph", "navi grapth  :filter : $filter")
                ShortForm(modifier, mainViewModel, viewModel, filter)
            }

            composable(
                Destination.My.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                MyScreen(accountViewModel, mainViewModel, billingViewModel)
            }

            composable(
                Destination.Setting.route,
                enterTransition = { EnterTransition.None },
            ) {
                val viewModel: SettingViewModel = hiltViewModel(key = SettingViewModel.TAG)
                Setting(viewModel, mainViewModel, pushViewModel, billingViewModel)
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
        ) { backStackEntry ->

            val viewModel: YouTubeContentEndViewModel =

                hiltViewModel(key = YouTubeContentEndViewModel.TAG)

            val param =
                backStackEntry.arguments

                    ?.getString(Destination.YouTube.ARG_PARAM)

            val topicId =
                backStackEntry.arguments

                    ?.getString(Destination.YouTube.ARG_TOPIC_ID)

            val filterType =
                backStackEntry.arguments

                    ?.getString(Destination.YouTube.ARG_FILTER_TYPE)

            YouTubeContentEnd(mainViewModel, viewModel, youTubePlayersManager)
        }

        composable(
            Destination.Home.Main.PoplarShortFormMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.EditorPickMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.RecommendMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.RankingChannelMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.RankingSubscriptionMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.RankingSubscriptionUpMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.RecentlyWatchMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            ListItemMoreView(mainViewModel, viewModel, billingViewModel)
        }
        composable(
            Destination.Home.Main.TrendShortsMore.route,
            enterTransition = { EnterTransition.None },
        ) {
            val viewModel: MainMoreViewModel = hiltViewModel(key = MainMoreViewModel.TAG)
            GridItemMoreView(mainViewModel, viewModel, billingViewModel)
        }

        composable(
            Destination.AppManager.route,
        ) {
            val appManagerViewModel: AppManagerViewModel =
                hiltViewModel(key = AppManagerViewModel.TAG)

            AppManagerView(modifier, appManagerViewModel, mainViewModel, billingViewModel)
        }

        composable(Destination.FilterEdit.route) {
            FilterEditScreen(accountViewModel, mainViewModel)
        }

        composable(
            Destination.LibraryFolder.route,
            arguments = Destination.LibraryFolder.navArguments,
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString(Destination.LibraryFolder.ARG_FOLDER_ID).orEmpty()
            LibraryFolderScreen(folderId, libraryViewModel, mainViewModel)
        }

        composable(Destination.HiddenVideos.route) {
            HiddenVideosScreen(accountViewModel, mainViewModel)
        }

        composable(Destination.Onboarding.route) {
            OnboardingScreen(accountViewModel, mainViewModel)
        }

        composable(Destination.SettingAppManagerDetail.route) {
            val viewModel: SettingViewModel = hiltViewModel(key = SettingViewModel.TAG)
            SettingsAppManager(viewModel, mainViewModel, billingViewModel)
        }
        composable(Destination.SettingAppLicense.route) {
            SettingOpenSourceLicensesScreen(modifier, billingViewModel)
        }
        composable(
            Destination.Home.Main.TopicListDetail.route,
            arguments = Destination.Home.Main.TopicListDetail.navArguments,
        ) { backStackEntry ->
            LaunchedEffect(backStackEntry.destination.route) {
                mainViewModel.sendGALog(GASplashAnalytics.SCREEN_NAME.get(TOPIC_DETAIL) ?: "")
            }
            val topicId = backStackEntry.arguments?.getString("topicId")
            val filterType = backStackEntry.arguments?.getString("filterType")

            RLog.d("hbungshin", "111111 topicId : $topicId ,  filterType : $filterType")

            topicId?.let { topicId ->
                TopicDetailScreen(modifier, topicId, mainViewModel, billingViewModel)
            }
        }

        composable(
            Destination.Notifcation.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            NotificationScreen(modifier, mainViewModel, pushViewModel, billingViewModel)
        }
    }
}
