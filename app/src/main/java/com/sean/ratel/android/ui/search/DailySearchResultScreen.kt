package com.sean.ratel.android.ui.search

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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.common.STRINGS.REMAIN_AD_MARGIN
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.LoadBanner
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.utils.ComposeUtil.isAtBottom
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailySearchResultScreen(
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
) {
    KeyWordSearchDisplayUi(adViewModel, searchViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun KeyWordSearchDisplayUi(
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
) {
    val currentData = searchViewModel.dailyCurrentSearchShortformList.collectAsState()
    if (currentData.value.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "데이터가 없습니다.",
                modifier =
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                // Text의 크기를 내용에 맞게 설정
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    } else {
        Scaffold(
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

            val listState =
                rememberLazyListState(
                    initialFirstVisibleItemIndex = scrollPosition.value,
                    initialFirstVisibleItemScrollOffset = scrollOffset.value,
                )

            if (currentData.value.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND)
                        .padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = 0.dp,
                            top = 0.dp,
                        ),
                ) {
                    DailySearchGridItemView(
                        currentData.value,
                        adViewModel,
                        searchViewModel,
                        loading = { load ->
                            moreLoading = load
                        },
                        listState,
                    )
                }
            }

            LaunchedEffect(Unit) {
                listState.scrollToItem(0)
            }

            RLog.d(
                "KKKKKKKKK",
                "moreLoading : $moreLoading , adBannerSize :$adBannerSize ,  bottomBarHeight : $bottomBarHeight $",
            )
            if (moreLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = (adBannerSize + bottomBarHeight).dp + REMAIN_AD_MARGIN)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            Modifier
                                .size(18.dp)
                                .padding(1.dp),
                            strokeWidth = 3.dp,
                            color = APP_TEXT_COLOR,
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailySearchGridItemView(
    data: List<MainShortsModel>,
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    // 로딩이 끝나면 ShortsItemList 표시
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .wrapContentHeight(),
    ) {
        KeyWordSearchGridItemList(data, adViewModel, searchViewModel, loading, listState)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun KeyWordSearchGridItemList(
    items: List<MainShortsModel>,
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    loading: (Boolean) -> Unit,
    listState: LazyListState,
) {
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()
    val index = searchViewModel.moreIndex.collectAsState()
    val searchComplete = searchViewModel.searchDataComplete.collectAsState()
    val currentCategory = searchViewModel.selectedCategory.collectAsState()

    val isAtBottom = listState.isAtBottom()
    val coroutine = rememberCoroutineScope()
    RLog.d(
        "KeywordSearch",
        "isAtBottom : $isAtBottom , maxMoreIndex : ${searchViewModel.maxMoreIndex(
            SearchType.ArchiveSearch,
        )} , index.value : ${index.value}",
    )
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            RLog.d(
                "KKKKKKKKK",
                "isAtBottom : $isAtBottom , maxMoreIndex : ${searchViewModel.maxMoreIndex(
                    SearchType.ArchiveSearch,
                )} , index.value : ${index.value}}",
            )
            if (searchViewModel.maxMoreIndex(SearchType.ArchiveSearch) > index.value) {
                searchViewModel.dailyMoreContent(index.value + 1, complete = {
                    loading(true)
                    coroutine.launch {
                        loading(false)
                        listState.animateScrollBy(50f)
                        searchViewModel.setMorEVent(index.value + 1)
                    }
                })

                searchViewModel.sendGALog(
                    screenName = GASplashAnalytics.SCREEN_NAME.get(SEARCH_SCREEN) ?: "",
                    eventName = GASplashAnalytics.Event.SEARCH__DAILY_MORE_VIEW,
                    actionName = GASplashAnalytics.Action.VIEW,
                    parameter =
                        mapOf(
                            GASplashAnalytics.Param.SEARCH_MORE_INDEX to index.toString(),
                        ),
                )
            }
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(0.83f)) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND),
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
                                DailyGridItemBoxRow(
                                    rowSize = 3,
                                    items =
                                        listOf(
                                            items[i].apply { itemPosition = i },
                                            items[i + 1].apply { itemPosition = i + 1 },
                                            items[i + 2].apply { itemPosition = i + 2 },
                                        ),
                                    searchViewModel = searchViewModel,
                                    currentCategory = currentCategory.value,
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
                                DailyGridItemBoxRow(
                                    rowSize = 3,
                                    items = list,
                                    searchViewModel = searchViewModel,
                                    currentCategory = currentCategory.value,
                                )
                            }

                            i += 3
                        }
                    }
                }
            }
        }
        val bottomPadding =
            adBannerLoadingComplete.value.second.dp +
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() +
                STRINGS.REMAIN_AD_MARGIN
        Box(
            Modifier
                .fillMaxWidth()
                .weight(0.17f)
                .then(
                    if (adBannerLoadingComplete.value.first) {
                        Modifier
                            .height(
                                bottomPadding,
                            )
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            // if (!searchComplete.value) {
            LoadBanner(Destination.Search.route, adViewModel, AdBannerLocation.BOTTOM)
            //  }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailyGideVideoArea(shortVideoModel: ShortsVideoModel?) {
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
fun DailyGridChannelArea(
    modifier: Modifier,
    channel: ShortsChannelModel?,
) {
    val channelThumbnail = channel?.channelThumbNail ?: ""

    val channelTitle = channel?.channelTitle ?: ""

    Row(
        modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        // .padding(start = 7.dp),
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
fun DailyGridItemBoxRow(
    rowSize: Int,
    items: List<MainShortsModel>,
    searchViewModel: SearchViewModel,
    currentCategory: YouTubeCategory,
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
            val context = LocalContext.current

            Box(
                Modifier.weight(1f)

                    then (
                        if (shortVideoModel != null) {
                            Modifier.clickable {
                                RLog.d(
                                    "deepLink",
                                    "position : $position viewType  " +
                                        "title :${shortVideoModel.title} ,  videoId : ${items[i].shortsVideoModel?.videoId}",
                                )

                                searchViewModel.goEndContent(
                                    context = context,
                                    route = Destination.Search.route,
                                    viewType = ViewType.SearchShortsDaily,
                                    videoId = shortVideoModel.videoId,
                                    selectedIndex = position,
                                    categoryKey = currentCategory.categoryKey,
                                )
                                searchViewModel.sendGALog(
                                    screenName = GASplashAnalytics.SCREEN_NAME.get(SEARCH_SCREEN) ?: "",
                                    eventName = GASplashAnalytics.Event.SELECT_SEARCH_DAILY_ITEM_CLICK,
                                    actionName = GASplashAnalytics.Action.CLICK,
                                    parameter =
                                        mapOf(
                                            GASplashAnalytics.Param.VIDEO_ID to (items[i].shortsVideoModel?.videoId ?: ""),
                                        ),
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
                        DailyGideVideoArea(shortVideoModel)
                        DailyGridChannelArea(modifier = Modifier, shortChannelModel)

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

// @Suppress("ktlint:standard:function-naming")
// @Preview(showBackground = true)
// @Composable
// fun SearchResultPreView() {
//    RatelappTheme {
//        val s1 =
//            SearchResultModel(
//                0,
//                "감스트",
//                "감스트 앞에서 그 옷을 입었더니.. 흑백 챌린지",
//                "https://www.youtube.com/shorts/uRCZx7uhRv8",
//                "uRCZx7uhRv8",
//                "https://i.ytimg.com/vi/uRCZx7uhRv8/hqdefault.jpg",
//                "SOONIGROUP [수니그룹]",
//                null,
//                "https://yt3.ggpht.com/ZSuwLWQmRwB-ZbFL0S6KRYzdOp_L9iEQV1AWFfYHMKLOO4twN4MyvRRBn4CB6RT1I4pXFwTl=s68-c-k-c0x00ffffff-no-rj",
//                "조회수 796만회",
//                "11개월 전",
//            )
//        // KeyWordSearchGridItemList(listOf(s1),null,null,{},LazyListState(),{})
//    }
// }
