package com.sean.ratel.android.data.common

import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue

object RemoteConfig {
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
    val POPULAR_ORDER: String = "popular_order"
    val EDITOR_PICK_ORDER: String = "editor_pick_order"
    val DAILY_RANKING_ORDER: String = "daily_ranking_order"
    val RECOMMEND_SHORTFORM_ORDER: String = "recommend_shortform_order"

    private val map = hashMapOf<String, Any>()

    fun setRemoteConfig(remoteConfigKey: Map<String, FirebaseRemoteConfigValue>) {
        remoteConfigKey.map {
            when (it.key) {
                BANNER_AD_VISIBILITY -> map.put(it.key, it.value.asBoolean())
                else -> map.put(it.key, it.value.asLong())
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
            return (map[key] as? Long)?.toInt() ?: 1
        }
        return 0
    }
}
