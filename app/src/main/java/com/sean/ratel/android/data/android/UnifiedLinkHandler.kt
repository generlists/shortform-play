package com.sean.ratel.android.data.android

import android.content.Context
import android.content.Intent
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_PACKAGE_NAME
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.PhoneUtil.getAppVersionCode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UnifiedLinkHandler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private var onDeepLinkCallback: ((DeepLinkInfo) -> Unit)? = null

        fun setOnDeepLinkHandler(h: (DeepLinkInfo) -> Unit) {
            onDeepLinkCallback = h
        }

        fun handleDeepLink(deepLinkInfo: DeepLinkInfo) {
            RLog.d(
                "deepLink",
                "onDeepLinkCallback : $onDeepLinkCallback " +
                    "route : ${deepLinkInfo.deepLinkType}route, type ${deepLinkInfo.type}type , videoId : ${deepLinkInfo.extraParam1}",
            )
            onDeepLinkCallback?.invoke(deepLinkInfo)
        }

        fun goDeepLinKPage(
            context: Context,
            intent: Intent?,
        ) {
            val deepLinkUri = intent?.data
            val scheme = deepLinkUri?.scheme
            val version = deepLinkUri?.getQueryParameter("v")
            val versionCode = getAppVersionCode(context)
            RLog.d("deepLink", "deepLinkUri : $deepLinkUri, $version , versionCode : $versionCode")
            deepLinkUri?.let {
                if (version.isNullOrEmpty() || (version.toLong()) < versionCode) {
                    PhoneUtil.runAppStore(
                        context,
                        URL_GOOGLE_PLAY_APP(
                            URL_MY_PACKAGE_NAME,
                        ),
                    )
                    return
                }
            }

            RLog.d("deepLink", "goDeepLinKPage deepLinkUri $deepLinkUri , scheme : $scheme")

            val selection =
                when (scheme) {
                    "https" -> {
                        deepLinkUri.lastPathSegment
                    }

                    "shortformplay" -> {
                        deepLinkUri.host
                    }

                    else -> {
                        deepLinkUri?.lastPathSegment
                    }
                }
            RLog.d("deepLink", "deepLinkUri : $deepLinkUri ,selection : $selection")

            val deepLinkInfo: DeepLinkInfo =
                when (selection) {
                    HOME -> {
                        DeepLinkInfo(HOME, Destination.Home.Main.route, ViewType.DeepLinkVideo)
                    }

                    SHORTFORM -> {
                        DeepLinkInfo(SHORTFORM, Destination.Home.ShortForm.route, ViewType.DeepLinkVideo)
                    }

                    SETTING -> {
                        val page = deepLinkUri?.getQueryParameter("page")
                        RLog.d("deepLink", "page :$page")
                        if (page == APP_MANAGER) {
                            DeepLinkInfo(APP_MANAGER, Destination.AppManager.route, ViewType.DeepLinkVideo)
                        } else {
                            DeepLinkInfo(SETTING, Destination.Setting.route, ViewType.DeepLinkVideo)
                        }
                    }

                    YOUTUBE -> {
                        val videoId = deepLinkUri?.getQueryParameter("vid")
                        RLog.d("deepLink", "videoId : $videoId")
                        DeepLinkInfo(
                            YOUTUBE,
                            Destination.DeepLink.route,
                            ViewType.DeepLinkVideo,
                            videoId,
                        )
                    }

                    // https://shortform-play.ai/search?t=keyword&q=아이유&v=10070
                    //  https://shortform-play.ai/search?t=daily&d=20251020&c=0&v=10080
                    SEARCH -> {
                        val tab = deepLinkUri?.getQueryParameter("t")
                        val query = deepLinkUri?.getQueryParameter("q")
                        val date = deepLinkUri?.getQueryParameter("d")
                        val category = deepLinkUri?.getQueryParameter("c")
                        RLog.d("deeplink", "tab : $tab query : $query , date : $date , category : $category")
                        DeepLinkInfo(
                            SEARCH,
                            Destination.DeepLink.route,
                            ViewType.DeepLinkVideo,
                            query,
                            tab,
                            date,
                            category,
                        )
                    }

                    SHARE -> {
                        // val title = deepLinkUri.getQueryParameter("title")
                        val videoId = deepLinkUri?.getQueryParameter("vid")
                        DeepLinkInfo(
                            SHARE,
                            Destination.DeepLink.route,
                            ViewType.DeepLinkVideo,
                            videoId,
                        )
                    }

                    else -> {
                        DeepLinkInfo(HOME, Destination.Home.Main.route)
                    }
                }

            handleDeepLink(deepLinkInfo)
        }

        companion object {
            const val HOME = "main"
            const val SHORTFORM = "shortform"
            const val SETTING = "setting"
            const val SEARCH = "search"
            const val SHARE = "share"
            const val YOUTUBE = "youtube"
            const val APP_MANAGER = "appManager"
        }

        fun setReferer(
            context: Context,
            path: (String) -> Unit,
        ) {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(
                object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                val response = referrerClient.installReferrer
                                val referrer = response.installReferrer
                                RLog.d("deepLink", "referrer : $referrer")
                                if (referrer.contains("path=")) {
                                    path(referrer)
                                }
                            }

                            else -> {
                                RLog.d("OKJSP", "fail!! referer $path")
                                // 실패 처리
                            }
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        // 재시도 로직 필요 시
                    }
                },
            )
        }
    }

data class DeepLinkInfo(
    val deepLinkType: String,
    val route: String,
    val type: ViewType? = null,
    val extraParam1: String? = null,
    val extraParam2: String? = null,
    val extraParam3: String? = null,
    val extraParam4: String? = null,
)
