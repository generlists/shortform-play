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
//        const val PAGE_INDEX = GAKeys.PAGE_INDEX
//        const val ISSUE_KEYWORD = GAKeys.ISSUE_KEYWORD
    }
}
