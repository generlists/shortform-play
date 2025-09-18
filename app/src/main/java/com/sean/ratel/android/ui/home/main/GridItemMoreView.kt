package com.sean.ratel.android.ui.home.main

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.REMAIN_AD_MARGIN
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.ShortFormBottomSheetDialog
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.progress.TrendShortsMenuButton
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.utils.ComposeUtil.isAtBottom
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridItemMoreView(
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
) {
    val viewType = mainViewModel.viewType.collectAsState()
    val mainShorts = mainViewModel.mainShorts.collectAsState()
    val trendShorts = mainViewModel.trendsShorts.collectAsState()
    val moreTrendShortsKey = mainViewModel.moreTrendShortsKey.collectAsState()
    val moreIndex = moreViewModel.moreIndex.collectAsState()

    if (moreIndex.value == 0) {
        if (viewType.value == ViewType.TrendShortsMore) {
            moreViewModel.mainTrendsShortsData(moreTrendShortsKey.value, trendShorts.value)
        } else {
            moreViewModel.mainShortFormData(
                viewType.value,
                mainShorts.value,
                null,
            )
        }
    }

    GridDisplayUi(viewType.value, adViewModel, mainViewModel, moreViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridDisplayUi(
    viewType: ViewType,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
) {
    var filterAction by remember { mutableIntStateOf(-1) }
    val initScroll = moreViewModel.initScroll.collectAsState()
    val title = moreViewModel.gridShortFormTitle.collectAsState()
    val popularTitle = moreViewModel.popularShortsFormMoreList.collectAsState()
    val editorTitle = moreViewModel.editorPickMoreList.collectAsState()
    val recommendTitle = moreViewModel.recommendMoreList.collectAsState()
    val trendShortsTitle = moreViewModel.trendShortsMoreList.collectAsState()
    val trendShortsKeys = trendShortsTitle.value?.event_list?.keys
    val moreTrendShortsKey = mainViewModel.moreTrendShortsKey.collectAsState()

    val filterVisiable =
        remember {
            mutableStateOf(
                viewType == ViewType.PopularSearchShortForm ||
                    viewType == ViewType.PopularLikeShortForm ||
                    viewType == ViewType.PopularCommentShortForm ||
                    viewType == ViewType.ChannelSearchRanking,
            )
        }

    val currentData = moreViewModel.currentDataList.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()

    val menuStringList =
        listOf(
            stringResource(R.string.main_more_search),
            stringResource(R.string.main_more_like),
            stringResource(R.string.main_more_comment),
        )

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
                items = menuStringList,
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
        val scrollPosition = remember { mutableStateOf(0) }
        val scrollOffset = remember { mutableStateOf(0) }
        var showBottomSheet by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf<String?>(moreTrendShortsKey.value) }

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
                popularTitle = popularTitle.value.videoSearchList.title,
                editorTitle = editorTitle.value.title,
                recommendTitle = recommendTitle.value.title,
                trendShortsTitle = "🔥${stringResource(R.string.main_trends_shorts_title)}",
            )
        }

        if (currentData.value.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                GridItemView(
                    currentData.value,
                    mainViewModel,
                    moreViewModel,
                    adViewModel,
                    loading = { load ->
                        moreLoading = load
                    },
                    listState,
                )
                if (viewType == ViewType.TrendShortsMore) {
                    Box(Modifier.wrapContentSize()) {
                        TrendShortsMenuButton(adViewModel, Modifier, true, onclick = {
                            showBottomSheet = true
                        })
                    }
                }
            }
        }
        if (showBottomSheet) {
            ShortFormBottomSheetDialog(
                items = trendShortsKeys?.toTypedArray(),
                selectedValue = selected,
                onSelect = { value ->
                    selected = value
                    showBottomSheet = false
                    moreViewModel.mainTrendsShortsData(selected)
                },
                onDismiss = { showBottomSheet = false },
            )
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
        if (viewType == ViewType.Recommend || viewType == ViewType.EditorPick || viewType == ViewType.TrendShortsMore) {
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
    moreViewModel: MainMoreViewModel,
) {
    val popularTitle = moreViewModel.popularShortsFormMoreList.collectAsState()
    val editorTitle = moreViewModel.editorPickMoreList.collectAsState()
    val recommendTitle = moreViewModel.recommendMoreList.collectAsState()

    SetTitle(
        moreViewModel,
        mainViewModel,
        popularTitle.value.videoSearchList.title,
        editorTitle.value.title,
        recommendTitle.value.title,
        "🔥${stringResource(R.string.main_trends_shorts_title)}",
    )
    if (filterAction == -1) return

    LaunchedEffect(filterAction) {
        when (filterAction) {
            0 -> {
                mainViewModel.setViewType(ViewType.PopularSearchShortForm)
            }

            1 -> {
                mainViewModel.setViewType(ViewType.PopularLikeShortForm)
            }

            2 -> {
                mainViewModel.setViewType(ViewType.PopularCommentShortForm)
            }
        }

        moreViewModel.popularShortFormFilter(filterAction)
    }
    SetTitle(
        moreViewModel,
        mainViewModel,
        popularTitle.value.videoSearchList.title,
        editorTitle.value.title,
        recommendTitle.value.title,
        "🔥${stringResource(R.string.main_trends_shorts_title)}",
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SetTitle(
    moreViewModel: MainMoreViewModel,
    mainViewModel: MainViewModel,
    popularTitle: String,
    editorTitle: String,
    recommendTitle: String,
    trendShortsTitle: String,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        mainViewModel.viewType.collect {
            when (it) {
                ViewType.PopularSearchShortForm ->
                    moreViewModel.setGridShortFormTitle(
                        String.format(
                            "%s",
                            "$popularTitle(${context.getString(R.string.main_more_search)})",
                        ),
                    )

                ViewType.PopularLikeShortForm ->
                    moreViewModel.setGridShortFormTitle(
                        (
                            String.format(
                                "%s",
                                "$popularTitle(${context.getString(R.string.main_more_like)})",
                            )
                        ),
                    )

                ViewType.PopularCommentShortForm ->
                    moreViewModel.setGridShortFormTitle(
                        (
                            String.format(
                                "%s",
                                "$popularTitle(${context.getString(R.string.main_more_comment)})",
                            )
                        ),
                    )

                ViewType.EditorPick ->
                    moreViewModel.setGridShortFormTitle(editorTitle)

                ViewType.Recommend ->
                    moreViewModel.setGridShortFormTitle(recommendTitle)

                ViewType.TrendShortsMore ->
                    moreViewModel.setGridShortFormTitle(trendShortsTitle)

                else -> moreViewModel.setGridShortFormTitle(popularTitle)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridItemView(
    data: List<MainShortsModel>,
    mainViewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
    adViewModel: AdViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    // 로딩이 끝나면 ShortsItemList 표시
    Column(
        Modifier
            .fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            GridItemList(data, mainViewModel, moreViewModel, adViewModel, loading, listState)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridItemList(
    items: List<MainShortsModel>,
    viewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
    adViewModel: AdViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    val index = moreViewModel.moreIndex.collectAsState()
    val viewType = viewModel.viewType.collectAsState()
    val route = viewModel.moreButtonClicked.value
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

    val isAtBottom = listState.isAtBottom()
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            if (moreViewModel.maxMoreIndex(viewType.value) > index.value) {
                loading(true)
                moreViewModel.setMorEVent(index.value + 1)
            }

            moreViewModel.moreIndex.collectLatest { newValue ->
                RLog.d(
                    "hbungshin",
                    "Received moreIndex update: $newValue, maxindex : ${
                        moreViewModel.maxMoreIndex(
                            viewType.value,
                        )
                    }",
                )
                if (newValue > 0 && moreViewModel.maxMoreIndex(viewType.value) >= newValue) {
                    moreViewModel.moreContent(viewType.value, newValue)
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

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (adBannerLoadingComplete.value.first) {
                        Modifier.padding(
                            bottom =
                                adBannerLoadingComplete.value.second.dp + insetPaddingValue.calculateTopPadding().value.dp +
                                    REMAIN_AD_MARGIN,
                        )
                    } else {
                        Modifier
                    },
                ),
        state = listState,
    ) {
        var i = 0
        item(key = items[i].itemPosition) {
            while (i < items.size) {
                if (i + 2 < items.size) {
                    Row(
                        Modifier.padding(vertical = 1.5.dp, horizontal = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    ) {
                        GridItemBoxRow(
                            rowSize = 3,
                            route = route ?: Destination.Home.Main.route,
                            viewType = viewType.value,
                            items =
                                listOf(
                                    items[i].apply { itemPosition = i },
                                    items[i + 1].apply { itemPosition = i + 1 },
                                    items[i + 2].apply { itemPosition = i + 2 },
                                ),
                            viewModel = viewModel,
                            moreViewModel = moreViewModel,
                        )
                    }

                    i += 3
                } else {
                    val remainCount = (items.size) - i
                    val list = mutableListOf<MainShortsModel>()

                    for (r in 0 until remainCount) {
                        list.add(
                            items[r].apply {
                                itemPosition = (items.size - 1) - remainCount - r
                            },
                        )
                    }
                    val blankItem = (3 - list.size)
                    if (blankItem > 0) {
                        for (b in 0 until blankItem) {
                            list.add(MainShortsModel(items.size - blankItem - b))
                        }
                    }

                    if (remainCount < 3) {
                        GridItemBoxRow(
                            rowSize = 3,
                            route = route ?: Destination.Home.Main.route,
                            viewType = viewType.value,
                            items = list,
                            viewModel = viewModel,
                            moreViewModel = moreViewModel,
                        )
                    }

                    i += 3
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun GideVideoArea(shortVideoModel: ShortsVideoModel?) {
    val videoTitle = shortVideoModel?.title ?: ""

    Text(
        videoTitle,
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 7.dp, end = 7.dp, bottom = 5.dp)
            .background(Color.Transparent),
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Normal,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = Color.White,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
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

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridChannelArea(
    modifier: Modifier,
    channel: ShortsChannelModel?,
) {
    val channelThumbnail = channel?.channelThumbNail ?: ""

    val channelTitle = channel?.channelTitle ?: ""

    Row(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 7.dp),
    ) {
        if (LocalInspectionMode.current) {
            Image(
                painter = painterResource(id = R.drawable.ic_play_icon),
                contentDescription = "Preview Image",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .wrapContentSize()
                        .width(24.dp)
                        .height(24.dp),
            )
        } else {
            NetworkImage(
                url = channelThumbnail,
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
        }
        Text(
            text = channelTitle,
            Modifier
                .wrapContentSize()
                .alpha(0.9f)
                .padding(start = 5.dp)
                .align(Alignment.CenterVertically),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun GridItemBoxRow(
    rowSize: Int,
    route: String,
    viewType: ViewType,
    items: List<MainShortsModel>,
    viewModel: MainViewModel,
    moreViewModel: MainMoreViewModel,
) {
    Row(
        // 좌우 패딩 추가
        Modifier.padding(vertical = 1.5.dp, horizontal = 3.dp),
//        // 아이템 간 간격 7dp
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
    ) {
        for (i in 0 until rowSize) {
            val shortVideoModel = items[i].shortsVideoModel
            val shortChannelModel = items[i].shortsChannelModel
            val position = items[i].itemPosition

            val videoThumbnail = items[i].shortsVideoModel?.thumbNail
            val videoDuration = shortVideoModel?.duration ?: "00:00"

            Box(
                Modifier.weight(1f)

                    then (
                        if (shortVideoModel != null) {
                            Modifier.clickable {
                                RLog.d(
                                    "hbungshin",
                                    "position : $position viewType : $viewType title :${shortVideoModel.title}",
                                )
                                viewModel.goEndContent(
                                    route,
                                    viewType,
                                    position,
                                )
                                viewModel.sendGALog(
                                    Event.SCREEN_VIEW,
                                    Destination.YouTube.dynamicRoute(
                                        items[i].shortsChannelModel?.channelId ?: "",
                                    ),
                                    moreViewModel.getConvertViewType(viewType),
                                    items[i].shortsChannelModel?.channelId,
                                    items[i].shortsVideoModel?.videoId,
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
            ) {
                if (videoThumbnail?.isNotEmpty() == true) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = 10.dp,
                    ) {
                        if (LocalInspectionMode.current) {
                            // Preview 모드에서 로컬 이미지 사용
                            Image(
                                // 로컬 이미지
                                painter = painterResource(id = R.drawable.sample_image),
                                contentDescription = "Preview Image",
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .aspectRatio(0.5625f)
                                        .fillMaxSize(),
                            )
                        } else {
                            // 실제 앱에서는 네트워크 이미지 사용
                            NetworkImage(
                                url = videoThumbnail,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .aspectRatio(0.5625f)
                                        .fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                R.drawable.vertical_background,
                                R.drawable.vertical_background,
                                R.drawable.vertical_background,
                            )
                        }
                    }

                    Column(
                        Modifier
                            .background(Background_op_20)
                            .align(Alignment.BottomEnd)
                            .padding(top = 7.dp),
                    ) {
                        GideVideoArea(shortVideoModel)
                        GridChannelArea(modifier = Modifier, shortChannelModel)

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
                                    ).align(Alignment.End),
                        ) {
                            Text(
                                text = videoDuration,
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
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
                    }
                }
            }
        }
    }
}
