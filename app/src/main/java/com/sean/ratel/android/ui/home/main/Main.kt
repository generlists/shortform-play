package com.sean.ratel.android.ui.home.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.MAX_RECOMMEND_SIZE
import com.sean.ratel.android.data.dto.ChannelSubscriptionList
import com.sean.ratel.android.data.dto.ChannelSubscriptionUpList
import com.sean.ratel.android.data.dto.ChannelVideoSearchList
import com.sean.ratel.android.data.dto.EditorPickList
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.ShortFormVideoSearchList
import com.sean.ratel.android.data.dto.TopFiveList
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.home.main.itemview.AutoScrollImagePager
import com.sean.ratel.android.ui.home.main.itemview.EditorPickHorizontalList
import com.sean.ratel.android.ui.home.main.itemview.HomeRecommendList
import com.sean.ratel.android.ui.home.main.itemview.PopularShortFormPager
import com.sean.ratel.android.ui.home.main.itemview.RankingHorizontalScrollView
import com.sean.ratel.android.ui.home.main.itemview.RecentVideoWatchList
import com.sean.ratel.android.ui.home.main.itemview.TrendShortsList
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.utils.UIUtil.validationIndex
import kotlinx.coroutines.delay
import so.smartlab.common.ad.admob.data.model.AdMobBannerState

private const val TAG = "MainView"

@Suppress("ktlint:standard:function-naming")
@Composable
fun Main(
    modifier: Modifier,
    mainVideoViewModel: MainVideoViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
) {
    BackHandler(enabled = true) {
        mainVideoViewModel.navigator.finish()
    }

    val itemSize = remember { mainViewModel.mainShorts.value.second }
    val topFiveData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .topFiveList,
            )
        }
    val trendShortsData =
        remember {
            mutableStateOf(
                mainViewModel.trendsShorts.value,
            )
        }
    val editorPickData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .editorPickList,
            )
        }
    val shortFormSearchData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .shortformVideoList
                    .videoSearchList,
            )
        }
    val channelSearchData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .channelVideoList
                    .channelSearchList,
            )
        }
    val channelSubscriptionData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .channelSubscriptionList,
            )
        }
    val channelSubscriptionUpData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .channelSubscriptionUpList,
            )
        }
    val reCommendData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first.shortformRecommendList,
            )
        }

    val complete = remember { mutableStateOf(false) }

    val scrollPosition = rememberSaveable { mutableStateOf(0) }
    val scrollOffset = rememberSaveable { mutableStateOf(0) }

    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = scrollPosition.value,
            initialFirstVisibleItemScrollOffset = scrollOffset.value,
        )

    if (!complete.value) {
        MainShortFormView(
            modifier,
            itemSize,
            topFiveData.value,
            trendShortsData.value,
            editorPickData.value,
            shortFormSearchData.value,
            channelSearchData.value,
            channelSubscriptionData.value,
            channelSubscriptionUpData.value,
            reCommendData.value,
            mainViewModel,
            adViewModel,
            listState,
        )
    }
    LaunchedEffect(mainViewModel) {
        mainViewModel.setWatchVideoList()
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun MainShortFormView(
    modifier: Modifier,
    itemSize: Int,
    topFiveData: TopFiveList,
    trendShortsData: TrendsShortFormList,
    editorPickData: EditorPickList,
    shortFormSearchData: ShortFormVideoSearchList,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
    recommendShortFormData: RecommendList,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    listState: LazyListState,
) {
    Column(
        Modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            ShortsItemList(
                itemSize,
                topFiveData,
                trendShortsData,
                editorPickData,
                shortFormSearchData,
                channelSearchData,
                channelSubscriptionData,
                channelSubscriptionUpData,
                recommendShortFormData,
                mainViewModel,
                listState,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortsItemList(
    itemSize: Int,
    topFiveData: TopFiveList,
    trendShortsData: TrendsShortFormList,
    editorPickData: EditorPickList,
    shortFormSearchData: ShortFormVideoSearchList,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
    recommendShortFormData: RecommendList,
    viewModel: MainViewModel,
    listState: LazyListState,
) {
    val adFixedBannerState by viewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }

    val isFirstItemVisible by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset == 0 }
    }

    LaunchedEffect(isFirstItemVisible) {
        if (isFirstItemVisible) {
            // 첫 번째 아이템이 보일 때 처리할 작업
            delay(50)
            viewModel.setIsHomeVisible(false)
        }
    }
    when {
        adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
            adSize = (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
        }

        else -> {
            adSize = 0
        }
    }
    val size = itemSize - 2 + RemoteConfig.getRemoteConfigIntValue(MAX_RECOMMEND_SIZE)

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = adSize.dp),
//                .then(
//                    if (adBannerLoadingComplete.value.first == true) {
//                        Modifier.padding(bottom = adBannerLoadingComplete.value.second.dp)
//                    } else {
//                        Modifier
//                    },
//                ),
        // verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        var i = 0
        val targetIndexList = validationIndex(Destination.Home.Main.route, size)

        item {
            while (i < size) {
                var adPosition = -1
                targetIndexList.forEach { if (it == i) adPosition = it }

                if (i == 0) {
                    AutoScrollImagePager(viewModel, topFiveData)
                    Spacer(Modifier.height(8.dp))
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECENTLY_WATCH_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECENTLY_WATCH_ORDER) + 1

                ) {
                    RecentVideoWatchList(viewModel)
                    Spacer(Modifier.height(32.dp))
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.TRENDS_SHORTS_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.TRENDS_SHORTS_ORDER) + 1

                ) {
                    TrendShortsList(viewModel, trendShortsData)
                    Spacer(Modifier.height(32.dp))
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.POPULAR_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.POPULAR_ORDER) + 1
                ) {
                    PopularShortFormPager(viewModel, shortFormSearchData)
                    Spacer(Modifier.height(32.dp))
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.EDITOR_PICK_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.EDITOR_PICK_ORDER) + 1
                ) {
                    EditorPickHorizontalList(viewModel, editorPickData)
                    Spacer(Modifier.height(32.dp))
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.DAILY_RANKING_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.DAILY_RANKING_ORDER) + 1
                ) {
                    RankingHorizontalScrollView(
                        3,
                        viewModel,
                        channelSearchData,
                        channelSubscriptionData,
                        channelSubscriptionUpData,
                    )
                    Spacer(Modifier.height(32.dp))
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECOMMEND_SHORTFORM_ORDER)) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECOMMEND_SHORTFORM_ORDER) + 1
                ) {
                    HomeRecommendList(
                        mainViewModel = viewModel,
                        recommendList = recommendShortFormData,
                    )
                    Spacer(Modifier.height(32.dp))
                }
                i++
            }
        }
    }
}
