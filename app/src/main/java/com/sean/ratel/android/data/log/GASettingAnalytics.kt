package com.sean.ratel.android.data.log

object GASettingAnalytics {
    const val SCREEN_NAME = "setting_screen"

    object Event {
        const val SETTING_MAIN_COUNTY_ITEM_CLICK = "setting_main_country_item_click"
        const val SELECT_COUNTY_CLICK = "select_country_btn_click"
    }

    object Action {
        const val CLICK = "click"
        const val SELECT = "select"
    }

    object Param {
        const val COUNTY_CODE = GAKeys.COUNTY_CODE
//        const val VIDEO_ID = GAKeys.VIDEO_ID
//        const val PAGE_INDEX = GAKeys.PAGE_INDEX
//        const val ISSUE_KEYWORD = GAKeys.ISSUE_KEYWORD
    }
}
