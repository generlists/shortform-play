package com.sean.ratel.android.data.log

/**
 * 로그인·내 필터·온보딩·보관함·시청 한도·플레이어 도구 이벤트(1.2.0).
 * 개인정보 처리방침 약속에 따라 이메일·이름·필터 키워드 원문·검색어는 보내지 않고, **개수와 종류만** 보낸다.
 * 보내는 방법은 기존과 같다: `gaLog.sendEvent(screenName, eventName, actionName, parameters)`.
 */
object GAAccountAnalytics {
    object Screen {
        const val MY = "my_screen"
        const val ONBOARDING = "onboarding_screen"
        const val FILTER_EDIT = "filter_edit_screen"
        const val HIDDEN_VIDEOS = "hidden_videos_screen"
        const val LIBRARY_FOLDER = "library_folder_screen"
        const val HOME = "main_screen"
        const val PLAYER = "player_screen"
    }

    object Event {
        // 로그인
        const val LOGIN_START = "login_start"
        const val LOGIN_SUCCESS = "login_success"
        const val LOGIN_FAIL = "login_fail"
        const val LOGIN_CANCEL = "login_cancel"
        const val LOGOUT = "logout"
        const val ACCOUNT_DELETE = "account_delete"

        // 온보딩
        const val ONBOARDING_VIEW = "onboarding_view"
        const val ONBOARDING_STEP = "onboarding_step"
        const val ONBOARDING_SKIP = "onboarding_skip"
        const val ONBOARDING_COMPLETE = "onboarding_complete"

        // 내 필터
        const val FILTER_SAVE = "filter_save"
        const val FILTER_EDIT_OPEN = "filter_edit_open"
        const val FILTER_OPTION_CHANGE = "filter_option_change"
        const val HIDDEN_VIDEOS_VIEW = "hidden_videos_view"

        // 홈 배너·필터 띠
        const val FILTER_BANNER_VIEW = "filter_banner_view"
        const val FILTER_BANNER_CREATE = "filter_banner_create"
        const val FILTER_BANNER_DISMISS = "filter_banner_dismiss"
        const val FILTER_BAR_MANAGE = "filter_bar_manage"

        // 관심없음
        const val NOT_INTERESTED_OPEN = "not_interested_open"
        const val NOT_INTERESTED_APPLY = "not_interested_apply"
        const val VIDEO_DISLIKE = "video_dislike"
        const val VIDEO_DISLIKE_CANCEL = "video_dislike_cancel"

        // 보관함
        const val LIBRARY_SAVE_SHEET_OPEN = "library_save_sheet_open"
        const val LIBRARY_SAVE = "library_save"
        const val LIBRARY_REMOVE = "library_remove"
        const val LIBRARY_FOLDER_CREATE = "library_folder_create"
        const val LIBRARY_FOLDER_RENAME = "library_folder_rename"
        const val LIBRARY_FOLDER_DELETE = "library_folder_delete"
        const val LIBRARY_FOLDER_OPEN = "library_folder_open"
        const val VIDEO_LIKE = "video_like"
        const val VIDEO_LIKE_CANCEL = "video_like_cancel"

        // 시청 한도
        const val LIMIT_SET = "limit_set"
        const val LIMIT_PROMPT_SHOW = "limit_prompt_show"
        const val LIMIT_CONTINUE = "limit_continue"
        const val LIMIT_STOP = "limit_stop"

        // 플레이어 도구
        const val PLAYER_SOUND_TOGGLE = "player_sound_toggle"
        const val PLAYER_SPEED_CHANGE = "player_speed_change"
        const val PLAYER_CAPTION_TOGGLE = "player_caption_toggle"
    }

    object Action {
        const val VIEW = "view"
        const val CLICK = "click"
        const val SELECT = "select"
        const val SUCCESS = "success"
        const val FAIL = "fail"
    }

    object Param {
        const val SOURCE = GAKeys.SOURCE
        const val REASON = GAKeys.REASON
        const val STEP = GAKeys.STEP
        const val OPTION = GAKeys.OPTION
        const val VALUE = GAKeys.VALUE
        const val MODE = GAKeys.MODE
        const val FOLDER_TYPE = GAKeys.FOLDER_TYPE
        const val TOPIC_COUNT = GAKeys.TOPIC_COUNT
        const val KEYWORD_COUNT = GAKeys.KEYWORD_COUNT
        const val CHANNEL_COUNT = GAKeys.CHANNEL_COUNT
        const val CHANNEL_BLOCKED = GAKeys.CHANNEL_BLOCKED
        const val RESULT = GAKeys.RESULT
        const val VIDEO_ID = GAKeys.VIDEO_ID
    }
}
