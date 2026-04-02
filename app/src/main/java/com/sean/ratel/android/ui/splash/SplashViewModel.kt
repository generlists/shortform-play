package com.sean.ratel.android.ui.splash

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.android.permission.PermissionManager
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.ApiResult.Loading.safeApiCall
import com.sean.ratel.android.data.common.IntegrityManager
import com.sean.ratel.android.data.dto.IntegrityExchangeReq
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.local.pref.AuthTokenPreference
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.AuthRepository
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.NetworkUtil
import com.sean.ratel.android.utils.TimeUtil.getCurrentDate
import com.sean.ratel.android.utils.UIUtil.pickTendShortsFromMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
@Suppress("ktlint:standard:property-naming")
class SplashViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        val navigator: Navigator,
        val gaLog: GALog,
        val permissionManager: PermissionManager,
        private val youTubeRepository: YouTubeRepository,
        private val settingRepository: SettingRepository,
        private val autoRepository: AuthRepository,
        private val prefs: AuthTokenPreference,
        private val integrityManager: IntegrityManager,
    ) : ViewModel() {
        private val _shortformList =
            MutableStateFlow(Pair(MainShortFormList(), 0))
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

        private val _authCheck = MutableStateFlow<Int?>(null)
        val authCheck = _authCheck

        private val _hasLoadedOnce = MutableStateFlow(false)
        val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

        init {
            viewModelScope.launch {
                prefs.updateTokenCache()
                val token = prefs.getAccessToken()
                RLog.d("SPLASH", "init splash $token")
                RLog.d("SPLASH", "isExpired ${autoRepository.isExpired(token)}")

                if (token == null || autoRepository.isExpired(token)) {
                    if (isNetWorkAvailable(context)) {
                        try {
                            val hash = autoRepository.getRequestHash()

                            when (val result = integrityManager.requestIntegrityToken(hash)) {
                                is IntegrityManager.IntegrityResult.Success -> {
                                    // 정상 처리
                                    RLog.d("SPLASH", "Integrity token: ${result.token}")
                                    val authResult =
                                        safeApiCall {
                                            autoRepository.exchange(
                                                IntegrityExchangeReq(
                                                    context.packageName,
                                                    result.token,
                                                    hash,
                                                ),
                                            )
                                        }

                                    when (authResult) {
                                        is ApiResult.Success -> {
                                            val data = authResult.data
                                            val accessToken = data.access_token
                                            val expiresIn = data.expires_in ?: (24 * 3600L)
                                            accessToken?.let {
                                                prefs.saveAccessToken(accessToken, expiresIn)
                                                prefs.updateTokenCache()
                                            }
                                            RLog.d(
                                                "SPLASH",
                                                "Access Token 갱신 성공: expires in $expiresIn 초",
                                            )
                                            _authCheck.value = 0
                                        }

                                        is ApiResult.Error -> {
                                            RLog.e(
                                                "SPLASH",
                                                "응답 오류(${authResult.code}): ${authResult.message}",
                                            )
                                            Toast
                                                .makeText(
                                                    context,
                                                    "권한 응답 오류(${authResult.code}): ${authResult.message}",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        }

                                        is ApiResult.Exception -> {
                                            RLog.e(
                                                "SPLASH",
                                                "기타 네트워크 예외: ${authResult.e.localizedMessage}",
                                            )
                                            Toast
                                                .makeText(
                                                    context,
                                                    "권한 서버 예외:${authResult.e.localizedMessage}",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                        }

                                        else -> {
                                            Unit
                                        }
                                    }
                                }

                                is IntegrityManager.IntegrityResult.Failure -> {
                                    RLog.e("SPLASH", "Integrity failed: ${result.errorCode}")
                                    _authCheck.value = result.errorCode
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    _authCheck.value = 0
                }
            }
        }

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
                    RLog.d(
                        "SPLASH",
                        "countryCode : $countryCode  $shortformList" +
                            " itemSize : ${response.shortformList.shortformVideoList.videoLikeList.likeList.size}",
                    )

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
            RLog.d(TAG, "locale: requestYouTubeTrendShorts")
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
                            }
                            viewModelScope.launch {
                                requestYouTubeTrendShorts(
                                    RequestType.DEFAULT,
                                    FirebaseStorage.getInstance(),
                                    countryCode,
                                    forceRefresh,
                                )
                            }
                        }
                        _retryCount.value += 1
                    }
            }

        fun isNetWorkAvailable(context: Context) = NetworkUtil.isNetworkAvailable(context)

        @OptIn(FlowPreview::class)
        val locale: StateFlow<String?> =
            settingRepository
                .getLocale()
                .onStart {
                    _hasLoadedOnce.value = true
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )

        suspend fun setLocale(locale: String) {
            settingRepository.setLocale(locale)
        }

        fun setAuthCheck(check: Int) {
            _authCheck.value = check
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
