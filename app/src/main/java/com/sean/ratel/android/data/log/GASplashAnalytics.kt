package com.sean.ratel.android.data.log

object GASplashAnalytics {
    const val SCREEN_NAME = "splash_screen"

    object Event {
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
