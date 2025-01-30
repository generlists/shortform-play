package com.sean.ratel.android.ui.end

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.repository.SettingRepository
import com.sean.ratel.android.data.repository.YouTubeEndUserRepository
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class
YouTubeContentEndViewModel
    @Inject
    constructor(
        private val navigator: Navigator,
        private val savedStateHandle: SavedStateHandle,
        private val youTubeEndUserRepository: YouTubeEndUserRepository,
        private val settingRepository: SettingRepository,
    ) : ViewModel() {
        private val _mainFromShorts =
            MutableStateFlow<Pair<MainShortFormList, Int>>(
                Pair(
                    MainShortFormList(),
                    SHORTS_ITEM_SIZE,
                ),
            )
        val mainFromShorts: StateFlow<Pair<MainShortFormList, Int>> = _mainFromShorts

        private val _shortFormVideoMap =
            MutableStateFlow<Map<String, List<MainShortsModel>>>(
                mapOf(),
            )

        private val _imageFlowShortsList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val imageFlowShortsList: StateFlow<List<MainShortsModel>?> = _imageFlowShortsList

        private val _recentlyWatchList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val recentlyWatchList: StateFlow<List<MainShortsModel>> = _recentlyWatchList

        private val _popularShortsFormList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val popularShortsFormList: StateFlow<List<MainShortsModel>?> = _popularShortsFormList

        private val _editorPickList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val editorPickList: StateFlow<List<MainShortsModel>?> = _editorPickList

        private val _recommendShortsList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val recommendShortsList: StateFlow<List<MainShortsModel>> = _recommendShortsList

        private val _channelRankingList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val channelRankingList: StateFlow<List<MainShortsModel>> = _channelRankingList

        private val _subscriptionRankingList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val subscriptionRankingList: StateFlow<List<MainShortsModel>> = _subscriptionRankingList

        private val _subscriptionRankingUpList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val subscriptionRankingUpList: StateFlow<List<MainShortsModel>> = _subscriptionRankingUpList

        private val _shortFormVideoList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val shortFormVideoList: StateFlow<List<MainShortsModel>> = _shortFormVideoList

        private val _watchList = MutableStateFlow<List<MainShortsModel>>(emptyList())
        val watchList: StateFlow<List<MainShortsModel>> = _watchList

        private val _isLoading = mutableStateOf(true)
        val isLoading: MutableState<Boolean> = _isLoading

        private val _isAdLoading = mutableStateOf(false)

        val isAdLoading: MutableState<Boolean> = _isAdLoading

        private val _isPlaying = mutableStateOf(true)

        val isPlaying: MutableState<Boolean> = _isPlaying

        private val _channelId = savedStateHandle.get<String>(Destination.YouTube.routeArgName)

        fun setAdLoading(loading: Boolean) {
            _isAdLoading.value = loading
        }

        fun setLoading(loading: Boolean) {
            _isLoading.value = loading
        }

        fun setPlaying(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        fun mainShortsData(items: Pair<MainShortFormList, Int>) {
            _mainFromShorts.value = items
        }

        fun shortFormVideoData(items: Map<String, List<MainShortsModel>>) {
            _shortFormVideoMap.value = items
        }

        fun setRecentlyWatchData(items: List<MainShortsModel>) {
            _recentlyWatchList.value = items
        }

        fun setImageFlowData() {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_imageFlowShortsList.value.isNotEmpty()) {
                _imageFlowShortsList.value = emptyList() // clear 대신 새로운 빈 리스트 할당
            }
            val headList = _mainFromShorts.value.first.topFiveList.fiveList[_channelId]

            val tailList =
                _mainFromShorts.value.first.topFiveList.fiveList
                    .filter { it.key != _channelId }
                    .flatMap { it.value }

            if (headList != null) {
                _imageFlowShortsList.value = headList + tailList
            }
        }

        fun setPopularShortFormData(
            viewType: ViewType,
            selectedIndex: Int,
        ) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_popularShortsFormList.value.isNotEmpty()) {
                _popularShortsFormList.value = emptyList() // clear 대신 새로운 빈 리스트 할당
            }
            val popularList: List<MainShortsModel>? =
                when (viewType) {
                    ViewType.PopularSearchShortForm -> mainFromShorts.value.first.shortformVideoList.videoSearchList.searchList
                    ViewType.PopularLikeShortForm -> mainFromShorts.value.first.shortformVideoList.videoLikeList.likeList
                    ViewType.PopularCommentShortForm -> mainFromShorts.value.first.shortformVideoList.videoCommentList.commentList
                    else -> null
                }

            val size = popularList?.size ?: 0

            if (popularList != null && selectedIndex < size) {
                val headVideoList =
                    popularList.subList(selectedIndex.coerceAtLeast(0), popularList.size)
                val tailVideoList = popularList.subList(0, selectedIndex.coerceAtMost(popularList.size))

                _popularShortsFormList.value = headVideoList + tailVideoList
            } else {
                RLog.e(TAG, "no data found : setPopularShortFormData")
            }
        }

        fun setEditorPickData(selectedIndex: Int) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_editorPickList.value.isNotEmpty()) {
                _editorPickList.value = emptyList()
            }
            val editorPickList = mainFromShorts.value.first.editorPickList.pickList

            if (editorPickList.isEmpty() || selectedIndex > editorPickList.size) {
                _editorPickList.value = editorPickList
            }

            val headVideoList =
                editorPickList.subList(selectedIndex.coerceAtLeast(0), editorPickList.size)
            val tailVideoList =
                editorPickList.subList(0, selectedIndex.coerceAtMost(editorPickList.size))

            _editorPickList.value = headVideoList + tailVideoList
        }

        fun setRecommendData(selectedIndex: Int) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_recommendShortsList.value.isNotEmpty()) {
                _recommendShortsList.value = emptyList()
            }
            val recommendList = mainFromShorts.value.first.shortformRecommendList.recommendList

            if (recommendList.isEmpty() || selectedIndex > recommendList.size) {
                _recommendShortsList.value = recommendList
            }

            val headVideoList =
                recommendList.subList(selectedIndex.coerceAtLeast(0), recommendList.size)
            val tailVideoList = recommendList.subList(0, selectedIndex.coerceAtMost(recommendList.size))

            _recommendShortsList.value = headVideoList + tailVideoList
        }

        fun setChannelRankingData(
            viewType: ViewType,
            selectedIndex: Int,
        ) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화

            val channelList: List<MainShortsModel>? =
                when (viewType) {
                    ViewType.ChannelSearchRanking -> mainFromShorts.value.first.channelVideoList.channelSearchList.searchList
                    ViewType.ChannelLikeRanking -> mainFromShorts.value.first.channelVideoList.channelLikeList.likeList
                    else -> null
                }
            val size = channelList?.size ?: 0

            if (channelList != null && selectedIndex < size) {
                val headVideoList =
                    channelList.subList(selectedIndex.coerceAtLeast(0), channelList.size)
                val tailVideoList = channelList.subList(0, selectedIndex.coerceAtMost(channelList.size))

                _channelRankingList.value = headVideoList + tailVideoList
            } else {
                RLog.e(TAG, "no data found : setChannelRankingData")
            }
        }

        fun setSubscriptionRankingData(selectedIndex: Int) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_subscriptionRankingList.value.isNotEmpty()) {
                _subscriptionRankingList.value = emptyList()
            }
            val subscriptionList = mainFromShorts.value.first.channelSubscriptionList.subscriptionList

            if (subscriptionList.isEmpty() || selectedIndex > subscriptionList.size) {
                _subscriptionRankingList.value = subscriptionList
            }

            val headVideoList =
                subscriptionList.subList(selectedIndex.coerceAtLeast(0), subscriptionList.size)
            val tailVideoList =
                subscriptionList.subList(0, selectedIndex.coerceAtMost(subscriptionList.size))

            _subscriptionRankingList.value = headVideoList + tailVideoList
        }

        fun setSubscriptionRankingUpData(selectedIndex: Int) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_subscriptionRankingUpList.value.isNotEmpty()) {
                _subscriptionRankingUpList.value = emptyList()
            }
            val subscriptionUpList =
                mainFromShorts.value.first.channelSubscriptionUpList.subscriptionUpList

            if (subscriptionUpList.isEmpty() || selectedIndex > subscriptionUpList.size) {
                _subscriptionRankingUpList.value = subscriptionUpList
            }

            val headVideoList =
                subscriptionUpList.subList(selectedIndex.coerceAtLeast(0), subscriptionUpList.size)
            val tailVideoList =
                subscriptionUpList.subList(0, selectedIndex.coerceAtMost(subscriptionUpList.size))

            _subscriptionRankingUpList.value = headVideoList + tailVideoList
        }

        // 전체 영상이 아니고 현재 보여지는 view 영상을 넘겨준다.
        fun setShortFormVideoData(selectedIndex: Int) {
            val categoryId = _channelId

            if (_shortFormVideoList.value.isNotEmpty()) {
                _shortFormVideoList.value = emptyList()
            }
            val currentCategory = _shortFormVideoMap.value[categoryId]
            val headVideoList =
                currentCategory?.subList(selectedIndex.coerceAtLeast(0), currentCategory.size)

            val tailVideoList =
                currentCategory?.subList(0, selectedIndex.coerceAtMost(currentCategory.size))

            val otherTailList =
                _shortFormVideoMap.value
                    .filter { it.key != categoryId }
                    .flatMap { it.value }

            if (headVideoList != null && tailVideoList != null) {
                _shortFormVideoList.value = headVideoList + tailVideoList + otherTailList
            }
        }

        fun setWatchData(selectedIndex: Int) {
            // 리스트가 비어있지 않으면 빈 리스트로 초기화
            if (_watchList.value.isNotEmpty()) {
                _watchList.value = emptyList()
            }
            val watchList = _recentlyWatchList.value

            if (watchList.isEmpty() || selectedIndex > watchList.size) {
                _watchList.value = watchList
            }

            val headVideoList =
                _recentlyWatchList.value.subList(
                    selectedIndex.coerceAtLeast(0),
                    _recentlyWatchList.value.size,
                )
            val tailVideoList =
                _recentlyWatchList.value.subList(
                    0,
                    selectedIndex.coerceAtMost(_recentlyWatchList.value.size),
                )

            _watchList.value = headVideoList + tailVideoList
        }

        fun back() {
            navigator.navigateBack(recreate = false)
        }

        fun runSettingView() {
            navigator.navigateTo(Destination.Setting.route)
        }

        suspend fun likeDisLike(
            key: String,
            mainShortsModel: MainShortsModel?,
        ) {
            youTubeEndUserRepository.likeDisLike(key, mainShortsModel)
        }

        suspend fun canCelLikeDisLike(key: String) {
            youTubeEndUserRepository.cancelLikeDisLike(key)
        }

        suspend fun setSoundOff(isSound: Boolean) {
            settingRepository.setSoundOnOff(isSound)
        }

        suspend fun getLikeDisLikeVideo(videoId: String): Boolean =
            youTubeEndUserRepository.getLikeDisLikeVideo(videoId)?.let {
                true
            } ?: false

        suspend fun getAutoPlay() = settingRepository.getAutoPlay()

        suspend fun getLoopPlay() = settingRepository.getLoopPlay()

        suspend fun getWifiOnlyPlay() = settingRepository.getWifiOnlyPlay()

        suspend fun setWifiOnlyPlay() = settingRepository.setWifiOnlyPlay(isWifiPlay = false)

        suspend fun getSoundOff() = settingRepository.getSoundOnOff()

        companion object {
            val TAG = "YouTubeContentEndViewModel"
            const val SHORTS_ITEM_SIZE = 7
        }

        enum class PageScrollState {
            IDLE,
            SCROLL_DOWNWARDS,
            SCROLL_UPWARDS,
        }
    }
