package com.sean.ratel.android.data.log

import com.sean.ratel.android.data.log.GAKeys.MAIN_SCREEN
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GAKeys.SETTING_SCREEN
import com.sean.ratel.android.data.log.GAKeys.SPLASH_SCREEN
import com.sean.ratel.android.data.log.GAKeys.TOPIC_DETAIL

object GASplashAnalytics {
    val SCREEN_NAME =
        mapOf<String, String>(
            MAIN_SCREEN to "main_screen",
            SPLASH_SCREEN to "splash_screen",
            SEARCH_SCREEN to "search_screen",
            TOPIC_DETAIL to "topic_screen",
            SETTING_SCREEN to "setting_screen",
        )

    object Event {
        const val SEARCH_VIEW = "view"
        const val SELECT_COUNTY_CLICK = "select_country_btn_click"
        const val SELECT_SEARCH_BTN_CLICK = "select_user_search_btn_click"
        const val SELECT_SEARCH_USER_SUGGEST_ITEM_CLICK = "select__search_user_suggest_item_click"
        const val SELECT_SEARCH_ITEM_CLICK = "select_user_search_suggest_item_click"
        const val SEARCH_MORE_VIEW = "search_more_view"
        const val SELECT_SEARCH_TAB_KEYWORD_CLICK = "select_search_tab_keyword_click"
        const val SELECT_SEARCH_TAB_DAILY_CLICK = "select_search_tab_daily_click"
        const val SELECT_SEARCH_DAILY_FILTER_BTN_CLICK = "select_search_daily_filter_btn_click"
        const val SELECT_SEARCH_DAILY_FILTER_APPLY_BTN_CLICK = "select_search_daily_filter_apply_btn_click"
        const val SELECT_SEARCH_DAILY_FILTER_RESET_BTN_CLICK = "select_search_daily_filter_reset_btn_click"
        const val SELECT_SEARCH_DAILY_FILTER_CATEGORY_SELECT = "select_search_daily_filter_category_btn_select"
        const val SELECT_SEARCH_DAILY_RESULT = "select_user_search_daily_result"
        const val SELECT_SEARCH_DAILY_ITEM_CLICK = "select_user_search_daily_item_click"
        const val SEARCH__DAILY_MORE_VIEW = "search_more_daily_view"
        const val SELECT_NOTIFICATION_CLICK = "select_notification_click"
        const val SELECT_MAIN_HOME_TOPIC_ITEM_CLICK = "select_main_topic_item_click"
        const val SELECT_TOPIC_DETAIL_CHANNEL_ITEM_CLICK = "select_topic_channel_item_click"
        const val SELECT_TOPIC_DETAIL_GROUP_ITEM_CLICK = "select_topic_group_item_click"
        const val SELECT_TOPIC_DETAIL_FILTER_ITEM_CLICK = "select_topic_filter_item_click"
        const val SELECT_TOPIC_DETAIL_SHARE_BTN_CLICK = "select_topic_filter_item_click"
        const val SELECT_AD_VIEW_POPUP_CLICK = "select_ad_promotion_popup_click"
        const val SELECT_AD_VIEW_POPUP_SHOW = "select_ad_promotion_popup_show"
        const val SELECT_MAIN_AD_PROMOTION_ITEM_CLICK = "select_ad_main_promotion_item_click"
        const val SELECT_SETTING_AD_PROMOTION_POPUP_CLICK = "select_ad_setting_promotion_popup_click"
        const val SELECT_SETTING_AD_PROMOTION_ITEM_CLICK = "select_ad_setting_promotion_item_click"
        const val SELECT_BTN_CAST_CLICK = "select_cast_btn_click"
        const val SELECT_ARROW_CAST_CLICK = "select_arrow_cast_btn_click"
        const val SELECT_PLAY_PAUSE_CAST_CLICK = "select_play_pause_cast_btn_click"
        const val SELECT_PREV_CAST_CLICK = "select_prev_cast_btn_click"
        const val SELECT_NEXT_CAST_CLICK = "select_next_cast_btn_click"
        const val SELECT_MUTE_CAST_CLICK = "select_mute_cast_btn_click"
        const val SELECT_SPEED_UP_CAST_CLICK = "select_speed_up_cast_btn_click"
        const val SELECT_SPEED_DOWN_CAST_CLICK = "select_speed_down_cast_btn_click"
    }

    object Action {
        const val VIEW = "view"
        const val CLICK = "click"
        const val SELECT = "select"
    }

    object Param {
        const val COUNTY_CODE = GAKeys.COUNTY_CODE
        const val VIDEO_ID = GAKeys.VIDEO_ID
        const val SEARCH_MORE_INDEX = GAKeys.SEARCH_MORE_INDEX
        const val SEARCH_TYPE = GAKeys.SEARCH_TYPE
        const val CATEGORY_NAME = GAKeys.CATEGORY_NAME
        const val NOTIFICATION_TYPE = GAKeys.NOTIFICATION_TYPE
        const val CAST_VIEW_TYPE = GAKeys.CAST_TYPE
        const val CAST_PLAYING_TYPE = GAKeys.PLAY_PAUSE

//        const val PAGE_INDEX = GAKeys.PAGE_INDEX
//        const val ISSUE_KEYWORD = GAKeys.ISSUE_KEYWORD
    }
}
