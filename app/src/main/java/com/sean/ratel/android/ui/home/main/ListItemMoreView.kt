package com.sean.ratel.android.ui.home.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.end.BottomSeekBar
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.ComposeUtil.isAtBottom
import com.sean.ratel.android.utils.TimeUtil.timeToFloat
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale
import com.sean.ratel.android.utils.UIUtil.formatNumberWithCommas
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemMoreView(
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
) {
    val viewType = mainViewModel.viewType.collectAsState()
    val mainShorts = mainViewModel.mainShorts.collectAsState()
    val moreIndex = moreViewModel.moreIndex.collectAsState()
    val recentlyWatchVideo = mainViewModel.watchVideoList.collectAsState()

    if (moreIndex.value == 0) {
        moreViewModel.mainShortFormData(
            viewType.value,
            mainShorts.value,
            recentlyWatchVideo.value,
        )
    }

    ListItemDisplayUi(viewType.value, adViewModel, mainViewModel, moreViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemDisplayUi(
    viewType: ViewType,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
) {
    var filterAction by remember { mutableIntStateOf(-1) }
    val initScroll = moreViewModel.initScroll.collectAsState()
    val route = mainViewModel.moreButtonClicked.value
    val title = moreViewModel.listMoreTitle.collectAsState()

    val filterVisiable =
        remember { mutableStateOf(viewType == ViewType.ChannelSearchRanking || viewType == ViewType.ChannelLikeRanking) }

    val currentData = moreViewModel.currentDataList.collectAsState()

    val channelStringList =
        listOf(
            stringResource(R.string.main_more_search),
            stringResource(R.string.main_more_like),
        )
    val channelRankingTitle = moreViewModel.channelMoreList.collectAsState()
    val subscriptionTitle = moreViewModel.subscriptionMoreList.collectAsState()
    val subscriptionUpTitle = moreViewModel.subscriptionUpMoreList.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()

    Scaffold(
        Modifier.padding(insetPaddingValue),
        topBar = {
            TopNavigationBar(
                titleString = title.value,
                historyBack = { mainViewModel.runNavigationBack() },
                isShareButton = false,
                runSetting = {},
                filterButton = filterVisiable.value,
                onFilterChange = { newFilterAction ->
                    filterAction = newFilterAction
                },
                items = channelStringList,
            )
        },
        containerColor = APP_BACKGROUND,
    ) { innerPadding ->

        val bottomBarHeight = rememberSaveable { adViewModel.bottomBarHeight.value }
        val adBannerSize =
            adViewModel.adBannerLoadingCompleteAndGetAdSize
                .collectAsState()
                .value.second
        var moreLoading by remember { mutableStateOf(false) }
        val scrollPosition = rememberSaveable { mutableStateOf(0) }
        val scrollOffset = rememberSaveable { mutableStateOf(0) }

        val listState =
            rememberLazyListState(
                initialFirstVisibleItemIndex = scrollPosition.value,
                initialFirstVisibleItemScrollOffset = scrollOffset.value,
            )

        // 초기 로딩시
        if (title.value.isEmpty()) {
            SetTitle(
                moreViewModel,
                mainViewModel,
                channelRankingTitle.value.channelSearchList.title,
                channelRankingTitle.value.channelLikeList.title,
                subscriptionTitle.value.title,
                subscriptionUpTitle.value.title,
            )
        }

        if (currentData.value.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (viewType == ViewType.RecentlyWatch) {
                    Column(Modifier.fillMaxSize()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(
                                    start = 5.dp,
                                    top = 5.dp,
                                    end = 5.dp,
                                ).background(APP_BACKGROUND),
                        ) {
                            Text(
                                text = stringResource(R.string.main_recent_watch_video_max_1),
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(start = 5.dp, top = 5.dp),
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = APP_TEXT_COLOR,
                            )
                            Text(
                                text = stringResource(R.string.main_recent_watch_video_max_2),
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(start = 5.dp, bottom = 5.dp),
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                color = APP_TEXT_COLOR,
                            )
                        }

                        RecentlyWatchItemList(
                            viewType,
                            route ?: Destination.Home.Main.RecommendMore.route,
                            currentData.value,
                            moreViewModel,
                            mainViewModel,
                            adViewModel,
                            loading = { load ->
                                moreLoading = load
                            },
                            listState,
                        )
                    }
                } else {
                    ListItemList(
                        viewType,
                        route ?: Destination.Home.Main.RankingChannelMore.route,
                        currentData.value,
                        moreViewModel,
                        mainViewModel,
                        adViewModel,
                        loading = { load ->
                            moreLoading = load
                        },
                        listState,
                    )
                }
            }
        }
        if (initScroll.value) {
            LaunchedEffect(Unit) {
                listState.scrollToItem(0)
                moreViewModel.setInitScroll(false)
            }
        }

        if (moreLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = (adBannerSize + bottomBarHeight).dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.BottomCenter,
            ) {
                CircularProgressIndicator(
                    Modifier
                        .size(18.dp)
                        .padding(1.dp),
                    strokeWidth = 3.dp,
                    color = APP_BACKGROUND,
                )
            }
        }

        if (viewType == ViewType.SubscriptionRanking || viewType == ViewType.SubscriptionRankingUp) {
            filterAction = -1
        } else {
            FilterList(filterAction, mainViewModel, moreViewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FilterList(
    filterAction: Int,
    mainViewModel: MainViewModel,
    viewModel: MainMoreViewModel,
) {
    val channelRankingTitle = viewModel.channelMoreList.collectAsState()
    val subscriptionTitle = viewModel.subscriptionMoreList.collectAsState()
    val subscriptionUpTitle = viewModel.subscriptionUpMoreList.collectAsState()

    SetTitle(
        viewModel,
        mainViewModel,
        channelRankingTitle.value.channelSearchList.title,
        channelRankingTitle.value.channelLikeList.title,
        subscriptionTitle.value.title,
        subscriptionUpTitle.value.title,
    )
    if (filterAction == -1) return

    LaunchedEffect(filterAction) {
        when (filterAction) {
            0 -> mainViewModel.setViewType(ViewType.ChannelSearchRanking)
            1 -> mainViewModel.setViewType(ViewType.ChannelLikeRanking)
        }
        viewModel.popularChannelFilter(filterAction)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SetTitle(
    moreViewModel: MainMoreViewModel,
    mainViewModel: MainViewModel,
    channelSearchRanking: String,
    channelLikeRanking: String,
    subscription: String,
    subscripionUp: String,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.viewType.collect {
            when (it) {
                ViewType.ChannelSearchRanking ->
                    moreViewModel.setListItemMoreTitle(
                        String.format(
                            "%s",
                            "$channelSearchRanking(${context.getString(R.string.main_more_search)})",
                        ),
                    )

                ViewType.ChannelLikeRanking ->
                    moreViewModel.setListItemMoreTitle(
                        (
                            String.format(
                                "%s",
                                "$channelLikeRanking(${context.getString(R.string.main_more_like)})",
                            )
                        ),
                    )

                ViewType.SubscriptionRanking ->
                    moreViewModel.setListItemMoreTitle(subscription)

                ViewType.SubscriptionRankingUp ->
                    moreViewModel.setListItemMoreTitle(subscripionUp)

                ViewType.RecentlyWatch ->
                    moreViewModel.setListItemMoreTitle(context.getString(R.string.main_recent_watch_video))

                else -> moreViewModel.setListItemMoreTitle(channelSearchRanking)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemList(
    viewType: ViewType,
    route: String,
    items: List<MainShortsModel>,
    moreViewModel: MainMoreViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val index = moreViewModel.moreIndex.collectAsState()
    val isFirstItemVisible by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset == 0 }
    }

    val isAtBottom = listState.isAtBottom()
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(isFirstItemVisible) {
        if (isFirstItemVisible) {
            // 첫 번째 아이템이 보일 때 처리할 작업
            delay(500)
            mainViewModel.setIsHomeVisible(false)
        }
    }

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            if (moreViewModel.maxMoreIndex(viewType) > index.value) {
                loading(true)
                moreViewModel.setMorEVent(index.value)
            }

            moreViewModel.moreIndex.collectLatest { newValue ->
                RLog.d(
                    "hbungshin",
                    "Received moreIndex update: $newValue, maxindex : ${
                        moreViewModel.maxMoreIndex(
                            viewType,
                        )
                    }",
                )
                if (newValue > 0 && moreViewModel.maxMoreIndex(viewType) >= newValue) {
                    moreViewModel.moreContent(viewType, newValue)
                    coroutine.launch {
                        delay(500)
                        loading(false)
                        listState.animateScrollBy(50f)
                    }
                } else {
                    loading(false)
                }
            }
        } else {
            loading(false)
        }
    }
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(APP_BACKGROUND),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.main_rank_order),
                    Modifier
                        .padding(5.dp)
                        .weight(0.1f)
                        .wrapContentSize(),
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color.White,
                )
                Box(Modifier.wrapContentSize().weight(0.3f), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.main_rank_channel),
                        Modifier.wrapContentSize(),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = Color.White,
                    )
                }

                Row(
                    Modifier
                        .wrapContentSize()
                        .weight(0.2f),
                ) {
                    Text(
                        if (viewType == ViewType.ChannelSearchRanking) {
                            stringResource(
                                R.string.main_rank_search,
                            )
                        } else {
                            stringResource(R.string.main_rank_subscription)
                        },
                        Modifier
                            .wrapContentSize()
                            .weight(1f),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = Color.White,
                    )
                    if (viewType == ViewType.SubscriptionRankingUp) {
                        Text(
                            stringResource(R.string.main_rank_new),
                            Modifier
                                .wrapContentSize()
                                .weight(1f),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = APP_TEXT_COLOR,
                        )
                    }
                }
            }
        }

        items.let {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (adBannerLoadingComplete.value.first) {
                            Modifier
                                .padding(
                                    bottom = adBannerLoadingComplete.value.second.dp + insetPaddingValue.value.dp,
                                ).background(APP_BACKGROUND)
                        } else {
                            Modifier
                        },
                    ),
                state = listState,
            ) {
                items(count = items.size) { index ->
                    ListItem(viewType, route, index, items[index], mainViewModel, moreViewModel)
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItem(
    viewType: ViewType,
    route: String,
    index: Int,
    item: MainShortsModel?,
    mainViewModel: MainViewModel?,
    moreViewModel: MainMoreViewModel?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(APP_BACKGROUND)
            .clickable {
                mainViewModel?.goEndContent(
                    route,
                    viewType,
                    index,
                )
                mainViewModel?.sendGALog(
                    Event.SCREEN_VIEW,
                    Destination.YouTube.dynamicRoute(item?.shortsChannelModel?.channelId ?: ""),
                    moreViewModel?.getConvertViewType(viewType),
                    item?.shortsChannelModel?.channelId,
                    item?.shortsVideoModel?.videoId,
                )
            }.padding(start = 7.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val channel = item?.shortsChannelModel

        Text(
            text = getRankingOrder(index),
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.wrapContentSize().weight(0.1f),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
        )
        Row(
            Modifier.wrapContentSize().weight(0.3f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkImage(
                url = channel?.channelThumbNail ?: "",
                contentDescription = null,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .width(24.dp)
                        .height(24.dp),
                ContentScale.Fit,
                R.drawable.ic_play_icon,
                R.drawable.ic_play_icon,
                R.drawable.ic_play_icon,
            )
            Text(
                text =
                    channel?.channelTitle ?: "BTS",
                Modifier
                    .padding(start = 5.dp, top = 5.dp, bottom = 5.dp)
                    .width(200.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .weight(0.2f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (viewType == ViewType.ChannelSearchRanking) {
                    String.format(
                        "%s",
                        formatNumberByLocale(
                            (channel?.viewCount ?: "11334567").toLong(),
                        ),
                    )
                } else {
                    String.format(
                        "%s",
                        formatNumberByLocale(
                            (channel?.subscriberCount ?: "123455").toLong(),
                        ),
                    )
                },
                Modifier
                    .wrapContentSize()
                    .weight(1.0f),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = APP_TEXT_COLOR,
            )
            if (viewType == ViewType.SubscriptionRankingUp) {
                Text(
                    formatNumberWithCommas(channel?.subscriptionUp?.toLong() ?: 111111L),
                    Modifier
                        .wrapContentSize()
                        .weight(1.0f),
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = APP_TEXT_COLOR,
                )
            }
        }
    }
}

private fun getRankingOrder(index: Int): String {
    if (index == 0) {
        return "\uD83E\uDD47"
    } else if (index == 1) {
        return "\uD83E\uDD48"
    } else if (index == 2) {
        return "\uD83E\uDD49"
    } else {
        return "${(index + 1)}."
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun RecentlyWatchItemList(
    viewType: ViewType,
    route: String,
    items: List<MainShortsModel>?,
    moreViewModel: MainMoreViewModel,
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val index = moreViewModel.moreIndex.collectAsState()
    val isFirstItemVisible by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset == 0 }
    }
    Log.d(
        "hbungshin",
        "adBannerLoadingComplete : ${adBannerLoadingComplete.value.second} insetPaddingValue : ${insetPaddingValue.value.dp}",
    )
    val isAtBottom = listState.isAtBottom()
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(isFirstItemVisible) {
        if (isFirstItemVisible) {
            // 첫 번째 아이템이 보일 때 처리할 작업
            delay(500)
            mainViewModel.setIsHomeVisible(false)
        }
    }
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            if (moreViewModel.maxMoreIndex(viewType) > index.value) {
                loading(true)
                moreViewModel.setMorEVent(index.value + 1)
            }

            moreViewModel.moreIndex.collectLatest { newValue ->
                RLog.d(
                    "hbungshin",
                    "Received moreIndex update: $newValue, maxindex : ${
                        moreViewModel.maxMoreIndex(
                            viewType,
                        )
                    }",
                )
                if (newValue > 0 && moreViewModel.maxMoreIndex(viewType) >= newValue) {
                    moreViewModel.moreContent(viewType, newValue)
                    coroutine.launch {
                        delay(500)
                        loading(false)
                        listState.animateScrollBy(50f)
                    }
                } else {
                    loading(false)
                }
            }
        } else {
            loading(false)
        }
    }
    items?.let {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 5.dp, bottom = 5.dp)
                .then(
                    if (adBannerLoadingComplete.value.first) {
                        Modifier.padding(bottom = adBannerLoadingComplete.value.second.dp + insetPaddingValue.value.dp)
                    } else {
                        Modifier
                    },
                ),
            state = listState,
        ) {
            items(count = items.size) { index ->
                RecentlyWatchItems(
                    viewType,
                    route,
                    index,
                    items[index],
                    mainViewModel,
                    moreViewModel,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun RecentlyWatchItems(
    viewType: ViewType,
    route: String,
    index: Int,
    item: MainShortsModel?,
    mainViewModel: MainViewModel?,
    moreViewModel: MainMoreViewModel?,
) {
    val duration = timeToFloat(item?.shortsVideoModel?.duration ?: "00:01")
    val progress = (item?.saveTime ?: 0.1f) / duration

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 2.dp, bottom = 2.dp)
                .background(APP_BACKGROUND)
                .clickable {
                    mainViewModel?.goEndContent(
                        route,
                        viewType,
                        index,
                    )
                    mainViewModel?.sendGALog(
                        Event.SCREEN_VIEW,
                        Destination.YouTube.dynamicRoute(item?.shortsChannelModel?.channelId ?: ""),
                        moreViewModel?.getConvertViewType(viewType),
                        item?.shortsChannelModel?.channelId,
                        item?.shortsVideoModel?.videoId,
                    )
                },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .wrapContentHeight()
                .weight(0.7f),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Box(
                Modifier
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                ) {
                    NetworkImage(
                        url = item?.shortsVideoModel?.thumbNail ?: "",
                        contentDescription = null,
                        modifier =
                            Modifier
                                .aspectRatio(1.7f)
                                .wrapContentHeight(),
                    )
                }
            }

            Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.End) {
                Box(
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(top = 5.dp, bottom = 7.dp, end = 7.dp)
                            .background(
                                brush =
                                    Brush.horizontalGradient(
                                        colors =
                                            listOf(
                                                // 진한 검은색
                                                Color.LightGray.copy(alpha = 0.3f),
                                                // 투명
                                                Color.Black,
                                            ),
                                    ),
                            ),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    Text(
                        text = item?.shortsVideoModel?.duration ?: "00:00",
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White,
                        style =
                            TextStyle(
                                shadow =
                                    Shadow(
                                        color = Color.Black,
                                        // 그림자의 위치 (x, y)
                                        offset = Offset(2f, 2f),
                                        // 그림자의 흐림 정도
                                        blurRadius = 4f,
                                    ),
                            ),
                    )
                }
                BottomSeekBar(
                    progress = progress,
                    onSeekChanged = { _ ->
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(start = 5.dp, end = 7.dp),
        ) {
            Text(
                text = item?.shortsVideoModel?.title ?: "title",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NetworkImage(
                    url = item?.shortsChannelModel?.channelThumbNail ?: "",
                    contentDescription = null,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .width(24.dp)
                            .height(24.dp),
                    ContentScale.Fit,
                    R.drawable.ic_play_icon,
                    R.drawable.ic_play_icon,
                    R.drawable.ic_play_icon,
                )
                Text(
                    item?.shortsChannelModel?.channelTitle ?: "title",
                    // 한 줄로 제한
                    maxLines = 1,
                    // 말줄임표 처리
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(start = 5.dp, top = 5.dp, bottom = 5.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun RankingPreView() {
    RatelappTheme {
        // TitleArea("인기 숏폼(조회순)")
        // HorizontalScrollView(imageUrls)
//        ListItemList(
//            ViewType.ChannelSearchRanking,
//            Destination.Home.Main.RankingChannelMore.route,
//            listOf(MainShortsModel()),
//            null,
//            null,
//            null,
//            { } , rememberLazyListState()
//        )
        // RecentlyWatchItems(null,null,)
    }
}
