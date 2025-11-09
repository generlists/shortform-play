package com.sean.ratel.android.ui.search

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.SearchActivity
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.UIUtil.getAppLocaleByStringResource
import com.sean.ratel.android.utils.UIUtil.localeFromCountryCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        val gaLog: GALog,
        val youtubeApiRepository: YouTubeRepository,
        val settingRespository: SettingRepository,
    ) : ViewModel() {
        private val _shortsSearchList = MutableStateFlow(emptyList<SearchResultModel>())

        val shortsSearchList: StateFlow<List<SearchResultModel>> = _shortsSearchList

        private val _hasNext = MutableStateFlow(true)

        val hasNext: StateFlow<Boolean> = _hasNext

        private val _searchSuggestList = MutableStateFlow(emptyList<String>())
        val searchSuggestList: StateFlow<List<String>> = _searchSuggestList

        private val _userSuggestList = MutableStateFlow(emptyList<SearchResultModel>())

        val userSuggestList: StateFlow<List<SearchResultModel>> = _userSuggestList

        private val _searchDataComplete = MutableStateFlow(false)
        val searchDataComplete = _searchDataComplete

        private val _searchSuggestComplete = MutableStateFlow(false)
        val searchSuggestComplete = _searchSuggestComplete

        private val _sessionId = MutableStateFlow(generateNewSession())
        val sessionId: StateFlow<String> = _sessionId

        private var lastSearchQuery: String = ""

        private val _moreIndex = MutableStateFlow<Int>(0)
        val moreIndex: StateFlow<Int> get() = _moreIndex

        private val _searchItemClicked = mutableStateOf<String?>(null)
        val searchItemClicked: MutableState<String?> = _searchItemClicked

        private val _currentLocale = MutableStateFlow<String>(Locale.KOREA.toString())
        val currentLocale: StateFlow<String> = _currentLocale

        private val _uiState = MutableStateFlow<UiState<SearchShortsResponse>>(UiState.Idle)
        val uiState: StateFlow<UiState<SearchShortsResponse>> = _uiState

        private fun generateNewSession(): String = UUID.randomUUID().toString()

        init {
            requestSaveSuggestResultList()
            requestLocale()
        }

        fun requestYouTubeSearchResult(
            query: String,
            position: Int = 0,
            countryCode: String,
            language: String,
            lastVideoId: String? = null,
        ) {
            var startTime = System.currentTimeMillis()

            viewModelScope.launch {
                if (query != lastSearchQuery) {
                    _sessionId.value = generateNewSession()
                    lastSearchQuery = query
                }
                youtubeApiRepository
                    .requestYouTubeSearch(
                        query,
                        _sessionId.value,
                        position,
                        countryCode,
                        language,
                        lastVideoId,
                    ).collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                startTime = System.currentTimeMillis()
                                _uiState.value = UiState.Loading
                            }

                            is ApiResult.Success -> {
                                _shortsSearchList.value = response.data.results
                                _hasNext.value = response.data.hasNext
                                _searchDataComplete.value = true

                                if (response.data.results.isNotEmpty()) {
                                    saveSearchResultModel(query, response.data.results[0])
                                }

                                _uiState.value = UiState.Success(response.data)
                                val endTime = System.currentTimeMillis() - startTime
                                RLog.d("SearchViewModel", "endTime : ${endTime / 1000} 초")
                            }

                            is ApiResult.Exception -> {
                                RLog.d("SearchViewModel", "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                            }

                            else -> Unit
                        }
                    }
            }
        }

        fun requestYoutubeSuggestList(
            query: String,
            hl: String,
        ) {
            viewModelScope.launch {
                youtubeApiRepository
                    .requestYoutubeSuggestList(query, hl)
                    .collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                _uiState.value = UiState.Loading
                            }

                            is ApiResult.Success -> {
                                _searchSuggestList.value = response.data.suggestions
                                if (response.data.suggestions.isNotEmpty()) {
                                    _searchSuggestComplete.value = true
                                }
                            }

                            is ApiResult.Exception -> {
                                RLog.d("SearchViewModel", "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                            }

                            else -> Unit
                        }
                    }
            }
        }

        fun saveSearchResultModel(
            keyWord: String,
            item: SearchResultModel,
        ) {
            viewModelScope.launch {
                RLog.d("hbungshin", "keyWord : $keyWord , item : ${item.copy(searchKeyword = keyWord)}")
                youtubeApiRepository.saveSearchResultModel(item.copy(searchKeyword = keyWord.split("+")[0].trim()))
            }
        }

        fun requestSaveSuggestResultList() {
            viewModelScope.launch {
                youtubeApiRepository.getSaveSuggestResultList().collect { data ->
                    _userSuggestList.value = data
                }
            }
        }

        fun removeSuggestKeyWord(removeItem: SearchResultModel) {
            viewModelScope.launch {
                youtubeApiRepository.removeSuggestKeyWord(removeItem)
            }
        }

        fun searchDataComplete(complete: Boolean) {
            _searchDataComplete.value = complete
        }

        fun searchSuggestComplete(complete: Boolean) {
            searchSuggestComplete.value = complete
        }

        fun maxMoreIndex(): Int = _shortsSearchList.value.size

        fun setMorEVent(moreIndex: Int): Int {
            _moreIndex.value = moreIndex
            return _moreIndex.value
        }

        fun moreContent(
            context: Context,
            index: Int,
            query: String,
            complete: (Boolean) -> Unit,
        ) {
            val currentLocale = _currentLocale.value
            val locale = localeFromCountryCode(currentLocale)
            val newKeyword = "$query + ${getAppLocaleByStringResource(context, currentLocale)}"
            val lastVideoId = _shortsSearchList.value.last().videoId

            RLog.d(
                "KKKKKKKKK",
                "index $index , lastVideoId : $lastVideoId , country : ${locale.country} , toLanguageTag : ${locale.toLanguageTag()}",
            )

            viewModelScope.launch {
                if (query != lastSearchQuery) {
                    _sessionId.value = generateNewSession()
                    lastSearchQuery = query
                }
                youtubeApiRepository
                    .requestYouTubeSearch(
                        newKeyword,
                        _sessionId.value,
                        index,
                        locale.country,
                        locale.toLanguageTag(),
                        lastVideoId,
                    ).collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                _uiState.value = UiState.Loading
                            }

                            is ApiResult.Success -> {
                                _hasNext.value = response.data.hasNext
                                if (hasNext.value) {
                                    val currentList = _shortsSearchList.value.toMutableList()
                                    currentList.addAll(response.data.results)
                                    _shortsSearchList.value = currentList // ← 새로운 객체로 교체해야 emit됨
                                    _uiState.value = UiState.Success(response.data)

                                    complete(true)
                                } else {
                                    complete(false)
                                }
                            }

                            is ApiResult.Exception -> {
                                RLog.d("SearchViewModel", "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                                complete(false)
                            }

                            else -> Unit
                        }
                    }
            }
        }

        fun requestLocale() {
            viewModelScope.launch {
                _currentLocale.value = settingRespository.getLocale()
            }
        }

        @Suppress("DEPRECATION")
        fun goEndContent(
            context: Context,
            route: String,
            viewType: ViewType,
            videoId: String,
        ) {
            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("target", "youtube_end")
                    putExtra("viewType", viewType.name)
                    putExtra("videoId", videoId)
                }
            (context as SearchActivity).startActivity(intent)
            context.overridePendingTransition(0, 0)
            requestResetSession(_sessionId.value)
            context.finish()
            navigator.navigateTo(route, false)
        }

        private fun requestResetSession(seesionId: String) {
            viewModelScope.launch {
                youtubeApiRepository.requestResetSession(seesionId).collect { response ->
                    RLog.d("OKKKKKKKK", "$response ,  _sessionId : $seesionId")
                    when (response) {
                        is ApiResult.Loading -> RLog.d("OKKKKKKKK", "Loading")
                        is ApiResult.Success -> RLog.d("OKKKKKKKK", "message ${response.data.message}")
                        is ApiResult.Exception -> RLog.e("OKKKKKKKK", "message : ${response.e.message}")
                        else -> Unit
                    }
                }
            }
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
    }
