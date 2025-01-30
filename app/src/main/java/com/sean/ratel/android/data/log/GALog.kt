package com.sean.ratel.android.data.log

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.utils.TimeUtil
import javax.inject.Inject

class GALog
    @Inject
    constructor(
        val analytics: FirebaseAnalytics,
    ) {
        fun sendEvent(
            event: String,
            route: String? = null,
            viewType: ViewType? = null,
            channelId: String? = null,
            videoId: String? = null,
        ) {
            RLog.d(
                "hbungshin",
                "event : $event , route $route viewType $viewType channelId : $channelId , videoId : $videoId",
            )
            analytics.logEvent(event) {
                when (event) {
                    FirebaseAnalytics.Event.APP_OPEN ->
                        param(FirebaseAnalytics.Param.ITEM_ID, TimeUtil.getCurrentDate())

                    FirebaseAnalytics.Event.SCREEN_VIEW -> {
                        route?.let {
                            param(FirebaseAnalytics.Param.ITEM_ID, route)
                        }
                        viewType?.let {
                            param(FirebaseAnalytics.Param.ITEM_NAME, viewType.toString())
                        }
                        channelId?.let {
                            param(FirebaseAnalytics.Param.ITEM_LIST_ID, channelId)
                        }
                        videoId?.let {
                            param(FirebaseAnalytics.Param.ITEM_LIST_NAME, videoId)
                        }
                    }
                }
            }
        }
    }
