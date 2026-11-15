package com.sean.ratel.android

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.RANDOM_GA_END_SIZE
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.filter.CompiledFilter
import com.sean.ratel.android.data.filter.FilterRepository
import com.sean.ratel.android.data.filter.FilterSyncRepository
import com.sean.ratel.android.data.filter.HiddenSample
import com.sean.ratel.android.data.filter.allVideos
import com.sean.ratel.android.data.filter.channelNames
import com.sean.ratel.android.data.filter.filterList
import com.sean.ratel.android.data.filter.filterMain
import com.sean.ratel.android.data.filter.filterTrends
import com.sean.ratel.android.data.filter.preview
import com.sean.ratel.android.data.library.LibrarySyncRepository
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.InstallRefererRepository
import com.sean.ratel.android.data.repository.RecentVideoRepository
import com.sean.ratel.android.data.repository.SearchResultDataRepository
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.watch.WatchRecorder
import com.sean.ratel.android.ui.ad.AdTarget
import com.sean.ratel.android.ui.ad.InterstitialAdManager
import com.sean.ratel.android.ui.end.YouTubeEndFragment
import com.sean.ratel.android.ui.home.TopicFilterType
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import com.sean.ratel.android.utils.PhoneUtil.newActivity
import com.sean.ratel.android.utils.onShareClicked
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import so.smartlab.common.ad.admob.AdsSdk
import so.smartlab.common.ad.admob.data.GoogleMobileAdsConsentManager
import so.smartlab.common.ad.admob.data.model.AdMobBannerState
import so.smartlab.common.ad.admob.data.model.AdMobInitState
import so.smartlab.common.push.PushSDK
import so.smartlab.common.review.ReviewManager
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val navigator: Navigator,
        val imageLoader: ImageLoader,
        val gaLog: GALog,
        val recentVideoRepository: RecentVideoRepository,
        val installRefererRepository: InstallRefererRepository,
        private val searchResultsRepository: SearchResultDataRepository,
        private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager,
        private val settingRepository: SettingRepository,
        private val filterRepository: FilterRepository,
        private val filterSyncRepository: FilterSyncRepository,
        private val watchRecorder: WatchRecorder,
        private val librarySyncRepository: LibrarySyncRepository,
        val interstitialAdManager: InterstitialAdManager,
        val adsSdk: AdsSdk,
        val pushSDK: PushSDK,
        val reviewManager: ReviewManager,
    ) : ViewModel() {
        private val _isInstallReferer = MutableStateFlow<String?>(null)
        val isInstallReferer: StateFlow<String?> = _isInstallReferer

        // FAB 가시성을 관리하는 상태
        private val _isDebugVisible = MutableStateFlow(false)
        val isDebugVisible: StateFlow<Boolean> = _isDebugVisible

        // Top bar 가시성을 관리하는 상태
        private val _isTopViewVisible = MutableStateFlow(true)
        val isTopViewVisible: StateFlow<Boolean> = _isTopViewVisible

        // shortform video
        private val _shortFormVideoList =
            MutableStateFlow<Map<String, List<MainShortsModel>>>(
                mapOf(),
            )
        val shortFormVideoList: StateFlow<Map<String, List<MainShortsModel>>> = _shortFormVideoList

        private val _mainShorts =
            MutableStateFlow(Pair(MainShortFormList(), MAIN_ITEM_COUNT))
        val mainShorts: StateFlow<Pair<MainShortFormList, Int>> = _mainShorts

        private val _trendsShorts = MutableStateFlow(TrendsShortFormList())
        val trendsShorts: StateFlow<TrendsShortFormList> = _trendsShorts

        private val _mainTrendShortsList = MutableStateFlow(emptyList<MainShortsModel>())
        val mainTrendShortsList: StateFlow<List<MainShortsModel>> = _mainTrendShortsList

        // 내 제외 필터 — 서버에서 받은 원본을 보관하고, 필터가 바뀔 때마다 다시 걸러서 위 상태(_mainShorts 등)에 반영한다.
        // 필터가 없으면(기본) 원본이 그대로 들어가므로 기존 동작과 같다.
        private var rawMainShorts: Pair<MainShortFormList, Int>? = null
        private var rawTrendsShorts = TrendsShortFormList()
        private var rawMainTrendShortsList = emptyList<MainShortsModel>()

        @Volatile
        private var compiledFilter: CompiledFilter = CompiledFilter.NoOp

        private val _hiddenCount = MutableStateFlow(0)

        private val _hiddenSamples = MutableStateFlow(emptyList<HiddenSample>())

        /** 지금 필터로 빠진 영상과 이유("숨긴 영상" 화면용). 필터가 바뀌면 같이 바뀐다. */
        val hiddenSamples: StateFlow<List<HiddenSample>> = _hiddenSamples

        /** 홈 데이터에 나온 채널 id → 이름. */
        fun channelNames(): Map<String, String> = rawMain().channelNames()

        /** 이 id 들의 영상(홈 데이터에 있는 것만). "빼지 않은 영상" 목록용. */
        fun videosById(ids: Collection<String>): List<MainShortsModel> {
            if (ids.isEmpty()) return emptyList()
            val wanted = ids.toSet()
            return rawMain().allVideos().filter { it.shortsVideoModel?.videoId in wanted }
        }

        /** 필터로 빠진 영상 수 ("N개를 빼고 보여드렸어요" 표시용). */
        val hiddenCount: StateFlow<Int> = _hiddenCount

        init {
            observeFilter()
            // 로그인 상태를 지켜보며 내 필터를 서버와 맞춘다(로그인하지 않으면 기기의 필터도 없다)
            filterSyncRepository.start(viewModelScope)
            // 내 보관함(좋아요·폴더)도 로그인 상태를 지켜보며 서버와 맞춘다. 이 기기의 예전 좋아요는 처음 한 번 "좋아요" 폴더로 옮긴다
            librarySyncRepository.start(viewModelScope)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observeFilter() {
            viewModelScope.launch {
                // 국가(언어)가 바뀌거나 필터가 바뀌면 다시 컴파일된 필터가 온다
                settingRepository
                    .getLocale()
                    .distinctUntilChanged()
                    .flatMapLatest { filterRepository.compiled(it) }
                    .collect {
                        compiledFilter = it
                        applyFilter()
                    }
            }
        }

        private fun applyFilter() {
            val raw = rawMainShorts ?: return
            val filter = compiledFilter
            val main = filter.filterMain(raw.first)
            val trends = filter.filterTrends(rawTrendsShorts)
            val trendList = filter.filterList(rawMainTrendShortsList)
            _mainShorts.value = raw.copy(first = main.value)
            _trendsShorts.value = trends.value
            _mainTrendShortsList.value = trendList.value
            val hiddenIds = main.hiddenIds + trends.hiddenIds + trendList.hiddenIds
            _hiddenCount.value = hiddenIds.size
            // 마이 탭의 "이번 주에 N개를 빼고 보여드렸어요"용으로 하루 합계를 쌓는다(영상 id 는 오늘 분만 중복 제거에 쓰고 버린다)
            watchRecorder.recordHidden(hiddenIds)
            _hiddenSamples.value = filter.preview(raw.first).hidden
        }

        private val _isHomeVisible = MutableStateFlow(true)
        val isHomeVisible: StateFlow<Boolean> = _isHomeVisible

        private val _selectedIndex =
            mutableStateOf(0)

        val selectedIndex: MutableState<Int> = _selectedIndex

        private val _itemClicked = mutableStateOf<String?>(null)
        val itemClicked: MutableState<String?> = _itemClicked

        private val _viewType = MutableStateFlow<ViewType>(ViewType.ImageFlow)
        val viewType: MutableStateFlow<ViewType> = _viewType

        private val _tabClicked = MutableStateFlow<String?>(null)
        val tabClicked: Flow<String?> = _tabClicked

        private val _endBack = MutableStateFlow(false)
        val endBack: StateFlow<Boolean> = _endBack

        private val _moreButtonClicked = mutableStateOf<String?>(null)
        val moreButtonClicked: MutableState<String?> = _moreButtonClicked

        private val _channelCurrentPager = MutableStateFlow<Int>(0)
        val channelCurrentPager: Flow<Int> = _channelCurrentPager

        private val _popularShortFormPager = MutableStateFlow<Int>(0)
        val popularShortFormPager: Flow<Int> = _popularShortFormPager

        private val _recentVideo = MutableStateFlow<Pair<MainShortsModel?, Float>>(Pair(null, 0f))
        val recentVideo: StateFlow<Pair<MainShortsModel?, Float>?> = _recentVideo

        private val _watchVideoList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val watchVideoList: StateFlow<List<MainShortsModel>> = _watchVideoList

        private val _isPrivacyOptionMenu = MutableStateFlow<Boolean>(false)
        val isPrivacyOptionMenu: Flow<Boolean> = _isPrivacyOptionMenu

        private val _currentSelection = MutableStateFlow<Int>(0)
        val currentSelection: Flow<Int> = _currentSelection

        private val _topBarHeight = MutableStateFlow<Int>(53)
        val topBarHeight: Flow<Int> = _topBarHeight

        private val _viewPager2 = MutableStateFlow<ViewPager2?>(null)
        val viewPager2: Flow<ViewPager2?> = _viewPager2

        private val _pipClick = MutableStateFlow<Pair<Boolean, ViewPager2?>>(Pair(false, null))
        val pipClick: Flow<Pair<Boolean, ViewPager2?>> = _pipClick

        private val _buttonClickState = MutableStateFlow<Boolean>(false)
        val buttonClickState: Flow<Boolean> = _buttonClickState

        private val _moreTrendShortsKey = MutableStateFlow<String?>(null)
        val moreTrendShortsKey: MutableStateFlow<String?> = _moreTrendShortsKey

        private val _selectVideoId = mutableStateOf<String?>(null)
        val selectVideoId: MutableState<String?> = _selectVideoId

        private val _categoryByContents =
            MutableStateFlow<Map<String, List<MainShortsModel>>>(
                mutableMapOf(),
            )
        val categoryByContents: StateFlow<Map<String, List<MainShortsModel>>> = _categoryByContents

        private val _currentCategory = MutableStateFlow<String>("0")
        val currentCategory: MutableStateFlow<String> = _currentCategory

        private val _topPipClick =
            MutableStateFlow<YouTubeEndFragment?>(null)
        val topPipClick: StateFlow<YouTubeEndFragment?> = _topPipClick

        private val _interstitialAdStart =
            MutableSharedFlow<AdTarget>(
                replay = 1,
                extraBufferCapacity = 1,
            )
        val interstitialAdStart: SharedFlow<AdTarget> = _interstitialAdStart.asSharedFlow()

        private val _currentMainShorts = MutableStateFlow<MainShortsModel?>(null)
        val currentMainShorts: MutableStateFlow<MainShortsModel?> = _currentMainShorts

        private val _bottomBarHeight = mutableStateOf(56)
        val bottomBarHeight = _bottomBarHeight

        fun setBottomBarHeight(height: Int) {
            _bottomBarHeight.value = height
        }

        private val _forceClearCache = MutableStateFlow(false)
        val forceClearCache: StateFlow<Boolean> = _forceClearCache

        fun setForceClearCache(forceClearCache: Boolean) {
            _forceClearCache.value = forceClearCache
        }

        fun setMainShortsModel(mainShortsModel: MainShortsModel?) {
            _currentMainShorts.value = mainShortsModel
        }

        fun setInterstitialAdStart(
            route: String,
            adStart: Boolean,
        ) {
            _interstitialAdStart.tryEmit(AdTarget(route, adStart))
        }

        fun setPIPClick(pipClick: Pair<Boolean, ViewPager2?>) {
            _pipClick.value = pipClick
            _isTopViewVisible.value = !pipClick.first && !isCurrentPageMoreView()
            RLog.d(
                "MainViewModel",
                "_pipClick : ${_pipClick.value},  _isTopViewVisible : ${_isTopViewVisible.value}",
            )
        }

        fun setTopPipClick(fragment: YouTubeEndFragment?) {
            _topPipClick.value = fragment
        }

        fun setViewPager(viewPager2: ViewPager2?) {
            _viewPager2.value = viewPager2
        }

        fun setPIPButtonClickState(isPlaying: Boolean) {
            _buttonClickState.value = isPlaying
        }

        fun setTopBarHeight(height: Int) {
            _topBarHeight.value = height
        }

        fun setCurrentSelection(select: Int) {
            _currentSelection.value = select
        }

        fun setPrivacyOptionMenu(isOptionMenu: Boolean) {
            _isPrivacyOptionMenu.value = isOptionMenu
        }

        fun setViewType(viewType: ViewType) {
            _viewType.value = viewType
        }

        fun setPopularShortFormPager(index: Int) {
            _popularShortFormPager.value = index
        }

        fun setChannelPager(index: Int) {
            _channelCurrentPager.value = index
        }

        fun setEndBack(click: Boolean) {
            _endBack.value = click
        }

        fun setTabClicked(click: String) {
            _tabClicked.value = click
            _popularShortFormPager.value = 0
            _channelCurrentPager.value = 0
        }

        fun setIsHomeVisible(click: Boolean) {
            _isHomeVisible.value = click
        }

        fun setItemClicked(
            route: String?,
            selectedIndex: Int,
        ) {
            _itemClicked.value = route
            _selectedIndex.value = selectedIndex
        }

        // 보관함 폴더에서 재생할 목록(재생 화면이 이 목록을 선택한 위치부터 이어서 재생한다)
        private val _libraryPlaylist = MutableStateFlow(emptyList<MainShortsModel>())
        val libraryPlaylist: StateFlow<List<MainShortsModel>> = _libraryPlaylist

        /** 보관함 폴더의 영상들을 [index] 번째부터 재생한다. */
        fun playLibrary(
            videos: List<MainShortsModel>,
            index: Int,
        ) {
            if (videos.isEmpty()) return
            _libraryPlaylist.value = videos
            goEndContent(
                route = Destination.LibraryFolder.route,
                viewType = ViewType.Library,
                selectedIndex = index.coerceIn(0, videos.lastIndex),
            )
        }

        fun goEndContent() {
            navigator.navigateTo(Destination.YouTube.dynamicRoute(_selectedIndex.toString()), false)
        }

        fun goEndContent(
            route: String,
            viewType: ViewType,
            selectedIndex: Int = 0,
            channelId: String? = null,
            videoId: String? = null,
            topicId: String? = null,
            filterType: TopicFilterType = TopicFilterType.Popular,
        ) {
            _itemClicked.value = route
            _selectedIndex.value = selectedIndex
            _viewType.value = viewType
            _selectVideoId.value = videoId

            when (viewType) {
                ViewType.ImageFlow -> {
                    channelId?.let {
                        navigator.navigateTo(
                            Destination.YouTube.dynamicRoute(it),
                            false,
                        )
                    }
                }

                ViewType.TopicChannel -> {
                    channelId?.let { param ->

                        val createdRoute =
                            Destination.YouTube.createRoute(
                                pathArgs = listOf(param),
                                queryArgs =
                                    mapOf(
                                        "topicId" to topicId,
                                        "filterType" to filterType.name,
                                    ),
                            )

                        navigator.navigateTo(createdRoute, false)
                    }
                }

                ViewType.TopicGroup -> {
                    val createdRoute =
                        Destination.YouTube.createRoute(
                            pathArgs = listOf(selectedIndex.toString()),
                            queryArgs = mapOf("topicId" to topicId, "filterType" to filterType.name),
                        )

                    navigator.navigateTo(createdRoute, false)
                }

                ViewType.PopularSearchShortForm,
                ViewType.PopularLikeShortForm,
                ViewType.PopularCommentShortForm,
                ViewType.EditorPick,
                ViewType.Recommend,
                ViewType.ChannelSearchRanking,
                ViewType.ChannelLikeRanking,
                ViewType.SubscriptionRanking,
                ViewType.SubscriptionRankingUp,
                ViewType.RecentlyWatch,
                ViewType.Library,
                ViewType.MainTrendShorts,
                ViewType.TrendShortsMore,
                -> {
                    navigator.navigateTo(
                        Destination.YouTube.dynamicRoute(selectedIndex.toString()),
                        false,
                    )
                }

                ViewType.ShortFormVideo -> {
                    // 카테코리 키
                    channelId?.let {
                        navigator.navigateTo(
                            Destination.YouTube.dynamicRoute(it),
                            false,
                        )
                    }
                }

                ViewType.SearchShortsVideo, ViewType.DeepLinkVideo -> {
                    videoId?.let {
                        navigator.navigateTo(
                            Destination.YouTube.dynamicRoute(it),
                            false,
                        )
                    }
                }

                ViewType.SearchShortsDaily -> {
                    videoId?.let {
                        navigator.navigateTo(
                            Destination.YouTube.dynamicRoute(it),
                            false,
                        )
                    }
                }

                else -> {
                }
            }
        }

        fun goMoreContent(
            route: String,
            viewType: ViewType,
            trendsShortsKey: String? = null,
            topicId: String? = null,
        ) {
            _viewType.value = viewType
            _moreButtonClicked.value = route
            _moreTrendShortsKey.value = trendsShortsKey
            if (viewType == ViewType.MainTopic || viewType == ViewType.TopicChannel || viewType == ViewType.TopicGroup) {
                topicId?.let {
                    navigator.navigateTo(
                        Destination.Home.Main.TopicListDetail
                            .createRoute(listOf(it)),
                    )
                }
            } else {
                navigator.navigateTo(route, false)
            }
        }

        fun setSettingDetailClick(route: String) {
            _moreButtonClicked.value = route
        }

        fun goSettingView() {
            navigator.navigateTo(Destination.Setting.route)
        }

        fun mainShortsData(
            mainShorts: Pair<MainShortFormList, Int>,
            trendsShorts: TrendsShortFormList,
            mainTrendShortsList: List<MainShortsModel>,
        ) {
            rawMainShorts = mainShorts
            rawTrendsShorts = trendsShorts
            rawMainTrendShortsList = mainTrendShortsList
            applyFilter()
        }

        fun shortFormVideoData(items: Map<String, List<MainShortsModel>>) {
            _shortFormVideoList.value = items
        }

        fun debugVisibility(isDebugVisible: Boolean) {
            _isDebugVisible.value = isDebugVisible
        }

        fun topViewVisibility(isTopViewVisible: Boolean) {
            _isTopViewVisible.value = isTopViewVisible
        }

        fun runDebugEnd() {
            viewModelScope.launch {
                navigator.navigateTo(Destination.DebugMode.route, false)
            }
        }

        fun runSearch() {
            viewModelScope.launch {
                navigator.navigateTo(Destination.Search.route, false)
            }
        }

        fun runNavigationBack(
            route: String? = null,
            recreate: Boolean = false,
            context: Context? = null,
        ) {
            // 시스템 죽으면 재시작
            if (route == null && context != null) {
                newActivity(context = context)
                return
            }

            if (_viewType.value == ViewType.DeepLinkVideo ||
                route == Destination.Home.Main.route
            ) {
                goMainHome()
            } else {
                navigator.navigateBack(
                    false,
                )
            }

            _tabClicked.value = null
            if (isCurrentPageMoreView() && route != Destination.YouTube.route) {
                _moreButtonClicked.value = null
            }

            if (route == Destination.YouTube.route) _endBack.value = true
        }

        fun goMainHome() {
            navigator.navigateTo(Destination.Home.Main.route)
        }

        /** 링크(web to app)로 열렸는지. 그때는 온보딩을 보여주지 않는다 — 사용자가 보려던 화면이 먼저다. */
        @Volatile
        var launchedFromLink: Boolean = false

        /**
         * 스플래시가 끝났을 때. 처음 실행(온보딩을 본 적 없고 필터도 없음)이면 온보딩으로, 아니면 홈으로.
         * 온보딩은 끝나면 자신을 빼고 홈으로 간다([goHomeFromOnboarding]).
         */
        fun goHomeAfterSplash() {
            viewModelScope.launch {
                val seen = filterRepository.onboardingSeen.first()
                val hasFilter = filterRepository.state.first().filter.hasRules
                if (!seen && !hasFilter && !launchedFromLink) {
                    navigator.navigateTo(Destination.Onboarding.route)
                } else {
                    goMainHome()
                }
            }
        }

        fun goHomeFromOnboarding() {
            navigator.navigateTo(Destination.Home.Main.route, popBackstack = true)
        }

        /** 서버에서 받은 홈 데이터 원본(필터 적용 전). 온보딩 미리보기가 "몇 개가 빠지는지" 셀 때 쓴다. */
        fun rawMain(): MainShortFormList = rawMainShorts?.first ?: _mainShorts.value.first

        fun sendGALog(
            event: String,
            route: String? = null,
            viewType: ViewType? = null,
            channelId: String? = null,
            videoId: String? = null,
        ) {
            if (route == Destination.YouTube.route) {
                if (Random.nextInt(RemoteConfig.getRemoteConfigIntValue(RANDOM_GA_END_SIZE)) == 0) {
                    gaLog.sendEvent(
                        event,
                        route,
                        viewType,
                        channelId,
                        videoId,
                    )
                }
            } else {
                gaLog.sendEvent(event, route, viewType, channelId, videoId)
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

        fun sendGALog(pageView: String) {
            gaLog.sendPageView(pageView)
        }

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        suspend fun setRecentVideo() {
            scope.launch {
                val (shortsModel, time) = _recentVideo.value

                if (shortsModel != null && time > 1f) {
                    shortsModel.saveTime = time
                    recentVideoRepository.updateRecentVideo(shortsModel)
                }
            }
        }

        suspend fun removeRecentVideoe() {
            recentVideoRepository.removeRecentVideoe()
        }

        private suspend fun getRecentVideo(): List<MainShortsModel> = recentVideoRepository.getRecentVideo()

        suspend fun recentVideo(
            mainShortsModel: MainShortsModel?,
            saveTime: Float,
        ) {
            _recentVideo.value = Pair(mainShortsModel, saveTime)
        }

        fun runPrivacyOptionMenu(activity: Activity?) {
            if (activity == null) return
            googleMobileAdsConsentManager.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Toast.makeText(activity, formError.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        suspend fun setWatchVideoList() {
            _watchVideoList.value = getRecentVideo()
        }

        private fun isCurrentPageMoreView(): Boolean {
            val moreRoute = _moreButtonClicked.value
            return (
                moreRoute == Destination.Home.Main.RecentlyWatchMore.route ||
                    moreRoute == Destination.Home.Main.EditorPickMore.route ||
                    moreRoute == Destination.Home.Main.PoplarShortFormMore.route ||
                    moreRoute == Destination.Home.Main.RankingChannelMore.route ||
                    moreRoute == Destination.Home.Main.RankingSubscriptionMore.route ||
                    moreRoute == Destination.Home.Main.RankingSubscriptionUpMore.route ||
                    moreRoute == Destination.Home.Main.RecommendMore.route ||
                    moreRoute == Destination.Home.Main.TrendShortsMore.route
            )
        }

        fun setSearchCategoryShortFormVieo() {
            val shorts = _mainShorts.value.first

            val zipList =
                (
                    (shorts.topFiveList.fiveList.flatMap { it.value }) +
                        (
                            shorts.shortformVideoList.videoSearchList.searchList +
                                shorts.shortformVideoList.videoCommentList.commentList +
                                shorts.shortformVideoList.videoLikeList.likeList
                        ) +
                        (
                            shorts.channelVideoList.channelSearchList.searchList +
                                shorts.channelVideoList.channelLikeList.likeList
                        ) +
                        shorts.editorPickList.pickList +
                        shorts.channelSubscriptionList.subscriptionList +
                        shorts.channelSubscriptionUpList.subscriptionUpList +
                        shorts.shortformRecommendList.recommendList
                ).toMutableList()
                    .distinct()
                    .groupBy { it.shortsVideoModel?.categoryName }
                    .filterKeys { it != null }
                    .mapKeys { it.key ?: "음악" }

            _categoryByContents.value = zipList

            RLog.d("OKKKKKKKK", "zipList : ${zipList.keys}")
        }

        fun setSearchCategoryDailyShortFormVieo(dailySearch: List<MainShortsModel>) {
            searchResultsRepository.setResults(dailySearch)
            RLog.d("deeplink", "sizesizesizesize : ${searchResultsRepository.getResults()?.size}")
        }

        fun currentDailyShorts(
            category: String,
            selectedIndex: Int,
        ): List<MainShortsModel>? {
            // 전체
            if (category == "0") {
                val list =
                    searchResultsRepository.getResults()?.let {
                        it
                            .filter { it.shortsVideoModel != null }
                            .distinctBy { it.shortsVideoModel?.videoId }
                    }

                val headVideoList =
                    list?.subList(selectedIndex.coerceAtLeast(0), list.size)
                val tailVideoList =
                    list?.subList(0, selectedIndex.coerceAtMost(list.size))
                if (headVideoList != null && tailVideoList != null) {
                    return headVideoList + tailVideoList
                }
            } else {
                val list =
                    searchResultsRepository.getResults()?.let {
                        it
                            .filter { it.shortsVideoModel != null && (it.shortsVideoModel?.category == category) }
                            .distinctBy { it.shortsVideoModel?.videoId }
                    }
                val headVideoList =
                    list?.subList(selectedIndex.coerceAtLeast(0), list.size)
                val tailVideoList =
                    list?.subList(0, selectedIndex.coerceAtMost(list.size))

                _currentCategory.value = category
                if (headVideoList != null && tailVideoList != null) {
                    return headVideoList + tailVideoList
                }
            }
            return null
        }

        fun setInstallReferer(path: String) {
            viewModelScope.launch {
                installRefererRepository.setInstallReferer(path)
            }
        }

        fun clearSearchData() {
            searchResultsRepository.clearCache()
        }

        suspend fun getInstallRerere(): String? = installRefererRepository.getInstallReferer()

        private val _adMobinitState = MutableStateFlow<AdMobInitState>(AdMobInitState.Idle)
        val adMobinitState: StateFlow<AdMobInitState> = _adMobinitState

        private val _fixedBannerState = MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
        val fixedBannerState: StateFlow<AdMobBannerState> = _fixedBannerState

        private val _adaptiveInlineBannerState =
            MutableStateFlow<AdMobBannerState>(AdMobBannerState.Idle)
        val adaptiveInlineBannerState: StateFlow<AdMobBannerState> = _adaptiveInlineBannerState

        private var interstitialCollectJob: Job? = null
        private var mainBannerCollectJob: Job? = null
        private var inLineAdaptiveCollectJob: Job? = null

        fun initAdMobSDK(activity: Activity) {
            adsSdk.initAdMob(activity)

            viewModelScope.launch {
                adsSdk.adInitState.collect { state ->
                    RLog.d("adMobInitState", "initAdMobSDK $state")
                    setInitAdMobState(state)
                }
            }
        }

        fun setInitAdMobState(state: AdMobInitState) {
            _adMobinitState.value = state
        }

        fun setFixedBannerState(state: AdMobBannerState) {
            _fixedBannerState.value = state
        }

        fun setAdaptiveInLineBannerState(state: AdMobBannerState) {
            _adaptiveInlineBannerState.value = state
        }

        fun requestBannerAdView(
            activity: Activity,
            admobBannerId: String,
        ) {
            mainBannerCollectJob?.cancel()

            mainBannerCollectJob =
                viewModelScope.launch {
                    adsSdk.requestBannerAdView(
                        activity,
                        admobBannerId,
                        BuildConfig.BANNER_UNIT_ID,
                    )
                }
            viewModelScope.launch {
                adsSdk.adFixedBannerState.collect { state ->
                    state[admobBannerId]?.let {
                        setFixedBannerState(it)
                    }
                }
            }
        }

        fun requestInLineBannerAdView(activity: Activity) {
            inLineAdaptiveCollectJob?.cancel()
            inLineAdaptiveCollectJob =
                viewModelScope.launch {
                    adsSdk.requestInLineBannerAdView(
                        activity,
                        BuildConfig.ADAPTIVE_BANNER_UNIT_ID,
                    )
                }
            viewModelScope.launch {
                adsSdk.adAdaptiveBannerState.collect { state ->
                    setAdaptiveInLineBannerState(state)
                }
            }
            pushSDK.register()
        }

        fun deleteWatchItem(mainShortsModel: MainShortsModel) {
            viewModelScope.launch {
                recentVideoRepository.deleteWatchItem(mainShortsModel)
            }
        }

        // 저장
        fun onShareClicked(activity: Activity) {
            reviewManager.onShareClicked(
                activity = activity,
                scope = viewModelScope,
            )
        }

        val locale =
            settingRepository.getLocale().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        companion object {
            private const val MAIN_ITEM_COUNT = 0
            private const val TAG = "MainViewModel"
            private const val INIT_MAX_SIZE = 30
        }
    }
