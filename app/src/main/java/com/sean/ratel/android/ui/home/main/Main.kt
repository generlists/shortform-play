package com.sean.ratel.android.ui.home.main

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.sean.ratel.android.data.dto.TopicList
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdBannerView
import com.sean.ratel.android.ui.home.BillingViewModel
import com.sean.ratel.android.ui.home.main.itemview.AutoScrollImagePager
import com.sean.ratel.android.ui.home.main.itemview.EditorPickHorizontalList
import com.sean.ratel.android.ui.home.main.itemview.FilterCreateBanner
import com.sean.ratel.android.ui.home.main.itemview.FilterHiddenNote
import com.sean.ratel.android.ui.home.main.itemview.FilterStatusBar
import com.sean.ratel.android.ui.home.main.itemview.HomeRecommendList
import com.sean.ratel.android.ui.home.main.itemview.PopularShortFormPager
import com.sean.ratel.android.ui.home.main.itemview.RankingHorizontalScrollView
import com.sean.ratel.android.ui.home.main.itemview.RecentVideoWatchList
import com.sean.ratel.android.ui.home.main.itemview.TopicCardPager
import com.sean.ratel.android.ui.home.main.itemview.TrendShortsList
import com.sean.ratel.android.ui.my.AccountViewModel
import com.sean.ratel.android.ui.navigation.Destination
import kotlinx.coroutines.delay

private const val TAG = "MainView"

