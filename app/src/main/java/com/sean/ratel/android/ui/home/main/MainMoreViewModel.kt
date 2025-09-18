package com.sean.ratel.android.ui.home.main

import androidx.lifecycle.ViewModel
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.dto.ChannelSubscriptionList
import com.sean.ratel.android.data.dto.ChannelSubscriptionUpList
import com.sean.ratel.android.data.dto.ChannelVideoList
import com.sean.ratel.android.data.dto.EditorPickList
import com.sean.ratel.android.data.dto.MainShortFormList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.ShortFormVideoList
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("ktlint:standard:property-naming")
@HiltViewModel
class MainMoreViewModel
    @Inject
    constructor(
        val navigator: Navigator,
    ) : ViewModel() {
        private val _currentDataList = MutableStateFlow<MutableList<MainShortsModel>>(mutableListOf())
        val currentDataList: StateFlow<MutableList<MainShortsModel>> = _currentDataList

        private val _popularShortsFormMoreList =
            MutableStateFlow<ShortFormVideoList>(ShortFormVideoList())
        val popularShortsFormMoreList: StateFlow<ShortFormVideoList> = _popularShortsFormMoreList

        private val _editorPickMoreList = MutableStateFlow<EditorPickList>(EditorPickList())
        val editorPickMoreList: StateFlow<EditorPickList> = _editorPickMoreList

        private val _recommendMoreList = MutableStateFlow<RecommendList>(RecommendList())
        val recommendMoreList: StateFlow<RecommendList> = _recommendMoreList

        private val _channelMoreList = MutableStateFlow<ChannelVideoList>(ChannelVideoList())
        val channelMoreList: StateFlow<ChannelVideoList> = _channelMoreList

        private val _subscriptionMoreList =
            MutableStateFlow<ChannelSubscriptionList>(
                ChannelSubscriptionList(),
            )
        val subscriptionMoreList: StateFlow<ChannelSubscriptionList> = _subscriptionMoreList

        private val _subscriptionUpMoreList =
            MutableStateFlow<ChannelSubscriptionUpList>(
                ChannelSubscriptionUpList(),
            )
        val subscriptionUpMoreList: StateFlow<ChannelSubscriptionUpList> = _subscriptionUpMoreList

        private val _recentlyWatchMoreList =
            MutableStateFlow<List<MainShortsModel>>(
                emptyList(),
            )
        val recentlyWatchMoreList: StateFlow<List<MainShortsModel>> = _recentlyWatchMoreList

        private val _trendShortsMoreList = MutableStateFlow<TrendsShortFormList?>(TrendsShortFormList())
        val trendShortsMoreList: StateFlow<TrendsShortFormList?> = _trendShortsMoreList

        private var _currentFilter = 0

        private val _initScroll = MutableStateFlow<Boolean>(false)
        val initScroll: StateFlow<Boolean> = _initScroll

        private val _gridShortFormTitle = MutableStateFlow<String>("")
        val gridShortFormTitle: StateFlow<String> = _gridShortFormTitle

        private val _listMoreTitle = MutableStateFlow<String>("")
        val listMoreTitle: StateFlow<String> = _listMoreTitle

        private val _moreIndex = MutableStateFlow<Int>(0)
        val moreIndex: StateFlow<Int> get() = _moreIndex

        fun setGridShortFormTitle(title: String) {
            _gridShortFormTitle.value = title
        }

        fun setListItemMoreTitle(title: String) {
            _listMoreTitle.value = title
        }

        fun maxMoreIndex(viewType: ViewType): Int {
            when (viewType) {
                ViewType.PopularSearchShortForm -> return _popularShortsFormMoreList.value.videoSearchList.searchList.size
                ViewType.PopularLikeShortForm -> return _popularShortsFormMoreList.value.videoLikeList.likeList.size
                ViewType.PopularCommentShortForm -> return _popularShortsFormMoreList.value.videoCommentList.commentList.size
                ViewType.EditorPick -> return _editorPickMoreList.value.pickList.size
                ViewType.Recommend -> return _recommendMoreList.value.recommendList.size
                ViewType.ChannelSearchRanking -> return _channelMoreList.value.channelSearchList.searchList.size
                ViewType.ChannelLikeRanking -> return _channelMoreList.value.channelLikeList.likeList.size
                ViewType.SubscriptionRanking -> return _subscriptionMoreList.value.subscriptionList.size
                ViewType.SubscriptionRankingUp -> return _subscriptionUpMoreList.value.subscriptionUpList.size
                ViewType.RecentlyWatch -> return _recentlyWatchMoreList.value.size
                else -> Unit
            }
            return 0
        }

        fun setMorEVent(moreIndex: Int): Int {
            _moreIndex.value = moreIndex
            return _moreIndex.value
        }

        fun moreContent(
            viewType: ViewType,
            index: Int,
        ): MutableList<MainShortsModel>? {
            when (viewType) {
                ViewType.PopularSearchShortForm -> {
                    val list =
                        _popularShortsFormMoreList.value.videoSearchList.searchList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.PopularLikeShortForm -> {
                    val list =
                        _popularShortsFormMoreList.value.videoLikeList.likeList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.PopularCommentShortForm -> {
                    val list =
                        _popularShortsFormMoreList.value.videoCommentList.commentList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.EditorPick -> {
                    val list =
                        _editorPickMoreList.value.pickList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.Recommend -> {
                    val list =
                        _recommendMoreList.value.recommendList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.ChannelSearchRanking -> {
                    val list =
                        _channelMoreList.value.channelSearchList.searchList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.ChannelLikeRanking -> {
                    val list =
                        _channelMoreList.value.channelLikeList.likeList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.SubscriptionRanking -> {
                    val list =
                        _subscriptionMoreList.value.subscriptionList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                ViewType.SubscriptionRankingUp -> {
                    val list =
                        _subscriptionUpMoreList.value.subscriptionUpList
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환
                    RLog.d(
                        "hbungshin",
                        "size : ${list?.size}  oriinal size : ${_subscriptionUpMoreList.value.subscriptionUpList.size}",
                    )

                    list?.let { _currentDataList.value.addAll(it) }
                }
                ViewType.RecentlyWatch -> {
                    val list =
                        _recentlyWatchMoreList.value
                            .chunked(
                                INIT_MAX_SIZE,
                            ) // 리스트를 30개씩 나눔
                            .mapIndexed { i, chunk -> i to chunk } // 인덱스를 키로 사용
                            .toMap()[index] // Map으로 변환

                    list?.let { _currentDataList.value.addAll(it) }
                }

                else -> Unit
            }

            return null
        }

        fun setInitScroll(init: Boolean) {
            _initScroll.value = init
        }

        fun mainShortFormData(
            viewType: ViewType,
            items: Pair<MainShortFormList, Int>,
            recentlyWatchList: List<MainShortsModel>? = null,
        ) {
            when (viewType) {
                ViewType.PopularSearchShortForm -> {
                    _popularShortsFormMoreList.value = items.first.shortformVideoList
                    _currentDataList.value =
                        _popularShortsFormMoreList.value.videoSearchList.searchList
                            .subList(
                                0,
                                INIT_MAX_SIZE,
                            ).toMutableList()
                }

                ViewType.EditorPick -> {
                    _editorPickMoreList.value = items.first.editorPickList
                    _currentDataList.value =
                        _editorPickMoreList.value.pickList
                            .subList(0, INIT_MAX_SIZE)
                            .toMutableList()
                }

                ViewType.Recommend -> {
                    _recommendMoreList.value = items.first.shortformRecommendList
                    _currentDataList.value =
                        _recommendMoreList.value.recommendList
                            .subList(0, INIT_MAX_SIZE)
                            .toMutableList()
                }

                ViewType.ChannelSearchRanking -> {
                    _channelMoreList.value = items.first.channelVideoList
                    _currentDataList.value =
                        _channelMoreList.value.channelSearchList.searchList
                            .subList(0, INIT_MAX_SIZE)
                            .toMutableList()
                }

                ViewType.SubscriptionRanking -> {
                    _subscriptionMoreList.value = items.first.channelSubscriptionList
                    _currentDataList.value =
                        _subscriptionMoreList.value.subscriptionList
                            .subList(0, INIT_MAX_SIZE)
                            .toMutableList()
                }

                ViewType.SubscriptionRankingUp -> {
                    _subscriptionUpMoreList.value = items.first.channelSubscriptionUpList
                    _currentDataList.value =
                        _subscriptionUpMoreList.value.subscriptionUpList
                            .subList(0, INIT_MAX_SIZE)
                            .toMutableList()
                }
                ViewType.RecentlyWatch -> {
                    if (recentlyWatchList != null) {
                        _recentlyWatchMoreList.value = recentlyWatchList
                        _currentDataList.value =
                            if (recentlyWatchList.size < INIT_MAX_SIZE) {
                                recentlyWatchList.toMutableList()
                            } else {
                                recentlyWatchList
                                    .subList(
                                        0,
                                        INIT_MAX_SIZE,
                                    ).toMutableList()
                            }
                    }
                }

                else -> Unit
            }
        }

        fun mainTrendsShortsData(
            filterKey: String?,
            trendShorts: TrendsShortFormList? = null,
        ) {
            if (_trendShortsMoreList.value?.event_list?.isEmpty() == true) _trendShortsMoreList.value = trendShorts

            val dataList =
                _trendShortsMoreList.value
                    ?.event_list
                    ?.get(filterKey)
                    ?.toMutableList() ?: mutableListOf<MainShortsModel>()

            _currentDataList.value.clear()
            _currentDataList.value.addAll(dataList)
            _initScroll.value = true
            _moreIndex.value = 0
        }

        suspend fun popularShortFormFilter(filter: Int) {
            withContext(Dispatchers.IO) {
                if (filter == _currentFilter) return@withContext

                val dataList: MutableList<MainShortsModel> =
                    when (filter) {
                        0 -> {
                            val size = _popularShortsFormMoreList.value.videoSearchList.searchList.size
                            _popularShortsFormMoreList.value.videoSearchList.searchList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }

                        1 -> {
                            val size = _popularShortsFormMoreList.value.videoLikeList.likeList.size
                            _popularShortsFormMoreList.value.videoLikeList.likeList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }

                        2 -> {
                            val size =
                                _popularShortsFormMoreList.value.videoCommentList.commentList.size
                            _popularShortsFormMoreList.value.videoCommentList.commentList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }

                        else -> {
                            val size = _popularShortsFormMoreList.value.videoSearchList.searchList.size
                            _popularShortsFormMoreList.value.videoSearchList.searchList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }
                    }
                _currentFilter = filter
                _currentDataList.value.clear()
                _currentDataList.value.addAll(dataList)
                _initScroll.value = true
                _moreIndex.value = 0
            }
        }

        suspend fun popularChannelFilter(filter: Int) {
            withContext(Dispatchers.IO) {
                if (filter == _currentFilter) return@withContext
                val dataList: MutableList<MainShortsModel> =
                    when (filter) {
                        0 -> {
                            val size = _channelMoreList.value.channelSearchList.searchList.size
                            _channelMoreList.value.channelSearchList.searchList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }

                        1 -> {
                            val size = _channelMoreList.value.channelLikeList.likeList.size
                            _channelMoreList.value.channelLikeList.likeList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }

                        else -> {
                            val size = _channelMoreList.value.channelSearchList.searchList.size
                            _channelMoreList.value.channelSearchList.searchList
                                .subList(
                                    0,
                                    if (size < INIT_MAX_SIZE) size else INIT_MAX_SIZE,
                                ).toMutableList()
                        }
                    }
                _currentFilter = filter
                _currentDataList.value.clear()
                _currentDataList.value.addAll(dataList)
                _initScroll.value = true
                _moreIndex.value = 0
            }
        }

        fun getConvertViewType(viewType: ViewType): ViewType {
            val type =
                when (viewType) {
                    ViewType.PopularSearchShortForm -> ViewType.PopularSearchShortFormMore
                    ViewType.PopularLikeShortForm -> ViewType.PopularLikeShortFormMore
                    ViewType.PopularCommentShortForm -> ViewType.PopularCommentShortFormMore
                    ViewType.EditorPick -> ViewType.EditorPickMore
                    ViewType.Recommend -> ViewType.RecommendMore
                    ViewType.ChannelSearchRanking -> ViewType.ChannelSearchRankingMore
                    ViewType.ChannelLikeRanking -> ViewType.ChannelLikeRankingMore
                    ViewType.SubscriptionRanking -> ViewType.SubscriptionRankingMore
                    ViewType.SubscriptionRankingUpMore -> ViewType.SubscriptionRankingUpMore
                    ViewType.RecentlyWatch -> ViewType.RecentlyWatchMore
                    else -> ViewType.UNKWNONTYPE
                }
            return type
        }

        companion object {
            val TAG = "MainMoreViewModel"
            val INIT_MAX_SIZE = 30
        }
    }
