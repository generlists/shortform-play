package com.sean.ratel.android.data.repository

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.NoConnectivityException
import com.sean.ratel.android.data.api.ServerErrorException
import com.sean.ratel.android.data.api.youtube.FireBaseApi
import com.sean.ratel.android.data.api.youtube.YouTubeSearchApi
import com.sean.ratel.android.data.dto.DailySearchList
import com.sean.ratel.android.data.dto.MainShortFormLegacyList
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.MainShortsResponse
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.dto.SearchShortsSuggestResponse
import com.sean.ratel.android.data.dto.SessionResetRes
import com.sean.ratel.android.data.dto.TrendShortsResponse
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.local.pref.ShortsJsonPreference
import com.sean.ratel.android.data.local.pref.YouTubeSearchPreference
import com.sean.ratel.android.ui.splash.SplashViewModel
import com.sean.ratel.android.utils.NetworkHelper
import com.sean.ratel.android.utils.TimeUtil.getCurrentDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import so.smartlab.common.utils.log.RLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// repository 패턴에서 인터페이스가 없는듯?
@Singleton
class YouTubeRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val firebaseApi: FireBaseApi,
        private val youtubeSearchApi: YouTubeSearchApi,
        private val shortsJson: ShortsJsonPreference,
        private val youtubeSearchPrefence: YouTubeSearchPreference,
        private val networkHelper: NetworkHelper,
    ) {
        private var mainShortsVideosCache: MainShortsResponse? = null
        private var trendShortsVideosCache: TrendShortsResponse? = null

        fun requestYouTubeVideos(
            requestType: SplashViewModel.RequestType,
            url: String,
            countryCode: String?,
            forceRefresh: Boolean = false,
        ): Flow<MainShortsResponse> =
            flow {
                if (mainShortsVideosCache != null && !forceRefresh) {
                    mainShortsVideosCache?.let { emit(it) }
                } else {
                    val currentDate = getCurrentDate()

                    val key =
                        if (requestType == SplashViewModel.RequestType.PUBLISHED) {
                            String.format(JSON_SAVE_KEY, currentDate, countryCode)
                        } else {
                            String.format(DEFAULT_URL, countryCode)
                        }
                    val mainShortsListResponse = shortsJson.getShortsList(key)
                    RLog.d(
                        "SPLASH",
                        "[YouTubeRepository.kt] start requestType : $requestType , " +
                            "key : $key , currentDate : $currentDate , " +
                            "countryCode : $countryCode, " +
                            " size  : ${
                                mainShortsListResponse?.shortformList?.topFiveList?.fiveList?.size
                            }",
                    )
                    val response = firebaseApi.requestYouTubeVideos(url)
                    shortsJson.clearDataByKeyPattern(JSON_REMOVE_KEY)
                    shortsJson.setShortsList(key, response)
                    mainShortsVideosCache = response
                    emit(response)
                    mainShortsListResponse?.shortformList?.let {
                        mainShortsVideosCache = mainShortsListResponse
                        emit(mainShortsListResponse)
                    } ?: run {
                        val response = firebaseApi.requestYouTubeVideos(url)
                        shortsJson.clearDataByKeyPattern(JSON_REMOVE_KEY)
                        shortsJson.setShortsList(key, response)
                        mainShortsVideosCache = response
                        emit(response)
                    }
                }
            }

        fun requestYouTubeTrendShorts(
            requestType: SplashViewModel.RequestType,
            url: String,
            countryCode: String?,
            forceRefresh: Boolean = false,
        ): Flow<TrendShortsResponse> =
            flow {
                if (trendShortsVideosCache != null && !forceRefresh) {
                    trendShortsVideosCache?.let { emit(it) }
                } else {
                    val currentDate = getCurrentDate()

                    val key =
                        if (requestType == SplashViewModel.RequestType.PUBLISHED) {
                            String.format(JSON_TRENDS_SAVE_KEY, currentDate, countryCode)
                        } else {
                            String.format(DEFAULT_TRENDS_URL, countryCode)
                        }

                    val trendsShortsListResponse = shortsJson.getTrendShortsList(key)

                    if (trendsShortsListResponse.isValidTrends()) {
                        trendShortsVideosCache = trendsShortsListResponse
                        emit(trendsShortsListResponse!!)
                    } else {
                        val response =
                            try {
                                firebaseApi.requestYouTubeTrendShorts(url)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                RLog.e("SPLASH", "[YouTubeRepository.kt] trends request error : $e")
                                null
                            }

                        if (response.isValidTrends()) {
                            shortsJson.clearDataByKeyPattern(JSON_TRENDS_REMOVE_KEY)
                            shortsJson.setTrendsShortsList(key, response!!)
                            trendShortsVideosCache = response
                            emit(response)
                        } else {
                            // 당일 트레일러가 없거나 비어 있으면 디폴트(shorts_trailer_default.json)로 대체, 빈 응답은 캐시하지 않음
                            RLog.e("SPLASH", "[YouTubeRepository.kt] trends empty, fallback to default key : $key")
                            val defaultResponse = requestDefaultTrendShorts()
                            trendShortsVideosCache = defaultResponse
                            emit(defaultResponse)
                        }
                    }
                }
            }

        private suspend fun requestDefaultTrendShorts(): TrendShortsResponse {
            shortsJson.getTrendShortsList(DEFAULT_TRENDS_URL)?.let {
                if (it.isValidTrends()) return it
            }
            val defaultUrl = getStorageDownloadUrl(DEFAULT_TRENDS_URL)
            val response = firebaseApi.requestYouTubeTrendShorts(defaultUrl)
            if (response.isValidTrends()) {
                shortsJson.setTrendsShortsList(DEFAULT_TRENDS_URL, response)
            }
            return response
        }

        private suspend fun getStorageDownloadUrl(path: String): String =
            suspendCancellableCoroutine { continuation ->
                FirebaseStorage
                    .getInstance()
                    .reference
                    .child(path)
                    .downloadUrl
                    .addOnSuccessListener { continuation.resume(it.toString()) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }

        // Gson 은 Kotlin non-null 을 보장하지 않으므로 shortformList 가 null 일 수 있다
        private fun TrendShortsResponse?.isValidTrends(): Boolean {
            val shortformList: TrendsShortFormList? = this?.shortformList
            return !shortformList?.event_list.isNullOrEmpty()
        }

        suspend fun requestYoutubeSuggestList(
            query: String,
            hl: String = "ko",
        ): Flow<ApiResult<SearchShortsSuggestResponse>> =
            flow {
                emit(ApiResult.Loading)

                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    val response =
                        youtubeSearchApi.requestYouTubeShortsSearchSuggest(
                            query,
                            hl,
                        )
                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> context.resources.getString(R.string.api_bad_request_error)
                            in 500..599 -> context.resources.getString(R.string.api_server_error)
                            else -> context.resources.getString(R.string.api_unknown_error)
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        suspend fun requestYouTubeSearch(
            query: String,
            sessionId: String,
            position: Int = 0,
            countryCode: String,
            language: String,
            lastVideoId: String? = null,
        ): Flow<ApiResult<SearchShortsResponse>> =
            flow {
                RLog.d("hbungshin", "requestYouTubeSearch query : $query , countryCode :$countryCode , language : $language")
                emit(ApiResult.Loading)

                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    val response =
                        youtubeSearchApi.requestYouTubeShortsSearch(
                            query,
                            sessionId,
                            position,
                            countryCode,
                            language,
                            lastVideoId,
                        )
                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> {
                                context.resources.getString(R.string.api_bad_request_error)
                            }

                            in 500..599 -> {
                                if (e.code() == 503) {
                                    context.resources.getString(R.string.api_empty_error)
                                } else {
                                    context.resources.getString(R.string.api_server_error)
                                }
                            }

                            else -> {
                                context.resources.getString(R.string.api_unknown_error)
                            }
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        suspend fun requestYouTubeShortsSearchToEnd(
            videoId: String,
            region: String,
            hl: String,
        ): Flow<ApiResult<MainShortsModel>> =
            flow {
                RLog.d("hbungshin", "requestYouTubeShortsSearchToEnd videoId : $videoId , region :$region")
                emit(ApiResult.Loading)

                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    val response =
                        youtubeSearchApi.requestYouTubeShortsSearchToEnd(
                            videoId,
                            region,
                            hl,
                        )
                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> context.resources.getString(R.string.api_bad_request_error)
                            in 500..599 -> context.resources.getString(R.string.api_server_error)
                            else -> context.resources.getString(R.string.api_unknown_error)
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        suspend fun requestResetSession(sessionId: String): Flow<ApiResult<SessionResetRes>> =

            flow {
                RLog.d("OKKKKKKKK", "requestResetSession sessionId : $sessionId")

                emit(ApiResult.Loading)
                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    val response =
                        youtubeSearchApi.requestResetSession(
                            sessionId,
                        )

                    RLog.d("OKKKKKKKK", "!!response response : $response")
                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> context.resources.getString(R.string.api_bad_request_error)
                            in 500..599 -> context.resources.getString(R.string.api_server_error)
                            else -> context.resources.getString(R.string.api_unknown_error)
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    RLog.d("OKKKKKKKK", "!!response e : $e")
                    emit(ApiResult.Exception(e))
                }
            }

        suspend fun saveSearchResultModel(newItem: SearchResultModel) {
            youtubeSearchPrefence.saveSearchResultModel(newItem)
        }

        fun getSaveSuggestResultList(): Flow<List<SearchResultModel>> = youtubeSearchPrefence.getSaveSuggestResultList()

        suspend fun removeSuggestKeyWord(removeItem: SearchResultModel) {
            youtubeSearchPrefence.removeSuggestKeyWord(removeItem)
        }

        suspend fun clearDataByKeyPattern(pattern: String) {
            shortsJson.clearDataByKeyPattern(pattern)
        }

        suspend fun requestYouTubeCategory(
            region: String,
            language: String,
        ): Flow<ApiResult<List<YouTubeCategory>>> =
            flow {
                emit(ApiResult.Loading)

                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    RLog.d("SearchViewModel", "region : $region , language : $language")
                    val response =
                        youtubeSearchApi.requestYouTubeCategory(region, language)
                    RLog.d("SearchViewModel", "response : $response")
                    val categoryList =
                        response.map { (key, value) ->
                            YouTubeCategory(key, value)
                        }
                    emit(ApiResult.Success(categoryList))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> context.resources.getString(R.string.api_bad_request_error)
                            in 500..599 -> context.resources.getString(R.string.api_server_error)
                            else -> context.resources.getString(R.string.api_unknown_error)
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        suspend fun saveSearchCategoryList(
            categoryList: List<YouTubeCategory>,
            topicCategory: Map<String, String>,
        ) {
            val category = categoryList + topicCategory.map { YouTubeCategory(it.key, it.value) }
            youtubeSearchPrefence.saveCategoryList(category)
        }

        suspend fun getYouTubeCategoryList(): List<YouTubeCategory> = youtubeSearchPrefence.getYouTubeCategoryList()

        suspend fun removeCategory() {
            youtubeSearchPrefence.removeCategory()
        }

        suspend fun requestDailyShortsSearch(
            date: String,
            region: String,
        ): Flow<ApiResult<DailySearchList>> =
            flow {
                emit(ApiResult.Loading)

                if (!networkHelper.isNetworkConnected()) {
                    emit(ApiResult.Exception(NoConnectivityException(context.getString(R.string.api_no_network_error))))
                    return@flow
                }
                try {
                    // 레거시 대응을 위해 쌩 파싱
                    val requestDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    val response = youtubeSearchApi.requestDailyShortsSearch(date, region)
                    val jsonElement: JsonElement? = response.body()

                    val responseJsonObject = jsonElement?.asJsonObject

                    responseJsonObject?.let {
                        val startDateString = it.get("startDate")?.asString ?: "20241029"
                        val legacyEndDate = it.get("legacyEndDate")?.asString ?: "20241207"
                        val shortformList = it.get("shortformList")?.asJsonObject
                        val legacyEndDateString =
                            LocalDate.parse(legacyEndDate, DateTimeFormatter.ofPattern("yyyyMMdd"))
                        // 과거
                        if (requestDate.isBefore(legacyEndDateString)) {
                            emit(
                                ApiResult.Success(
                                    parseLegacyData(
                                        startDateString,
                                        legacyEndDate,
                                        shortformList,
                                    ),
                                ),
                            )
                        } else {
                            emit(
                                ApiResult.Success(
                                    parseNewData(
                                        startDateString,
                                        legacyEndDate,
                                        shortformList,
                                    ),
                                ),
                            )
                        }
                        RLog.d(
                            "YouTubeRepository",
                            "startDateString : $startDateString , legacyEndDate : $legacyEndDate",
                        )
                    }
                        ?: emit(ApiResult.Exception(ServerErrorException(context.resources.getString(R.string.api_unknown_error))))
                } catch (e: HttpException) {
                    val message =
                        when (e.code()) {
                            in 400..499 -> context.resources.getString(R.string.api_bad_request_error)
                            in 500..599 -> context.resources.getString(R.string.api_server_error)
                            else -> context.resources.getString(R.string.api_unknown_error)
                        }
                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        fun clearMemoryCache() {
            mainShortsVideosCache = null
            trendShortsVideosCache = null
        }

        private fun parseLegacyData(
            startDate: String,
            legacyEndDate: String,
            json: JsonObject?,
        ) = Gson().fromJson(json, MainShortFormLegacyList::class.java).copy(startDate, legacyEndDate)

        private fun parseNewData(
            startDate: String,
            legacyEndDate: String,
            json: JsonObject?,
        ) = Gson().fromJson(json, MainShortFormList::class.java).copy(startDate, legacyEndDate)

        companion object {
            private const val JSON_SAVE_KEY = "%s/shortform-play/shorts_main_list_%s.json"
            private const val JSON_REMOVE_KEY = "/shorts_main_list"
            private const val DEFAULT_URL = "shorts_main_default_%s.json"
            // const val DEFAULT_NEW_URL = "shorts_main_new_default_KR.json"

            private const val JSON_TRENDS_SAVE_KEY = "%s/shortform-play/shorts_trailer_list_%s.json"
            private const val JSON_TRENDS_REMOVE_KEY = "/shorts_trailer_list"
            private const val DEFAULT_TRENDS_URL = "shorts_trailer_default.json"
        }
    }
