package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.ChannelSubscriptionList
import com.sean.ratel.android.data.dto.ChannelSubscriptionUpList
import com.sean.ratel.android.data.dto.ChannelVideoSearchList
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.MAIN_TITLE_UNDER_LINE
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.TimeUtil
import com.sean.ratel.android.utils.UIUtil
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale
import com.sean.ratel.android.utils.UIUtil.formatNumberWithCommas
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun RankingHorizontalScrollView(
    pageSize: Int,
    mainViewModel: MainViewModel,
    channelSearchData: ChannelVideoSearchList,
    channelSubscriptionData: ChannelSubscriptionList,
    channelSubscriptionUpData: ChannelSubscriptionUpList,
) {
    val page = mainViewModel.channelCurrentPager.collectAsState(initial = 0)
    val pagerState =
        rememberPagerState(pageCount = {
            Int.MAX_VALUE
        })

    val channelSearchTitle = channelSearchData.title
    val channelSearchList =
        if (channelSearchData.searchList.size > 10) {
            channelSearchData.searchList.subList(0, 10)
        } else {
            channelSearchData.searchList
        }
    val channelSubscriptionTitle = channelSubscriptionData.title
    val channelSubscriptionList =
        if (channelSubscriptionData.subscriptionList.size > 10) {
            channelSubscriptionData.subscriptionList.subList(0, 10)
        } else {
            channelSubscriptionData.subscriptionList
        }
    val channelSubscriptionUpTitle = channelSubscriptionUpData.title
    val channelSubscriptionUpList =
        if (channelSubscriptionUpData.subscriptionUpList.size > 10) {
            channelSubscriptionUpData.subscriptionUpList.subList(0, 10)
        } else {
            channelSubscriptionUpData.subscriptionUpList
        }

    val period =
        channelSearchData.period?.let {
            "(${
                String.format(
                    stringResource(R.string.base_date),
                    TimeUtil.formatTimestamp(it),
                )
            })"
        } ?: run {
            "(${
                String.format(
                    stringResource(R.string.base_date),
                    TimeUtil.formatTimestamp(System.currentTimeMillis()),
                )
            })"
        }

    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        RankingTitleArea(
            period = period,
            mainViewModel,
            20.sp,
            stringResource(R.string.main_ranking_label),
            start = 10.dp,
            end = 0.dp,
            top = 25.dp,
            bottom = 20.dp,
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            HorizontalPager(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                pageSpacing = 3.dp,
                state = pagerState,
            ) { index ->
                val rankingIndex = index % (pageSize)

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    when (rankingIndex) {
                        0 ->
                            PagerList(
                                mainViewModel,
                                rankingIndex,
                                channelSearchTitle,
                                channelSearchList,
                            )

                        1 ->
                            PagerList(
                                mainViewModel,
                                rankingIndex,
                                channelSubscriptionTitle,
                                channelSubscriptionList,
                            )

                        2 ->
                            PagerList(
                                mainViewModel,
                                rankingIndex,
                                channelSubscriptionUpTitle,
                                channelSubscriptionUpList,
                            )
                    }
                }
            }
        }
    }

    LaunchedEffect(
        key1 = pagerState,
        block = {
            var initPage = Int.MAX_VALUE

            while (initPage % pageSize != 0) {
                initPage++
            }
            pagerState.scrollToPage(page.value)
        },
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged() // 페이지 변경 이벤트만 수신
            .collect { currentPage ->
                mainViewModel.setChannelPager(currentPage)
                // RLog.d("hbungshin", "Page changed to: $currentPage")
            }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun RankingTitleArea(
    period: String? = null,
    viewModel: MainViewModel?,
    fontSize: TextUnit = TextUnit.Unspecified,
    title: String,
    start: Dp,
    top: Dp,
    bottom: Dp,
    end: Dp,
    rankingIndex: Int = 0,
    subTitle: Boolean = false,
) {
    var textWidth by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = start, end = end, bottom = bottom, top = top),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (!subTitle) {
            Box(
                Modifier
                    .width(UIUtil.pixelToDp(context, textWidth).dp)
                    .height(8.dp)
                    .background(MAIN_TITLE_UNDER_LINE),
                contentAlignment = Alignment.BottomCenter,
            ) {}
        }

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(if (!subTitle) Modifier.padding(bottom = 2.5.dp) else Modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .wrapContentSize(),
            ) {
                Row(Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text =
                        title,
                        Modifier
                            .wrapContentSize(),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        color = Color.White,
                        onTextLayout = { textLayoutResult: TextLayoutResult ->
                            textWidth = textLayoutResult.size.width.toFloat() // 렌더링된 픽셀 크기
                        },
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
                    if (!subTitle) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 2.5.dp, end = 7.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Text(
                                text =
                                    period.toString(),
                                Modifier
                                    .wrapContentSize(),
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp,
                                color = APP_SUBTITLE_TEXT_COLOR,
                                style =
                                    TextStyle(
                                        shadow =
                                            Shadow(
                                                color = Color.White,
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
            if (subTitle) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(end = 7.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        stringResource(R.string.main_more),
                        Modifier
                            .wrapContentWidth()
                            .clickable {
                                val viewType =
                                    if (rankingIndex ==
                                        0
                                    ) {
                                        ViewType.ChannelSearchRanking
                                    } else if (rankingIndex ==
                                        1
                                    ) {
                                        ViewType.SubscriptionRanking
                                    } else if (rankingIndex ==
                                        2
                                    ) {
                                        ViewType.SubscriptionRankingUp
                                    } else {
                                        ViewType.ChannelSearchRanking
                                    }
                                viewModel?.goMoreContent(
                                    Destination.Home.Main.RankingChannelMore.route,
                                    viewType,
                                )
                                // 로딩바
                                viewModel?.setIsHomeVisible(true)
                                viewModel?.sendGALog(
                                    Event.SCREEN_VIEW,
                                    Destination.Home.Main.RankingChannelMore.route,
                                    viewType,
                                )
                            }.wrapContentHeight(),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = APP_TEXT_COLOR,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PagerList(
    viewModel: MainViewModel?,
    rankingIndex: Int,
    title: String,
    item: List<MainShortsModel>,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            RankingTitleArea(
                period = null,
                viewModel,
                16.sp,
                title,
                10.dp,
                10.dp,
                10.dp,
                0.dp,
                rankingIndex,
                subTitle = true,
            )
            ItemList(viewModel, rankingIndex, item)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ItemList(
    mainViewModel: MainViewModel?,
    rankingIndex: Int,
    items: List<MainShortsModel>,
) {
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
            Box(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(APP_BACKGROUND),
            )
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
                Box(
                    Modifier
                        .wrapContentSize()
                        .weight(0.3f),
                    contentAlignment = Alignment.Center,
                ) {
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
                        if (rankingIndex == 0) {
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
                    if (rankingIndex == 2) {
                        Text(
                            stringResource(R.string.main_rank_new),
                            Modifier
                                .wrapContentSize()
                                .weight(1f),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        for (index in items.indices) {
            Item(index, rankingIndex, mainViewModel, items)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun Item(
    index: Int,
    rankingIndex: Int,
    mainViewModel: MainViewModel?,
    items: List<MainShortsModel>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(APP_BACKGROUND)
            .clickable {
                val viewType =
                    if (rankingIndex ==
                        0
                    ) {
                        ViewType.ChannelSearchRanking
                    } else if (rankingIndex ==
                        1
                    ) {
                        ViewType.SubscriptionRanking
                    } else {
                        ViewType.SubscriptionRankingUp
                    }
                mainViewModel?.goEndContent(
                    Destination.Home.Main.route,
                    viewType,
                    index,
                )
                mainViewModel?.sendGALog(
                    Event.SCREEN_VIEW,
                    Destination.YouTube.dynamicRoute(
                        items[index].shortsChannelModel?.channelId ?: "",
                    ),
                    viewType,
                    items[index].shortsChannelModel?.channelId,
                    items[index].shortsVideoModel?.videoId,
                )
            }.padding(start = 7.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val channel = items[index].shortsChannelModel

        Text(
            getRankingOrder(index),
            fontFamily = FontFamily.SansSerif,
            modifier =
                Modifier
                    .wrapContentSize()
                    .weight(0.1f),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
        )
        Row(
            Modifier
                .wrapContentSize()
                .weight(0.3f),
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
                if (rankingIndex == 0) {
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
            if (rankingIndex == 2) {
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
@Preview(showBackground = true)
@Composable
fun RankingPreView() {
    RatelappTheme {
        RankingTitleArea(
            period = TimeUtil.formatMillisToDate(System.currentTimeMillis()),
            null,
            20.sp,
            stringResource(R.string.main_ranking_label),
            start = 10.dp,
            end = 0.dp,
            top = 10.dp,
            bottom = 15.dp,
        )
        // HorizontalScrollView(imageUrls)
        // PagerList(null, 0, "인기 숏폼", listOf(MainShortsModel()))
    }
}
