package com.sean.ratel.android.data.common

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.ServerMaintainResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import so.smartlab.common.utils.log.RLog

object RemoteConfig {
    private val _complete = MutableStateFlow(false)
    val complete = _complete.asStateFlow()

    val MAIN_AD_KEY: String = "main_ad"
    val MAIN_SHORTFORM_KEY: String = "main_shortform_ad"
    val MAX_EDITOR_PICK_SIZE: String = "max_editor_pick_size"
    val MAX_RECENTLY_SIZE: String = "max_recently_size"
    val RANDOM_GA_END_SIZE: String = "random_ga_end_size"
    val MAX_RECENTLY_SAVE_SIZE: String = "max_recently_save_size"
    val BANNER_AD_VISIBILITY: String = "banner_ad_visibility"
    val MAX_RECOMMEND_SIZE = "max_recommend_size"
    val END_AD_POSITION: String = "end_ad_position"

    val RECENTLY_WATCH_ORDER: String = "recently_watch_order"
    val AD_BANNER_ORDER: String = "ad_banner_order"
    val TOPIC_LIST_ORDER: String = "topic_list_order"

    val TRENDS_SHORTS_ORDER: String = "trends_shorts_order"

    val POPULAR_ORDER: String = "popular_order"
    val EDITOR_PICK_ORDER: String = "editor_pick_order"
    val DAILY_RANKING_ORDER: String = "daily_ranking_order"
    val RECOMMEND_SHORTFORM_ORDER: String = "recommend_shortform_order"

    val SERVER_MAINTAIN: String = "server_maintain"
    val SERVER_MAINTAIN_START_TIME: String = "server_maintain_start"
    val SERVER_MAINTAIN_END_TIME: String = "server_maintain_end"

    private val map = hashMapOf<String, Any>()

    fun setRemoteConfig(remoteConfigKey: Map<String, FirebaseRemoteConfigValue>) {
        remoteConfigKey.map {
            val stringValue = it.value.asString()
            when {
                stringValue.toBooleanStrictOrNull() != null -> {
                    map.put(it.key, stringValue.toBoolean())
                }

                stringValue.toLongOrNull() != null -> {
                    map.put(it.key, stringValue.toLong())
                }

                stringValue.toDoubleOrNull() != null -> {
                    map.put(it.key, stringValue.toDouble())
                }

                stringValue.isNotEmpty() -> {
                    map.put(it.key, stringValue)
                }

                else -> {
                    throw IllegalArgumentException("Unsupported value type: ${it.key}")
                }
            }
        }
    }

    fun getRemoteConfigBooleanValue(key: String): Boolean {
        if (key == BANNER_AD_VISIBILITY) {
            return map[key] as? Boolean ?: true
        }
        return true
    }

    fun getRemoteConfigIntValue(key: String): Int {
        if (key != BANNER_AD_VISIBILITY) {
            return (map[key] as? Long)?.toInt() ?: -99999
        }
        return -99999
    }

    fun getRemoteConfigServerMaintainValue(
        context: Context,
        key: String,
    ): ServerMaintainResponse? {
        // 성공
        RLog.d("SPLASH", "key : ${map[key]}")

        if (map[key] is Long && map[key] == 1L) {
            val startTime =
                map[SERVER_MAINTAIN_START_TIME] as? Long ?: System.currentTimeMillis()
            val endTime = map[SERVER_MAINTAIN_END_TIME] as? Long ?: System.currentTimeMillis()

            return ServerMaintainResponse(
                maintain = true,
                code = -99999,
                title = context.getString(R.string.server_maintaing),
                message = context.getString(R.string.server_maintaing_message),
                startTime = startTime,
                endTime = endTime,
            )
        }
        return null
    }

    fun loadComplete(complete: Boolean) {
        _complete.value = complete
    }
}
