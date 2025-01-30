package com.sean.ratel.android.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Destination {
    data object Splash : Screen("splash")

    data object DebugMode : Screen("debugmode")

    data object YouTube : DynamicScreen("youtube", "contentId")

    data object Search : Screen("home/search")

    data object AppManager : Screen("detail/appManager")

    data object Setting : Screen("home/setting")

    data object SettingAppManagerDetail : Screen("home/setting/appManager/detail")

    data object SettingAppRate : Screen("home/setting/appManager/rate")

    data object SettingAppLicense : Screen("home/setting/appManager/license")

    data object SettingDeviceNetwork : Screen("home/setting/appManager/network")

    data object SettingPhoneCare : Screen("home/setting/appManager/phonecare")

    data object Notices : Screen("setting/notice")

    data object QNA : Screen("home/qna")

    data object Regal : Screen("setting/regal")

    data object Home : Screen("home") {
        data object Main : Screen("home/main") {
            data object PoplarShortFormMore : Screen("home/main/shortformMore")

            data object EditorPickMore : Screen("home/main/editorPickMore")

            data object RankingChannelMore : Screen("home/main/rankingChannelMore")

            data object RankingSubscriptionMore : Screen("home/main/rankingSubscriptionMore")

            data object RankingSubscriptionUpMore : Screen("home/main/rankingSubscriptionUpMore")

            data object RecommendMore : Screen("home/main/recommendMore")

            data object RecentlyWatchMore : Screen("home/main/recentlyWatchMore")
        }

        data object ShortForm : Screen("home/shortform") {
            data object YouTube : DynamicScreen("youtube", "jsonList")
        }
    }
//    data object My : Screen("my") {
//        data object Setting : Screen("profile/setting")
//    }

    abstract class Screen(
        baseRoute: String,
    ) {
        companion object {
            const val BASE_DEEPLINK_URL = "app://splay"
        }

        open val route = baseRoute
        open val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute"
    }

    abstract class DynamicScreen(
        private val baseRoute: String,
        val routeArgName: String,
    ) : Screen(baseRoute) {
        val navArguments = listOf(navArgument(routeArgName) { type = NavType.StringType })

        override val route = "$baseRoute/{$routeArgName}"
        override val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute/{$routeArgName}"

        fun dynamicRoute(param: String) = "$baseRoute/$param"

        fun dynamicDeeplink(param: String) = "$BASE_DEEPLINK_URL/$baseRoute/$param"
    }
}
