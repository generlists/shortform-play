package com.sean.ratel.android.ui.splash

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.NetworkUtil
import com.sean.ratel.android.utils.TimeUtil.getCurrentDate
import com.sean.ratel.android.utils.UIUtil.pickTendShortsFromMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
@Suppress("ktlint:standard:property-naming")
class SplashViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        val gaLog: GALog,
        private val youTubeRepository: YouTubeRepository,
        private val settingRepository: SettingRepository,
    ) : ViewModel() {
        private val _shortformList =
            MutableStateFlow(Pair(MainShortFormList(), 7))
        val shortformList: StateFlow<Pair<MainShortFormList, Int>> = _shortformList

        private val _trendShortsList = MutableStateFlow(TrendsShortFormList())
        val trendsShortsList: StateFlow<TrendsShortFormList> = _trendShortsList

        private val _mainTrendShortsList = MutableStateFlow(emptyList<MainShortsModel>())
        val mainTrendShortsList: StateFlow<List<MainShortsModel>> = _mainTrendShortsList

        private val _mainDataComplete = MutableStateFlow(false)
        val mainDataComplete = _mainDataComplete

        private val _trendsShortsComplete = MutableStateFlow(false)
        val trendsShortsComplete = _trendsShortsComplete

        private val _retryCount = MutableStateFlow(0)

        suspend fun requestYouTubeVideos(
            requestType: RequestType,
            storage: FirebaseStorage,
            countryCode: String? = null,
            forceRefresh: Boolean = false,
        ) {
            if (!isNetWorkAvailable(storage.app.applicationContext)) return

            val currentDate = getCurrentDate()
            val startTime = System.currentTimeMillis()
            val downloadKey =
                if (requestType == RequestType.TODAY) {
                    String.format(UPLOAD_URL, currentDate, countryCode)
                } else {
                    String.format(DEFAULT_URL, countryCode)
                }
            youTubeRepository
                .requestYouTubeVideos(
                    requestType,
                    getDownloadUrl(storage.reference.child(downloadKey), countryCode).toString(),
                    countryCode,
                    forceRefresh,
                ).collect { response ->

                    _shortformList.value = Pair(response.shortformList, response.itemSize)

                    if (_shortformList.value.second > 0) {
                        _mainDataComplete.value = true
                    }

                    val speed = (System.currentTimeMillis() - startTime) / 1000
                    RLog.d(
                        TAG,
                        "${_shortformList.value.second} , $speed 초",
                    )
                }
        }

        suspend fun requestYouTubeTrendShorts(
            requestType: RequestType,
            storage: FirebaseStorage,
            countryCode: String? = null,
            forceRefresh: Boolean = false,
        ) {
            val currentDate = getCurrentDate()
            val startTime = System.currentTimeMillis()
            val downloadKey =
                if (requestType == RequestType.TODAY) {
                    String.format(TRENDS_SHORTS_UPLOAD_URL, currentDate, countryCode)
                } else {
                    TRENDS_SHORTS_DEFAULT_URL
                }

            youTubeRepository
                .requestYouTubeTrendShorts(
                    requestType,
                    getDownloadUrl(storage.reference.child(downloadKey), countryCode).toString(),
                    countryCode,
                    forceRefresh,
                ).collect { response ->
                    _trendShortsList.value = response.shortformList

                    _mainTrendShortsList.value =
                        pickTendShortsFromMap(_trendShortsList.value.event_list)

                    if (_trendShortsList.value.event_list.size > 0) {
                        _trendsShortsComplete.value = true
                    }

                    val speed = (System.currentTimeMillis() - startTime) / 1000
                    RLog.d(
                        TAG,
                        "trendShorts 크기 : ${_trendShortsList.value.event_list.size} , $speed 초",
                    )
                }
        }

        // Firebase Storage의 download URL을 가져오는 suspend 함수
        private suspend fun getDownloadUrl(
            ref: StorageReference,
            countryCode: String?,
            forceRefresh: Boolean = false,
        ): Uri =
            suspendCoroutine { continuation ->
                ref.downloadUrl
                    .addOnSuccessListener { url ->
                        continuation.resume(url)
                    }.addOnFailureListener { exception ->
                        if (_retryCount.value <= 3) {
                            viewModelScope.launch {
                                requestYouTubeVideos(
                                    RequestType.DEFAULT,
                                    FirebaseStorage.getInstance(),
                                    countryCode,
                                    forceRefresh,
                                )
                                viewModelScope.launch {
                                    requestYouTubeTrendShorts(
                                        RequestType.DEFAULT,
                                        FirebaseStorage.getInstance(),
                                        countryCode,
                                        forceRefresh,
                                    )
                                }
                            }
                        }
                        _retryCount.value += 1
                    }
            }

        fun isNetWorkAvailable(context: Context) = NetworkUtil.isNetworkAvailable(context)

        suspend fun getLocale(): String = settingRepository.getLocale()

        suspend fun setLocale(locale: String) {
            settingRepository.setLocale(locale)
        }

        fun exitApp() {
            navigator.finish()
        }

        fun sendGALog(
            screenName: String,
            eventName: String,
            actionName: String,
            parameter: Map<String, String>,
        ) {
            gaLog.sendEvent(
                screenName,
                eventName,
                actionName,
                parameter,
            )
        }

        companion object {
            const val TAG = "SplashViewModel"
            private const val UPLOAD_URL = "%s/shortform-play/shorts_main_list_%s.json"
            private const val DEFAULT_URL = "shorts_main_default_%s.json"

            private const val TRENDS_SHORTS_UPLOAD_URL = "%s/shortform-play/shorts_trailer_list_%s.json"
            private const val TRENDS_SHORTS_DEFAULT_URL = "shorts_trailer_default.json"
        }

        enum class RequestType {
            TODAY,
            DEFAULT,
        }
    }
