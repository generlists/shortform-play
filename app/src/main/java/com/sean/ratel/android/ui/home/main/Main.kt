package com.sean.ratel.android.ui.home.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.NativeAdCompose
import com.sean.ratel.android.ui.home.main.itemview.AutoScrollImagePager
import com.sean.ratel.android.ui.home.main.itemview.EditorPickHorizontalList
import com.sean.ratel.android.ui.home.main.itemview.HomeRecommendList
import com.sean.ratel.android.ui.home.main.itemview.PopularShortFormPager
import com.sean.ratel.android.ui.home.main.itemview.RankingHorizontalScrollView
import com.sean.ratel.android.ui.home.main.itemview.RecentVideoWatchList
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.utils.UIUtil.validationIndex
import kotlinx.coroutines.delay

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
                editorPickData,
                shortFormSearchData,
                channelSearchData,
                channelSubscriptionData,
                channelSubscriptionUpData,
                recommendShortFormData,
                mainViewModel,
                adViewModel,
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
    editorPickData: EditorPickList,
    shortFormSearchData: ShortFormVideoSearchList,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
    recommendShortFormData: RecommendList,
    viewModel: MainViewModel,
    adViewModel: AdViewModel,
    listState: LazyListState,
) {
    val adFail = adViewModel.adNativeFail.collectAsState().value

    val isFirstItemVisible by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset == 0 }
    }

    LaunchedEffect(isFirstItemVisible) {
        if (isFirstItemVisible) {
            // 첫 번째 아이템이 보일 때 처리할 작업
            delay(500)
            viewModel.setIsHomeVisible(false)
        }
    }
    val size = itemSize - 2 + RemoteConfig.getRemoteConfigIntValue(MAX_RECOMMEND_SIZE)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        var i = 0
        val targetIndexList = validationIndex(Destination.Home.Main.route, size)

        item {
            while (i < size) {
                var adPosition = -1
                targetIndexList.forEach { if (it == i) adPosition = it }

                if (i == 0) {
                    AutoScrollImagePager(viewModel, topFiveData)
                }
                if (i == adPosition && adFail == null) {
                    NativeAdCompose(adViewModel)
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECENTLY_WATCH_ORDER) && adFail != null) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECENTLY_WATCH_ORDER) + 1 &&
                    adFail == null
                ) {
                    RecentVideoWatchList(viewModel)
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.POPULAR_ORDER) && adFail != null) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.POPULAR_ORDER) + 1 &&
                    adFail == null
                ) {
                    PopularShortFormPager(viewModel, shortFormSearchData)
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.EDITOR_PICK_ORDER) && adFail != null) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.EDITOR_PICK_ORDER) + 1 &&
                    adFail == null
                ) {
                    EditorPickHorizontalList(viewModel, editorPickData)
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.DAILY_RANKING_ORDER) && adFail != null) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.DAILY_RANKING_ORDER) + 1 &&
                    adFail == null
                ) {
                    RankingHorizontalScrollView(
                        3,
                        viewModel,
                        channelSearchData,
                        channelSubscriptionData,
                        channelSubscriptionUpData,
                    )
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECOMMEND_SHORTFORM_ORDER) && adFail != null) ||
                    i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECOMMEND_SHORTFORM_ORDER) + 1 &&
                    adFail == null
                ) {
                    HomeRecommendList(
                        mainViewModel = viewModel,
                        recommendList = recommendShortFormData,
                    )
                }
                i++
            }
        }
    }
}
