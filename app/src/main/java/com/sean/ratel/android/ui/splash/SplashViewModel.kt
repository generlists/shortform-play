package com.sean.ratel.android.ui.splash

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.NetworkUtil
import com.sean.ratel.android.utils.TimeUtil.getCurrentDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
@Suppress("ktlint:standard:property-naming")
class SplashViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        private val youTubeRepository: YouTubeRepository,
    ) : ViewModel() {
//        private val _shortsList = MutableStateFlow<MutableList<MainShortsModel>>(mutableListOf())
//        val shortsList: StateFlow<MutableList<MainShortsModel>> = _shortsList

        private val _shortformList =
            MutableStateFlow<Pair<MainShortFormList, Int>>(Pair(MainShortFormList(), 7))
        val shortformList: StateFlow<Pair<MainShortFormList, Int>> = _shortformList

        private val _mainDataComplete = MutableStateFlow(false)
        val mainDataComplete = _mainDataComplete

        private val _retryCount = MutableStateFlow(0)

        init {
            viewModelScope.launch {
                requestNewYouTubeVideos(RequestType.TODAY, FirebaseStorage.getInstance())
            }
        }

//        private suspend fun requestYouTubeVideos(
//            requestType: RequestType,
//            storage: FirebaseStorage,
//        ) {
//            if (!isNetWorkAvailable(storage.app.applicationContext)) return
//
//            val startTime = System.currentTimeMillis()
//            val countryCode = Locale.getDefault().country
//            val currentDate = getCurrentDate()
//            val downloadKey =
//                if (requestType == RequestType.TODAY) {
//                    String.format(UPLOAD_URL, currentDate, countryCode)
//                } else {
//                    DEFAULT_URL
//                }
//
//            youTubeRepository
//                .requestYouTubeVideos(
//                    requestType,
//                    getDownloadUrl(storage.reference.child(downloadKey)).toString(),
//                ).collect { response ->
//                    _shortsList.value = response.mainShortsList.toMutableList()
//
// //                    if (_shortsList.value.size > 0) {
// //                        mainLoadComplete()
// //                    }
//
//                    val speed = (System.currentTimeMillis() - startTime) / 1000
//                    RLog.d(
//                        TAG,
//                        "${_shortsList.value.size} , $speed 초",
//                    )
//                }
//        }

        private suspend fun requestNewYouTubeVideos(
            requestType: RequestType,
            storage: FirebaseStorage,
        ) {
            if (!isNetWorkAvailable(storage.app.applicationContext)) return
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
            val startTime = System.currentTimeMillis()
            val downloadKey =
                if (requestType == RequestType.TODAY) {
                    String.format(UPLOAD_URL, currentDate, countryCode)
                } else {
                    String.format(DEFAULT_URL, countryCode)
                }

            youTubeRepository
                .requestNewYouTubeVideos(
                    requestType,
                    getDownloadUrl(storage.reference.child(downloadKey)).toString(),
                ).collect { response ->

                    _shortformList.value = Pair(response.shortformList, response.itemSize)

                    if (_shortformList.value.second > 0) {
                        mainLoadComplete()
                    }

                    val speed = (System.currentTimeMillis() - startTime) / 1000
                    RLog.d(
                        TAG,
                        "${_shortformList.value.second} , $speed 초",
                    )
                }
        }

        // Firebase Storage의 download URL을 가져오는 suspend 함수
        private suspend fun getDownloadUrl(ref: StorageReference): Uri =
            suspendCoroutine { continuation ->
                ref.downloadUrl
                    .addOnSuccessListener { url ->
                        continuation.resume(url)
                    }.addOnFailureListener { exception ->
                        RLog.e(TAG, "$exception")
                        if (_retryCount.value <= 3) {
                            viewModelScope.launch {
                                requestNewYouTubeVideos(
                                    RequestType.DEFAULT,
                                    FirebaseStorage.getInstance(),
                                )
                            }
                        }
                        _retryCount.value += 1
                    }
            }

        fun isNetWorkAvailable(context: Context) = NetworkUtil.isNetworkAvailable(context)

        fun exitApp() {
            navigator.finish()
        }

        companion object {
            const val TAG = "SplashViewModel"
            private const val UPLOAD_URL = "%s/shortform-play/shorts_main_list_%s.json"
            private const val DEFAULT_URL = "shorts_main_default_%s.json"
        }

        private fun mainLoadComplete() {
            _mainDataComplete.value = true
        }

        enum class RequestType {
            TODAY,
            DEFAULT,
        }
    }
