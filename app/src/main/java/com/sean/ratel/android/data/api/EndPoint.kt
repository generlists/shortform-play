package com.sean.ratel.android.data.api

object EndPoint {
    const val SEARCH = "shortformplay/search"
    const val SEARCH_REMOVE_SESSION = "shortformplay/search/session-reset"
    const val SEARCH_TO_END = "shortformplay/search/end"
    const val SEARCH_SUGGEST = "shortformplay/search/suggest"
    const val AUTH_HASH = "shortformplay/attestation/request-hash"
    const val AUTH_INTEGRITY = "shortformplay/auth/integrity"
    const val CATEGORY = "shortformplay/search/category"
    const val DAILY_SHORTS = "shortformplay/search/daily"
    const val PREMIUM_PROMOTION = "shortformplay/promotion"
    const val IAP_VERIFY = "iap/verify"

    // 로그인·내 필터 (서버 shortformplay_v2) — 로그인 토큰은 X-Firebase-Token 헤더
    const val V2_PATH = "shortformplay/v2/"
    const val V2_LOGIN = "shortformplay/v2/auth/login"
    const val V2_ME = "shortformplay/v2/me"
    const val V2_FILTERS = "shortformplay/v2/me/filters"
    const val V2_LIBRARY_SYNC = "shortformplay/v2/me/library/sync"
}
