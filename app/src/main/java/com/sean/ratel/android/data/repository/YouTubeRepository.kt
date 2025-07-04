package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.api.youtube.FireBaseApi
import com.sean.ratel.android.data.dto.MainShortsResponse
import com.sean.ratel.android.data.local.pref.ShortsJsonPreference
import com.sean.ratel.android.ui.splash.SplashViewModel
import com.sean.ratel.android.utils.TimeUtil.getCurrentDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeRepository
    @Inject
    constructor(
        private val firebaseApi: FireBaseApi,
        private val shortsJson: ShortsJsonPreference,
    ) {
        private var mainShortsVideosCache: MainShortsResponse? = null

        suspend fun requestNewYouTubeVideos(
            requestType: SplashViewModel.RequestType,
            url: String,
            forceRefresh: Boolean = false,
        ): Flow<MainShortsResponse> =
            flow {
                if (mainShortsVideosCache != null && !forceRefresh) {
                    mainShortsVideosCache?.let { emit(it) }
                } else {
                    // 새로운 API 호출
                    val countryCode =
                        if (Locale
                                .getDefault()
                                .country
                                .toString()
                                .isNotEmpty()
                        ) {
                            Locale.getDefault().country
                        } else {
                            "KR"
                        }
                    val currentDate = getCurrentDate()

                    val key =
                        if (requestType == SplashViewModel.RequestType.TODAY) {
                            String.format(JSON_SAVE_KEY, currentDate, countryCode)
                        } else {
                            String.format(DEFAULT_URL, countryCode)
                        }
                    val mainShortsListResponse = shortsJson.getShortsList(key)

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

        companion object {
            private const val JSON_SAVE_KEY = "%s/shortform-play/shorts_main_list_%s.json"
            private const val JSON_REMOVE_KEY = "/shorts_main_list"
            private const val DEFAULT_URL = "shorts_main_default_%s.json"
        }
    }
