package com.sean.ratel.android.data.api.youtube

import com.sean.ratel.android.data.api.EndPoint
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.dto.SearchShortsSuggestResponse
import com.sean.ratel.android.data.dto.SessionResetRes
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeSearchApi {
    @GET(EndPoint.SEARCH)
    suspend fun requestYouTubeShortsSearch(
        @Query("q") query: String,
        @Query("sessionId") sessionId: String,
        @Query("position") position: Int,
        @Query("country") country: String,
        @Query("language") language: String,
        @Query("lastVideoId") lastVideoId: String? = null,
    ): SearchShortsResponse

    @GET(EndPoint.SEARCH_SUGGEST)
    suspend fun requestYouTubeShortsSearchSuggest(
        @Query("q") query: String,
        @Query("hl") hl: String,
    ): SearchShortsSuggestResponse

    @GET(EndPoint.SEARCH_TO_END)
    suspend fun requestYouTubeShortsSearchToEnd(
        @Query("videoId") videoId: String,
        @Query("region") region: String,
    ): MainShortsModel

    @GET(EndPoint.SEARCH_REMOVE_SESSION)
    suspend fun requestResetSession(
        @Query("sessionId") sessionId: String,
    ): SessionResetRes
}
