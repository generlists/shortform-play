package com.sean.ratel.android.data.api.youtube

import com.google.gson.JsonElement
import com.sean.ratel.android.data.api.EndPoint
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.PromotionResponse
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.dto.SearchShortsSuggestResponse
import com.sean.ratel.android.data.dto.SessionResetRes
import com.sean.ratel.android.data.dto.VerifyIAPRequest
import com.sean.ratel.android.data.dto.VerifyIAPResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
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
        @Query("hl") hl: String,
    ): MainShortsModel

    @GET(EndPoint.SEARCH_REMOVE_SESSION)
    suspend fun requestResetSession(
        @Query("sessionId") sessionId: String,
    ): SessionResetRes

    @GET(EndPoint.CATEGORY)
    suspend fun requestYouTubeCategory(
        @Query("region") region: String,
        @Query("hl") hl: String,
    ): Map<String, String>

    @GET(EndPoint.DAILY_SHORTS)
    suspend fun requestDailyShortsSearch(
        @Query("date") data: String,
        @Query("region") region: String,
    ): Response<JsonElement>

    @GET(EndPoint.PREMIUM_PROMOTION)
    suspend fun requestPremiumPromotion(
        @Query("appId") appId: String,
        @Query("locale") locale: String,
        @Query("timezoneId") timezoneId: String,
    ): PromotionResponse

    @POST(EndPoint.IAP_VERIFY)
    @Headers("Content-Type: application/json")
    suspend fun verifyIap(
        @Body body: VerifyIAPRequest,
    ): VerifyIAPResponse
}
