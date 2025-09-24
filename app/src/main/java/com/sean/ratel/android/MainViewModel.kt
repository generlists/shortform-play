package com.sean.ratel.android

import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.RANDOM_GA_END_SIZE
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.log.GALog
import com.sean.ratel.android.data.repository.RecentVideoRepository
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.ui.ad.GoogleMobileAdsConsentManager
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        val navigator: Navigator,
        val gaLog: GALog,
        val recentVideoRepository: RecentVideoRepository,
        val settingRepository: SettingRepository,
        val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager,
    ) : ViewModel() {
        // FAB 가시성을 관리하는 상태
        private val _isDebugVisible = MutableStateFlow(false)
        val isDebugVisible: StateFlow<Boolean> = _isDebugVisible

        // Top bar 가시성을 관리하는 상태
        private val _isTopViewVisible = MutableStateFlow(true)
        val isTopViewVisible: StateFlow<Boolean> = _isTopViewVisible

        // shortform video
        private val _shortFormVideoList =
            MutableStateFlow<Map<String, List<MainShortsModel>>>(
                mutableMapOf(),
            )
        val shortFormVideoList: StateFlow<Map<String, List<MainShortsModel>>> = _shortFormVideoList

        private val _mainShorts =
            MutableStateFlow(Pair(MainShortFormList(), MAIN_ITEM_COUNT))
        val mainShorts: StateFlow<Pair<MainShortFormList, Int>> = _mainShorts

        private val _trendsShorts = MutableStateFlow(TrendsShortFormList())
        val trendsShorts: StateFlow<TrendsShortFormList> = _trendsShorts

        private val _mainTrendShortsList = MutableStateFlow(emptyList<MainShortsModel>())
        val mainTrendShortsList: StateFlow<List<MainShortsModel>> = _mainTrendShortsList

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

        fun setPIPClick(pipClick: Pair<Boolean, ViewPager2?>) {
            _pipClick.value = pipClick
            _isTopViewVisible.value = !pipClick.first && !isCurrentPageMoreView()
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

        fun goEndContent() {
            navigator.navigateTo(Destination.YouTube.dynamicRoute(_selectedIndex.toString()), false)
        }

        fun goEndContent(
            route: String,
            viewType: ViewType,
            selectedIndex: Int = 0,
            channelId: String? = null,
            videoId: String? = null,
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
                else -> Unit
            }
        }

        fun goMoreContent(
            route: String,
            viewType: ViewType,
            trendsShortsKey: String? = null,
        ) {
            _viewType.value = viewType
            _moreButtonClicked.value = route
            _moreTrendShortsKey.value = trendsShortsKey
            navigator.navigateTo(route, false)
        }

        fun goSettingView() {
            navigator.navigateTo(Destination.Setting.route)
        }

        fun mainShortsData(
            mainShorts: Pair<MainShortFormList, Int>,
            trendsShorts: TrendsShortFormList,
            mainTrendShortsList: List<MainShortsModel>,
        ) {
            _mainShorts.value = mainShorts
            _trendsShorts.value = trendsShorts
            _mainTrendShortsList.value = mainTrendShortsList
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
        ) {
            navigator.navigateBack(recreate)
            _tabClicked.value = null

            if (route == Destination.YouTube.route) _endBack.value = true
        }

        fun goMainHome() {
            viewModelScope.launch {
                navigator.navigateTo(Destination.Home.route, true)
            }
        }

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

        fun runPrivacyOptionMenu(activity: MainActivity) {
            googleMobileAdsConsentManager.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Toast.makeText(activity, formError.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        suspend fun setWatchVideoList() {
            _watchVideoList.value = getRecentVideo()
        }

        suspend fun firebaseRemoteConfig(remoteConfig: FirebaseRemoteConfig) {
            remoteConfig
                .setDefaultsAsync(R.xml.remote_config_defaults)
                .addOnSuccessListener {
                    RemoteConfig.setRemoteConfig(remoteConfig.all)
                }.addOnFailureListener(
                    object : OnFailureListener {
                        override fun onFailure(p0: Exception) {
                            RLog.e("MainViewModel", "Fail RemoteConfig $p0")
                        }
                    },
                )

            // 2. 서버 값 가져오기 (캐시 0초로 강제 새로고침)
            remoteConfig
                .fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        RLog.d("RemoteConfig", "Fetch success. Updated: $updated")
                        // 실제 값 로그 출력
                        remoteConfig.all.forEach { entry ->
                            RLog.d("RemoteConfig", "${entry.key} = ${entry.value.asString()}")
                        }

                        RemoteConfig.setRemoteConfig(remoteConfig.all)
                    } else {
                        RLog.e("RemoteConfig", "Fetch failed: ${task.exception}")
                    }
                }
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

        companion object {
            private const val MAIN_ITEM_COUNT = 7
        }
    }