@Suppress("ktlint:standard:function-naming")
@Composable
fun Main(
    modifier: Modifier,
    mainVideoViewModel: MainVideoViewModel,
    mainViewModel: MainViewModel,
    billingViewModel: BillingViewModel,
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
    val topicData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .topicList,
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
            topicData.value,
            trendShortsData.value,
            editorPickData.value,
            shortFormSearchData.value,
            channelSearchData.value,
            channelSubscriptionData.value,
            channelSubscriptionUpData.value,
            reCommendData.value,
            mainViewModel,
            billingViewModel,
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
    topicList: TopicList,
    trendShortsData: TrendsShortFormList,
    editorPickData: EditorPickList,
    shortFormSearchData: ShortFormVideoSearchList,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
    recommendShortFormData: RecommendList,
    mainViewModel: MainViewModel,
    billingViewModel: BillingViewModel,
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
                topicList,
                trendShortsData,
                editorPickData,
                shortFormSearchData,
                channelSearchData,
                channelSubscriptionData,
                channelSubscriptionUpData,
                recommendShortFormData,
                mainViewModel,
                billingViewModel,
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
    topicList: TopicList,
    trendShortsData: TrendsShortFormList,
    editorPickData: EditorPickList,
    shortFormSearchData: ShortFormVideoSearchList,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
    recommendShortFormData: RecommendList,
    viewModel: MainViewModel,
    billingViewModel: BillingViewModel,
    listState: LazyListState,
) {
    val adFixedBannerState by viewModel.fixedBannerState.collectAsState()
    val isAdRemove by billingViewModel.isAdRemoved.collectAsStateWithLifecycle()
    var adSize by remember { mutableStateOf(64) }
    val activity = LocalActivity.current

    // 내 필터: 맨 위 바/배너와 "빼고 보여드렸어요" 줄(시안 B1·A1). 상태는 마이 탭과 같은 AccountViewModel 에서 읽는다
    val accountViewModel =
        remember(activity) { (activity as? ComponentActivity)?.let { ViewModelProvider(it)[AccountViewModel::class.java] } }
    val filterState = accountViewModel?.filterState?.collectAsStateWithLifecycle()?.value
    val savedBannerDismissed = accountViewModel?.homeBannerDismissed?.collectAsStateWithLifecycle()?.value ?: true

    // 닫기를 누르면 저장이 끝나길 기다리지 않고 바로 숨긴다(저장은 뒤에서 한다)
    var bannerClosedNow by remember { mutableStateOf(false) }
    val bannerDismissed = savedBannerDismissed || bannerClosedNow
    val hiddenCount by viewModel.hiddenCount.collectAsStateWithLifecycle()
    val filterActive = filterState?.filter?.hasRules == true

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
//    when {
//        adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
//            adSize = (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
//        }
//
//        else -> {
//            adSize = 0
//        }
//    }
    val size = itemSize - 2 + RemoteConfig.getRemoteConfigIntValue(MAX_RECOMMEND_SIZE)

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        var i = 0

        item {
            // "필터로 N개를 빼고 보여드렸어요" 는 처음 나오는 쇼츠 목록 섹션 바로 아래에 한 번만 붙인다
            var noteShown = false
            val showNote = filterActive && hiddenCount > 0

            @Composable
            fun HiddenNoteOnce() {
                if (showNote && !noteShown) {
                    noteShown = true
                    FilterHiddenNote(hiddenCount, onViewHidden = { viewModel.navigator.navigateTo(Destination.HiddenVideos.route) })
                    Spacer(Modifier.height(24.dp))
                }
            }

            while (i < size) {
                if (i == 0) {
                    AutoScrollImagePager(viewModel, topFiveData)
                    Spacer(Modifier.height(16.dp))
                    if (filterActive && filterState != null) {
                        FilterStatusBar(
                            topics = filterState.filter.topics.size,
                            keywords = filterState.filter.keywords.size,
                            onManage = {
                                accountViewModel?.track(GAAccountAnalytics.Screen.HOME, GAAccountAnalytics.Event.FILTER_BAR_MANAGE)
                                viewModel.navigator.navigateTo(Destination.FilterEdit.route)
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                    } else if (!filterActive && !bannerDismissed) {
                        LaunchedEffect(Unit) {
                            accountViewModel?.track(
                                GAAccountAnalytics.Screen.HOME,
                                GAAccountAnalytics.Event.FILTER_BANNER_VIEW,
                                GAAccountAnalytics.Action.VIEW,
                            )
                        }
                        FilterCreateBanner(
                            onCreate = {
                                accountViewModel?.track(GAAccountAnalytics.Screen.HOME, GAAccountAnalytics.Event.FILTER_BANNER_CREATE)
                                viewModel.navigator.navigateTo(Destination.Onboarding.route)
                            },
                            onLater = {
                                bannerClosedNow = true
                                accountViewModel?.dismissHomeBanner()
                            },
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                if ((
                        i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.AD_BANNER_ORDER) &&
                            RemoteConfig.getRemoteConfigBooleanValue(RemoteConfig.BANNER_AD_VISIBILITY)
                    )
                ) {
                    AdBannerView(
                        activity,
                        Destination.Home.Main.route,
                        AdBannerLocation.BOTTOM,
                        billingViewModel,
                        homeMainViewModel = viewModel,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.TOPIC_LIST_ORDER))) {
                    TopicCardPager(viewModel, billingViewModel, topicList)
                    Spacer(Modifier.height(32.dp))
                    HiddenNoteOnce()
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECENTLY_WATCH_ORDER))) {
                    RecentVideoWatchList(viewModel, billingViewModel)
                    Spacer(Modifier.height(32.dp))
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.TRENDS_SHORTS_ORDER))) {
                    TrendShortsList(viewModel, billingViewModel, trendShortsData)
                    Spacer(Modifier.height(32.dp))
                    HiddenNoteOnce()
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.POPULAR_ORDER))) {
                    PopularShortFormPager(viewModel, billingViewModel, shortFormSearchData)
                    Spacer(Modifier.height(32.dp))
                    HiddenNoteOnce()
                }

                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.EDITOR_PICK_ORDER))) {
                    EditorPickHorizontalList(viewModel, billingViewModel, editorPickData)
                    Spacer(Modifier.height(32.dp))
                    HiddenNoteOnce()
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.DAILY_RANKING_ORDER))) {
                    RankingHorizontalScrollView(
                        3,
                        viewModel,
                        billingViewModel,
                        channelSearchData,
                        channelSubscriptionData,
                        channelSubscriptionUpData,
                    )
                    Spacer(Modifier.height(32.dp))
                }
                if ((i == RemoteConfig.getRemoteConfigIntValue(RemoteConfig.RECOMMEND_SHORTFORM_ORDER))) {
                    HomeRecommendList(
                        mainViewModel = viewModel,
                        billingViewModel = billingViewModel,
                        recommendList = recommendShortFormData,
                    )
                    Spacer(Modifier.height(32.dp))
                }
                i++
            }
        }
    }
}
