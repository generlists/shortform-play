package com.sean.ratel.android.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.R
import com.sean.ratel.android.SearchActivity
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.RANDOM_GA_END_SIZE
import com.sean.ratel.android.data.dto.DailySearchList
import com.sean.ratel.android.data.dto.MainShortFormLegacyList
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.log.GASplashAnalytics.Event.SELECT_SEARCH_DAILY_ITEM_CLICK
import com.sean.ratel.android.data.log.GASplashAnalytics.Event.SELECT_SEARCH_ITEM_CLICK
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.UIUtil.getAppLocaleByStringResource
import com.sean.ratel.android.utils.UIUtil.localeFromCountryCode
import com.sean.ratel.android.utils.UIUtil.toStringMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val navigator: Navigator,
        val gaLog: GALog,
        val youtubeApiRepository: YouTubeRepository,
        val settingRespository: SettingRepository,
    ) : ViewModel() {
        private val _shortsSearchList = MutableStateFlow(emptyList<SearchResultModel>())

        val shortsSearchList: StateFlow<List<SearchResultModel>> = _shortsSearchList

        private val _dailySearchShortformList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val dailySearchShortformList: StateFlow<List<MainShortsModel>> = _dailySearchShortformList

        private val _dailyCurrentSearchShortformList =
            MutableStateFlow<List<MainShortsModel>>(emptyList())
        val dailyCurrentSearchShortformList: StateFlow<List<MainShortsModel>> =
            _dailyCurrentSearchShortformList

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

        private val _sessionId = MutableStateFlow("")
        val sessionId: StateFlow<String> = _sessionId

        private var lastSearchQuery: String = ""

        private val _moreIndex = MutableStateFlow<Int>(0)
        val moreIndex: StateFlow<Int> get() = _moreIndex

        private val _currentLocale = MutableStateFlow<String>(Locale.KOREA.toString())
        val currentLocale: StateFlow<String> = _currentLocale

        private val _uiState = MutableStateFlow<UiState<SearchShortsResponse>>(UiState.Idle)
        val uiState: StateFlow<UiState<SearchShortsResponse>> = _uiState

        private val _dailyUiState = MutableStateFlow<UiState<List<MainShortsModel>>>(UiState.Idle)
        val dailyUiState: StateFlow<UiState<List<MainShortsModel>>> = _dailyUiState

        private fun generateNewSession(): String = UUID.randomUUID().toString()

        private val _searchRetry = MutableStateFlow(false)
        val searchRetry: StateFlow<Boolean> = _searchRetry

        private val _searchLoading = MutableStateFlow(false)
        val searchLoading: StateFlow<Boolean> = _searchLoading

        private val _isSuggestLoading = MutableStateFlow(true)
        val isSuggestLoading: StateFlow<Boolean> = _isSuggestLoading

        private val _deepLinkTab = MutableStateFlow<String?>(null)
        val deepLinkTab: StateFlow<String?> = _deepLinkTab

        private val _deepLinkQuery = MutableStateFlow<String?>(null)
        val deepLinkQuery: StateFlow<String?> = _deepLinkQuery

        private val _deepLinkDate = MutableStateFlow<String?>(null)
        val deepLinkDate: StateFlow<String?> = _deepLinkDate

        private val _deepLinkCategory = MutableStateFlow<String?>(null)
        val deepLinkCategory: StateFlow<String?> = _deepLinkCategory

        // 선택된 날짜 (yyyy-MM-dd 같은 포맷으로 관리해도 좋고, millis로 관리해도 되고)
        private val _selectedDate = MutableStateFlow<String?>(null)
        val selectedDate: StateFlow<String?> = _selectedDate

        private val _selectedCategory = MutableStateFlow<YouTubeCategory>(getInitCategory())
        val selectedCategory: StateFlow<YouTubeCategory> = _selectedCategory

        private val _youtubeCategory = MutableStateFlow<List<YouTubeCategory>>(emptyList())
        val youtubeCategory: StateFlow<List<YouTubeCategory>> = _youtubeCategory

        private val _topicCategory = MutableStateFlow<Map<String, String>>(emptyMap())
        val topicCategory: StateFlow<Map<String, String>> = _topicCategory

        fun setTopicCategory(topic: Bundle?) {
            topic?.let {
                _topicCategory.value = it.toStringMap()
            }
        }

        init {
            requestSaveSuggestResultList()
            requestLocale()
            requestYouTubeCategory()
        }

        fun requestYouTubeSearchResult(
            context: Context,
            query: String,
            position: Int = 0,
            countryCode: String,
            language: String,
            lastVideoId: String? = null,
        ) {
            var startTime = System.currentTimeMillis()

            viewModelScope.launch {
                if (query != lastSearchQuery) {
                    // 기존 세션 제거
                    requestResetSession(_sessionId.value)

                    _sessionId.value = generateNewSession()
                    lastSearchQuery = query
                        .split("+")
                        .firstOrNull()
                        ?.trim()
                        ?: query.trim()
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
                                _searchLoading.value = true
                            }

                            is ApiResult.Success -> {
                                _shortsSearchList.value = response.data.results
                                _hasNext.value = response.data.hasNext
                                _searchDataComplete.value = true
                                _searchLoading.value = false

                                if (response.data.results.isNotEmpty()) {
                                    saveSearchResultModel(query, response.data.results[0])
                                    if (response.data.cache) {
                                        // 너무 빨라서 딜레이를 줘야 recomposition 이 일어남
                                        delay(500)
                                    }
                                    _uiState.value = UiState.Success(response.data)
                                } else {
                                    _uiState.value =
                                        UiState.Error(context.getString(R.string.api_empty_error))
                                }
                                val endTime = System.currentTimeMillis() - startTime
                                RLog.d("SearchViewModel", "endTime : ${endTime / 1000} 초")
                            }

                            is ApiResult.Exception -> {
                                RLog.d(TAG, "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                                _searchLoading.value = false
                            }

                            else -> {
                                Unit
                            }
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
                                RLog.d(TAG, "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                            }

                            else -> {
                                Unit
                            }
                        }
                    }
            }
        }

        fun saveSearchResultModel(
            keyWord: String,
            item: SearchResultModel,
        ) {
            viewModelScope.launch {
                RLog.d(TAG, "keyWord : $keyWord , item : ${item.copy(searchKeyword = keyWord)}")
                youtubeApiRepository.saveSearchResultModel(item.copy(searchKeyword = keyWord.split("+")[0].trim()))
            }
        }

        fun requestSaveSuggestResultList() {
            viewModelScope.launch {
                youtubeApiRepository
                    .getSaveSuggestResultList()
                    .onStart { _isSuggestLoading.value = true }
                    .onEach { data ->
                        _userSuggestList.value = data
                    }.onCompletion { _isSuggestLoading.value = false }
                    .catch { _isSuggestLoading.value = false }
                    .collect {
                        _userSuggestList.value = it
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

        fun maxMoreIndex(searchType: SearchType): Int =
            if (searchType == SearchType.VideoSearch) _shortsSearchList.value.size else _dailyCurrentSearchShortformList.value.size

        fun setMorEVent(moreIndex: Int): Int {
            _moreIndex.value = moreIndex
            return _moreIndex.value
        }

        fun setDeepLinkQuery(query: String?) {
            _deepLinkQuery.value = query
        }

        fun setDeepLinkTab(
            tab: String?,
            date: String?,
            categoryName: String?,
        ) {
            _deepLinkTab.value = tab
            _deepLinkDate.value = date
            _deepLinkCategory.value = categoryName
        }

        fun moreContent(
            context: Context,
            index: Int,
            query: String,
            complete: (Boolean) -> Unit,
        ) {
            val currentLocale = _currentLocale.value
            val locale = localeFromCountryCode(currentLocale)
            val newKeyword = "$query + ${
                getAppLocaleByStringResource(
                    context,
                    currentLocale,
                    R.string.main_shorts_post_fix,
                )
            }"
            val lastVideoId = _shortsSearchList.value.last().videoId

            RLog.d(
                TAG,
                "index $index , lastVideoId : $lastVideoId query : $lastSearchQuery : $lastSearchQuery , _sessionId : ${_sessionId.value}",
            )

            viewModelScope.launch {
                if (query != lastSearchQuery) {
                    // 기존 세션 제거
                    requestResetSession(_sessionId.value)

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
                                RLog.d(TAG, "message : ${response.e.message}")
                                _uiState.value =
                                    response.e.message?.let { UiState.Error(it) } ?: UiState.Error("")
                                complete(false)
                            }

                            else -> {
                                Unit
                            }
                        }
                    }
            }
        }

        fun requestLocale() {
            viewModelScope.launch {
                _currentLocale.value = settingRespository.getLocale().first() ?: "KR"
                _selectedCategory.value = getInitCategory()
                RLog.d("SearchViewModel", "_currentLocale : ${_selectedCategory.value}")
            }
        }

        @Suppress("DEPRECATION")
        fun goEndContent(
            context: Context,
            route: String,
            viewType: ViewType,
            videoId: String,
            selectedIndex: Int? = null,
            categoryKey: String? = null,
        ) {
            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("target", "youtube_end")
                    putExtra("viewType", viewType.name)
                    putExtra("videoId", videoId)
                    putExtra("selectedIndex", selectedIndex)
                    putExtra("categoryKey", categoryKey)
                }
            (context as SearchActivity).startActivity(intent)
            context.overridePendingTransition(0, 0)
            requestResetSession(_sessionId.value)
            context.finish()
            navigator.navigateTo(route, false)
        }

        fun setSearchRetry(searchRetry: Boolean) {
            _searchRetry.value = searchRetry
        }

        fun requestResetSession(sessionId: String) {
            if (sessionId.isEmpty()) return
            viewModelScope.launch {
                youtubeApiRepository.requestResetSession(sessionId).collect { response ->
                    RLog.d(TAG, "$response ,  _sessionId : $sessionId")
                    when (response) {
                        is ApiResult.Loading -> RLog.d(TAG, "Loading")
                        is ApiResult.Success -> RLog.d(TAG, "message ${response.data.message}")
                        is ApiResult.Exception -> RLog.e(TAG, "message : ${response.e.message}")
                        else -> Unit
                    }
                }
            }
        }

        fun requestYouTubeCategory() {
            viewModelScope.launch {
                val countryCode = settingRespository.getLocale().first() ?: "KR"

                val categoryList = youtubeApiRepository.getYouTubeCategoryList()
                RLog.d("OKJJJJJJJ", "countryCode : $countryCode  categoryList : $categoryList")

                if (categoryList.isNotEmpty() && categoryList.first().categoryKey != null) {
                    _youtubeCategory.value = categoryList
                    return@launch
                }
                youtubeApiRepository.requestYouTubeCategory(countryCode).collect { response ->

                    when (response) {
                        is ApiResult.Loading -> {
                            RLog.d(TAG, "Loading")
                        }

                        is ApiResult.Success -> {
                            RLog.d("OKJJJJJJJ", "category ${response.data} topicCategory : ${_topicCategory.value}")

                            youtubeApiRepository.saveSearchCategoryList(response.data, _topicCategory.value)

                            _youtubeCategory.value = youtubeApiRepository.getYouTubeCategoryList()

                            RLog.d(TAG, "modify category ${response.data}")
                        }

                        is ApiResult.Exception -> {
                            RLog.e(TAG, "message : ${response.e.message}")
                        }

                        else -> {
                            Unit
                        }
                    }
                }
            }
        }

        fun requestDailyShortsSearch(
            context: Context,
            date: String?,
            region: String,
        ) {
            var startTime = System.currentTimeMillis()

            viewModelScope.launch {
                youtubeApiRepository
                    .requestDailyShortsSearch(
                        date ?: "",
                        region,
                    ).collect { response ->
                        when (response) {
                            is ApiResult.Loading -> {
                                startTime = System.currentTimeMillis()
                                _dailyUiState.value = UiState.Loading
                                _searchLoading.value = true
                            }

                            is ApiResult.Success -> {
                                _searchDataComplete.value = true
                                _searchLoading.value = false
                                val requestDate =
                                    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
                                val legacyEndDateString =
                                    LocalDate.parse(
                                        response.data.legacyEndDate,
                                        DateTimeFormatter.ofPattern("yyyyMMdd"),
                                    )

                                if (response.data.startDate.isNotEmpty()) {
                                    RLog.d("SearchViewModel", "data : ${response.data}")

                                    if (requestDate.isBefore(legacyEndDateString)) {
                                        setDailySearchDataLegacy(response.data)
                                    } else {
                                        setDailySearchData(response.data)
                                    }
                                    _dailyUiState.value =
                                        UiState.Success(_dailyCurrentSearchShortformList.value)
                                } else {
                                    _dailyUiState.value =
                                        UiState.Error(context.getString(R.string.api_empty_error))
                                }
                                setDeepLinkTab(null, null, null)
                                val endTime = System.currentTimeMillis() - startTime
                                RLog.d("SearchViewModel", "endTime : ${endTime / 1000} 초")
                            }

                            is ApiResult.Exception -> {
                                RLog.d(TAG, "message : ${response.e.message}")
                                _dailyUiState.value =
                                    response.e.message?.let { UiState.Error(it) }
                                        ?: UiState.Error("")
                                _searchLoading.value = false
                                setDeepLinkTab(null, null, null)
                            }

                            else -> {
                                Unit
                            }
                        }
                    }
            }
        }

        fun setDailySearchData(shortformList: DailySearchList) {
            val dailyShortFormList =
                (shortformList as MainShortFormList)
                    .topFiveList.fiveList.values
                    .flatten() +

                    shortformList.shortformVideoList.videoSearchList.searchList +
                    shortformList.shortformVideoList.videoLikeList.likeList +
                    shortformList.shortformVideoList.videoCommentList.commentList +

                    shortformList.channelVideoList.channelSearchList.searchList +
                    shortformList.channelVideoList.channelLikeList.likeList +

                    shortformList.editorPickList.pickList +
                    shortformList.channelSubscriptionList.subscriptionList +
                    shortformList.channelSubscriptionUpList.subscriptionUpList +
                    shortformList.shortformRecommendList.recommendList +
                    shortformList.trendShortsList.event_list.values
                        .flatten()
                        .distinctBy { it.shortsVideoModel?.videoId }

            RLog.d(
                "SearchViewModel",
                "_selectedCategory category name : ${_selectedCategory.value.categoryName}" +
                    "  , categoryName : ${_selectedCategory.value.categoryName} " +
                    "dailyShortFormList size ${dailyShortFormList.size}",
            )
            val dailySearchShorts =
                if (_selectedCategory.value.categoryKey == "0") {
                    dailyShortFormList
                        .toMutableList()
                        .shuffled()
                } else {
                    dailyShortFormList
                        .filter { it.shortsVideoModel?.category == _selectedCategory.value.categoryKey }
                        .toMutableList()
                        .shuffled()
                }

            RLog.d("SearchViewModel", "dailySearchShorts  size ${dailySearchShorts.size}")

            val list =
                dailySearchShorts
                    .filter { it.shortsVideoModel != null }
                    .distinctBy { it.shortsVideoModel!!.videoId }
                    .chunked(INIT_MAX_SIZE)
                    .mapIndexed { i, chunk -> i to chunk }
                    .toMap()

            _dailySearchShortformList.value = dailySearchShorts
            val finalShuffledChunk = list[0]

            finalShuffledChunk?.let {
                val updatedList = _dailyCurrentSearchShortformList.value + it
                _dailyCurrentSearchShortformList.value = updatedList
            }

            RLog.d(
                "SearchViewModel",
                "_dailySearchShortformList  size ${_dailyCurrentSearchShortformList.value.size}",
            )
        }

        fun setDailySearchDataLegacy(shortformList: DailySearchList) {
            val dailyShortFormList =
                (shortformList as MainShortFormLegacyList).shortformRecommendList.recommendList.distinctBy { it.shortsVideoModel?.videoId }

            RLog.d(
                "SearchViewModel",
                "_selectedCategory category name : ${_selectedCategory.value.categoryName} " +
                    " , categoryName : ${_selectedCategory.value.categoryName} " +
                    "dailyShortFormList size ${dailyShortFormList.size}",
            )
            val dailySearchShorts =
                if (_selectedCategory.value.categoryKey == "0") {
                    dailyShortFormList
                        .toMutableList()
                        .shuffled()
                } else {
                    dailyShortFormList
                        .filter { it.shortsVideoModel?.category == _selectedCategory.value.categoryKey }
                        .toMutableList()
                        .shuffled()
                }

            RLog.d("SearchViewModel", "dailySearchShorts  size ${dailySearchShorts.size}")

            val list =
                dailySearchShorts
                    .filter { it.shortsVideoModel != null }
                    .distinctBy { it.shortsVideoModel!!.videoId }
                    .chunked(INIT_MAX_SIZE)
                    .mapIndexed { i, chunk -> i to chunk }
                    .toMap()

            _dailySearchShortformList.value = dailySearchShorts
            val finalShuffledChunk = list[0]

            finalShuffledChunk?.let {
                val updatedList = _dailyCurrentSearchShortformList.value + it
                _dailyCurrentSearchShortformList.value = updatedList
            }

            RLog.d(
                "SearchViewModel",
                "_dailySearchShortformList  size ${_dailyCurrentSearchShortformList.value.size}",
            )
        }

        fun dailyMoreContent(
            index: Int,
            complete: (Boolean) -> Unit,
        ) {
            RLog.d("KKKKKKKKK", "dailyMoreContent size : ${_dailySearchShortformList.value.size}")
            complete(true)
            val list =
                _dailySearchShortformList.value
                    .filter { it.shortsVideoModel != null }
                    .distinctBy { it.shortsVideoModel!!.videoId }
                    .chunked(INIT_MAX_SIZE)
                    .mapIndexed { i, chunk -> i to chunk }
                    .toMap()

            val finalShuffledChunk = list[index]

            finalShuffledChunk?.let {
                val updatedList = _dailyCurrentSearchShortformList.value + it
                _dailyCurrentSearchShortformList.value = updatedList
            }

            complete(false)
        }

        fun sendGALog(
            screenName: String,
            eventName: String,
            actionName: String,
            parameter: Map<String, String>,
        ) {
            if (eventName == SELECT_SEARCH_ITEM_CLICK || eventName == SELECT_SEARCH_DAILY_ITEM_CLICK) {
                if (Random.nextInt(RemoteConfig.getRemoteConfigIntValue(RANDOM_GA_END_SIZE)) == 0) {
                    gaLog.sendEvent(
                        screenName,
                        eventName,
                        actionName,
                        parameter,
                    )
                }
            } else {
                gaLog.sendEvent(
                    screenName,
                    eventName,
                    actionName,
                    parameter,
                )
            }
        }

        fun onDateSelected(
            context: Context,
            millis: Long,
            categoryName: YouTubeCategory,
        ) {
            viewModelScope.launch {
                val instant = Instant.ofEpochMilli(millis)
                val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                RLog.d("KKKKKK", "date : $date")
                _selectedDate.value = date.toString().replace("-", "")
                _selectedCategory.value = categoryName

                requestDailyShortsSearch(
                    context,
                    _selectedDate.value,
                    settingRespository.getLocale().first() ?: "KR",
                )
            }
        }

        fun selectCategory(category: YouTubeCategory) {
            _selectedCategory.value = category
        }

        fun resetDate() {
            _selectedDate.value = null
        }

        fun resetData() {
            _dailyCurrentSearchShortformList.value = mutableListOf()
        }

        fun getInitCategory() =
            YouTubeCategory(
                "0",
                getAppLocaleByStringResource(
                    context,
                    _currentLocale.value,
                    R.string.youtube_category_all,
                ),
            )

        fun getFindCategory(categoryKey: String?): YouTubeCategory? =
            _youtubeCategory.value.map { it }.find { it.categoryKey == categoryKey }

        companion object {
            private const val TAG = "SearchViewModel"
            private const val INIT_MAX_SIZE = 30
        }
    }
