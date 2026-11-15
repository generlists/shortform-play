package com.sean.ratel.android.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.filter.CompiledFilter
import com.sean.ratel.android.data.filter.FilterRepository
import com.sean.ratel.android.data.filter.SearchFilterInfo
import com.sean.ratel.android.data.filter.SearchFilterMode
import com.sean.ratel.android.data.filter.SearchFilterPolicy
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.log.GASplashAnalytics.Event.SELECT_SEARCH_DAILY_ITEM_CLICK
import com.sean.ratel.android.data.log.GASplashAnalytics.Event.SELECT_SEARCH_ITEM_CLICK
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeRepository
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.UIUtil.getAppLocaleByStringResource
import com.sean.ratel.android.utils.UIUtil.getLanguageCode
import com.sean.ratel.android.utils.UIUtil.localeFromCountryCode
import com.sean.ratel.android.utils.UIUtil.toStringMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
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
        private val filterRepository: FilterRepository,
    ) : ViewModel() {
        // 서버에서 받은 결과 원본. 다음 페이지 요청(마지막 영상 id)과 개수 계산은 항상 원본을 쓴다
        private val _shortsSearchList = MutableStateFlow(emptyList<SearchResultModel>())

        // 화면이 그리는 목록 — 내 제외 필터를 적용한 결과(SearchFilterPolicy 규칙). 필터가 없으면 원본과 같다
        private val visibleSearchList = MutableStateFlow(emptyList<SearchResultModel>())
        val shortsSearchList: StateFlow<List<SearchResultModel>> = visibleSearchList

        private val _searchFilterInfo = MutableStateFlow(SearchFilterInfo())
        val searchFilterInfo: StateFlow<SearchFilterInfo> = _searchFilterInfo

        /** 원본 결과 개수. 페이지가 도착했는지(전부 숨겨져 화면 목록은 그대로여도) 화면이 알 수 있게 한다. */
        private val _rawResultCount = MutableStateFlow(0)
        val rawResultCount: StateFlow<Int> = _rawResultCount

        @Volatile
        private var compiledFilter: CompiledFilter = CompiledFilter.NoOp
        private var currentQueryText = ""
        private var pendingDeepLinkQuery: String? = null
        private var deepLinkSearch = false
        private var revealHidden = false
        private var fillStreak = 0

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
            observeFilter()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observeFilter() {
            viewModelScope.launch {
                settingRespository
                    .getLocale()
                    .distinctUntilChanged()
                    .flatMapLatest { filterRepository.compiled(it) }
                    .collect {
                        compiledFilter = it
                        publishSearchList()
                    }
            }
        }

        /** 원본 결과에 필터 규칙을 적용해 화면 목록과 안내 상태를 만든다. */
        private fun publishSearchList() {
            val raw = _shortsSearchList.value
            val view = SearchFilterPolicy.keyword(raw, compiledFilter, currentQueryText, deepLinkSearch, revealHidden)
            visibleSearchList.value = view.visible
            _searchFilterInfo.value = SearchFilterInfo(view.mode, view.hiddenCount)
            _rawResultCount.value = raw.size
        }

        /** "숨긴 영상 N개 보기" / "다시 숨기기". */
        fun setRevealHidden(reveal: Boolean) {
            revealHidden = reveal
            publishSearchList()
        }

        /**
         * 걸러서 화면 목록이 짧아졌을 때 다음 페이지를 자동으로 더 받아도 되는지.
         * 페이지를 받아도 보이는 영상이 늘지 않는 일이 연속 3번이면 멈춘다(사용자가 다시 스크롤하면 재개).
         */
        fun canAutoFill(): Boolean =
            fillStreak < MAX_FILL_STREAK &&
                _searchFilterInfo.value.let {
                    it.mode == SearchFilterMode.FILTERED && it.hiddenCount > 0
                }

        fun resetFillStreak() {
            fillStreak = 0
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
                                // 새 검색: 검색어와 웹 링크 여부를 정하고 필터를 적용해 화면 목록을 만든다
                                currentQueryText = query.split("+").firstOrNull()?.trim() ?: query.trim()
                                deepLinkSearch = pendingDeepLinkQuery?.trim().equals(currentQueryText, ignoreCase = true)
                                if (!deepLinkSearch) pendingDeepLinkQuery = null
                                revealHidden = false
                                fillStreak = 0
                                _shortsSearchList.value = response.data.results
                                publishSearchList()
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
            // 화면이 곧바로 null 로 비우므로, 웹 링크로 온 검색어를 따로 기억해서 결과가 오면 필터 없이 보여 준다
            if (query != null) pendingDeepLinkQuery = query
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
                                    val visibleBefore = visibleSearchList.value.size
                                    val currentList = _shortsSearchList.value.toMutableList()
                                    currentList.addAll(response.data.results)
                                    _shortsSearchList.value = currentList // ← 새로운 객체로 교체해야 emit됨
                                    publishSearchList()
                                    // 페이지를 받았는데 보이는 영상이 늘지 않았다(전부 필터에 걸림) — 자동 이어받기 횟수를 센다
                                    fillStreak = if (visibleSearchList.value.size > visibleBefore) 0 else fillStreak + 1
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
                val language = getLanguageCode(countryCode)

                val categoryList = youtubeApiRepository.getYouTubeCategoryList()
                RLog.d("OKJJJJJJJ", "countryCode : $countryCode  categoryList : $categoryList language : $language")

                if (categoryList.isNotEmpty() && categoryList.first().categoryKey != null) {
                    _youtubeCategory.value = categoryList
                    return@launch
                }
                youtubeApiRepository.requestYouTubeCategory(countryCode, language).collect { response ->

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
            RLog.d("SearchViewModel", "date : $date ,  region : $region")

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
                                RLog.d("SearchViewModel", "message : ${response.e.message}")
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
                    shortformList.trendShortsList.event_list.values
                        .flatten() +
                    getTopicVideoList(shortformList.topicList.topicList)
                        .flatten() +

                    shortformList.shortformVideoList.videoSearchList.searchList +
                    shortformList.shortformVideoList.videoLikeList.likeList +
                    shortformList.shortformVideoList.videoCommentList.commentList +

                    shortformList.channelVideoList.channelSearchList.searchList +
                    shortformList.channelVideoList.channelLikeList.likeList +

                    shortformList.editorPickList.pickList +
                    shortformList.channelSubscriptionList.subscriptionList +
                    shortformList.channelSubscriptionUpList.subscriptionUpList +
                    shortformList.shortformRecommendList.recommendList
                        .distinctBy { it.shortsVideoModel?.videoId }

//            dailyShortFormList.forEach {
//                RLog.d(
//                    "BBBBDDDD",
//                    "category Key : ${_selectedCategory.value.categoryKey}" +
//                        "  topicId : ${it.shortsVideoModel?.topicKey} " +
//                        "   dailyShortFormList size ${dailyShortFormList.size}",
//                )
//            }

            val dailySearchShorts =
                if (_selectedCategory.value.categoryKey == "0") {
                    dailyShortFormList
                        .toMutableList()
                        .shuffled()
                } else {
                    dailyShortFormList
                        .filter {
                            it.shortsVideoModel?.category == _selectedCategory.value.categoryKey ||
                                it.shortsVideoModel?.topicKey == _selectedCategory.value.categoryKey
                        }.toMutableList()
                        .shuffled()
                }

//            RLog.d(
//                "CATEGORYKKKKKK",
//                dailyShortFormList.joinToString("\n") {
//                    "category=${it.shortsVideoModel?.category}, selected=${_selectedCategory.value.categoryKey} , topicKey=${it.shortsVideoModel?.topicKey}"
//                },
//            )

//            RLog.e("SearchViewModel", "dailySearchShorts  size ${dailySearchShorts.size}")

            val visibleDaily = filterDaily(dailySearchShorts)
            val list =
                visibleDaily
                    .filter { it.shortsVideoModel != null }
                    .distinctBy { it.shortsVideoModel!!.videoId }
                    .chunked(INIT_MAX_SIZE)
                    .mapIndexed { i, chunk -> i to chunk }
                    .toMap()

            _dailySearchShortformList.value = visibleDaily
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

        /**
         * 일간 검색 목록에 내 제외 필터를 한 번 적용한다. 목록 위치 번호가 끝 화면과 이어지므로 화면용·끝 화면용 목록을
         * 모두 이 결과에서 만든다. 웹 링크로 들어온 경우(_deepLinkTab)는 적용하지 않고, 전부 걸리면 원본을 쓴다.
         */
        private fun filterDaily(items: List<MainShortsModel>): List<MainShortsModel> =
            SearchFilterPolicy.daily(items, compiledFilter, _deepLinkTab.value != null).visible

        private fun getTopicVideoList(topicList: Map<String, TopicItem>): Collection<List<MainShortsModel>> =
            topicList
                .mapValues { (_, topicItem) ->
                    listOfNotNull(topicItem.popularlist, topicItem.viewlist, topicItem.subscriberlist)
                        .flatMap { it.topicList }
                        .flatMap { it.topicList }
                        .map {
                            it.copy(
                                shortsVideoModel =
                                    it.shortsVideoModel?.let { video ->
                                        RLog.e("KKKKKMM", "topicId : ${topicItem.topicId}")
                                        video.copy(
                                            topicName = topicItem.topicName,
                                            copyCategory = video.categoryName,
                                            categoryName = topicItem.topicName,
                                            topicKey = topicItem.topicId,
                                        )
                                    },
                            )
                        }
                }.values

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

            val visibleDaily = filterDaily(dailySearchShorts)
            val list =
                visibleDaily
                    .filter { it.shortsVideoModel != null }
                    .distinctBy { it.shortsVideoModel!!.videoId }
                    .chunked(INIT_MAX_SIZE)
                    .mapIndexed { i, chunk -> i to chunk }
                    .toMap()

            _dailySearchShortformList.value = visibleDaily
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
                RLog.d("onDateSelected", "date : $date , categoryName : $categoryName")
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
            private const val MAX_FILL_STREAK = 3
        }
    }
