package com.sean.ratel.android.ui.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sean.ratel.android.ui.navigation.Destination.YouTube.ARG_FILTER_TYPE
import com.sean.ratel.android.ui.navigation.Destination.YouTube.ARG_PARAM
import com.sean.ratel.android.ui.navigation.Destination.YouTube.ARG_TOPIC_ID

object Destination {
    data object Splash : Screen("splash")

    data object DebugMode : Screen("debugmode")

    data object YouTube : DynamicScreen(
        baseRoute = "youtube",
        pathArgNames = listOf(ARG_PARAM),
        queryArgNames = listOf(ARG_TOPIC_ID, ARG_FILTER_TYPE),
    ) {
        const val ARG_PARAM = "param"
        const val ARG_TOPIC_ID = "topicId"
        const val ARG_FILTER_TYPE = "filterType"
    }

    data object Search : Screen("home/search")

    data object Notifcation : Screen("home/notification")

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

    data object DeepLink : Screen("deeplink")

    data object Home : Screen("home") {
        data object Main : Screen("home/main") {
            data object PoplarShortFormMore : Screen("home/main/shortformMore")

            data object EditorPickMore : Screen("home/main/editorPickMore")

            data object RankingChannelMore : Screen("home/main/rankingChannelMore")

            data object RankingSubscriptionMore : Screen("home/main/rankingSubscriptionMore")

            data object RankingSubscriptionUpMore : Screen("home/main/rankingSubscriptionUpMore")

            data object RecommendMore : Screen("home/main/recommendMore")

            data object RecentlyWatchMore : Screen("home/main/recentlyWatchMore")

            data object TrendShortsMore : Screen("home/main/trendShortsMore")

            data object TopicListDetail : DynamicScreen(
                baseRoute = "home/topicDetail",
                pathArgNames = listOf("topicId"),
                queryArgNames = listOf("channelId", "filterType"),
            )
        }

        data object ShortForm : Screen("home/shortform") {
            data object YouTube : DynamicScreen("youtube", listOf("jsonList"))
        }
    }

    abstract class Screen(
        baseRoute: String,
    ) {
        companion object {
            const val BASE_DEEPLINK_URL = "shortformplay://"
        }

        open val route = baseRoute
        open val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute"
    }

    abstract class DynamicScreen(
        private val baseRoute: String,
        val pathArgNames: List<String> = emptyList(),
        val queryArgNames: List<String> = emptyList(),
    ) : Screen(baseRoute) {
        override val route: String =

            buildString {
                append(baseRoute)
                if (pathArgNames.isNotEmpty()) {
                    append(
                        pathArgNames.joinToString(
                            separator = "",
                            prefix = "/",
                        ) { "{$it}" },
                    )
                }
                if (queryArgNames.isNotEmpty()) {
                    append(
                        queryArgNames.joinToString(
                            separator = "&",
                            prefix = "?",
                        ) { "$it={$it}" },
                    )
                }
            }

        val navArguments =

            pathArgNames.map { argName ->
                navArgument(argName) {
                    type = NavType.StringType
                }
            } +
                queryArgNames.map { argName ->

                    navArgument(argName) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                }

        fun createRoute(
            pathArgs: List<String> = emptyList(),
            queryArgs: Map<String, String?> = emptyMap(),
        ): String {
            require(pathArgs.size == pathArgNames.size) {
                "path argument count mismatch. required=${pathArgNames.size}, actual=${pathArgs.size}"
            }

            val path =
                buildString {
                    append(baseRoute)
                    if (pathArgs.isNotEmpty()) {
                        append(
                            pathArgs.joinToString(
                                separator = "",
                                prefix = "/",
                            ) { Uri.encode(it) },
                        )
                    }
                }

            val query =
                queryArgNames
                    .mapNotNull { key ->

                        val value = queryArgs[key]

                        if (value.isNullOrBlank()) {
                            null
                        } else {
                            "$key=${Uri.encode(value)}"
                        }
                    }.joinToString("&")

            return if (query.isBlank()) path else "$path?$query"
        }

        fun dynamicRoute(param: String) = "$baseRoute/$param"

        fun dynamicDeeplink(param: String) = "$BASE_DEEPLINK_URL/$baseRoute/$param"
    }
}
