package com.sean.ratel.android.data.log

import com.sean.ratel.android.data.log.GAKeys.MAIN_SCREEN
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GAKeys.SPLASH_SCREEN

object GASplashAnalytics {
    val SCREEN_NAME =
        mapOf<String, String>(MAIN_SCREEN to "main_screen", SPLASH_SCREEN to "splash_screen", SEARCH_SCREEN to "search_screen")

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
//        const val PAGE_INDEX = GAKeys.PAGE_INDEX
//        const val ISSUE_KEYWORD = GAKeys.ISSUE_KEYWORD
    }
}
