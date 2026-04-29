package com.sean.ratel.android.ui.home.main

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.log.GAKeys.TOPIC_DETAIL
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.TopicFilterType
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_DIABLE_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.PhoneUtil.isTablet
import com.sean.ratel.android.utils.UIUtil.pixelToDp

@Suppress("ktlint:standard:function-naming")
@Composable
fun TopicDetailScreen(
    modifier: Modifier,
    topicKey: String,
    mainViewModel: MainViewModel,
) {
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    val listState = rememberLazyListState()
    val topicData =
        remember {
            mutableStateOf(
                mainViewModel.mainShorts.value.first
                    .topicList.topicList[topicKey],
            )
        }

    var selectedFilter by remember { mutableStateOf(0) }
    val filters =
        listOfNotNull(
            topicData.value?.popularlist?.title,
            topicData.value?.viewlist?.title,
            topicData.value?.subscriberlist?.title,
        )
    val currentList =
        when (selectedFilter) {
            0 -> topicData.value?.popularlist
            1 -> topicData.value?.viewlist
            2 -> topicData.value?.subscriberlist
            else -> topicData.value?.popularlist
        }

    Box(
        modifier
            .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF111111)),
        ) {
            val context = LocalContext.current

            // .padding(top = insetPaddingValue.calculateTopPadding()

            LazyColumn(state = listState) {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .height(200.dp),
                    ) {
                        val brandUrl =
                            fallbackBrandImage(
                                topicData.value?.topicId ?: "mukBang",
                                topicData.value?.topBackgroundThumbNailUrl,
                            )

                        NetworkImage(
                            url = brandUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                            imageLoader = mainViewModel.imageLoader,
                            // placeholderRes = R.drawable.image_flow_loading,
                            loadComplete = {},
                        )
                        TopNavigationBar(
                            titleString = topicData.value?.topicName ?: "",
                            historyBack = { mainViewModel.runNavigationBack() },
                            isShareButton = true,
                            runSetting = {
                                PhoneUtil.shareAppLinkButton(context)
                                mainViewModel.sendGALog(
                                    screenName =
                                        GASplashAnalytics.SCREEN_NAME.get(
                                            TOPIC_DETAIL,
                                        ) ?: "",
                                    eventName = GASplashAnalytics.Event.SELECT_TOPIC_DETAIL_SHARE_BTN_CLICK,
                                    actionName = GASplashAnalytics.Action.CLICK,
                                    mapOf(),
                                )
                            },
                            filterButton = false,
                            onFilterChange = {
                            },
                            items =
                                listOf(),
                            isTopicScreen = true,
                        )
                    }
                }
                // 필터탭 스티키
                stickyHeader {
                    LazyRow(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(APP_BACKGROUND),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        itemsIndexed(filters) { index, filter ->
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (selectedFilter == index) APP_TEXT_COLOR else APP_FILTER_BACKGROUND,
                                        ).clickable {
                                            selectedFilter = index

                                            val filterType =
                                                when (selectedFilter) {
                                                    0 -> TopicFilterType.Popular
                                                    1 -> TopicFilterType.Views
                                                    2 -> TopicFilterType.Subscriber
                                                    else -> TopicFilterType.Popular
                                                }

                                            mainViewModel.sendGALog(
                                                screenName =
                                                    GASplashAnalytics.SCREEN_NAME.get(
                                                        TOPIC_DETAIL,
                                                    ) ?: "",
                                                eventName = GASplashAnalytics.Event.SELECT_TOPIC_DETAIL_FILTER_ITEM_CLICK,
                                                actionName = GASplashAnalytics.Action.CLICK,
                                                mapOf(
                                                    "topicId" to topicKey,
                                                    "filterType" to filterType.name,
                                                ),
                                            )
                                        }.padding(horizontal = 16.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilter == index) APP_BACKGROUND else APP_FILTER_DIABLE_COLOR,
                                )
                            }
                        }
                    }
                }
                // 1~10위 채널 가로 스크롤

                item {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        border =
                            BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.main_topic_top_channel),
                            Modifier
                                .wrapContentSize()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            style =
                                TextStyle(
                                    shadow =
                                        Shadow(
                                            color = Color.White,
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f,
                                        ),
                                ),
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            itemsIndexed(
                                currentList
                                    ?.topicList
                                    ?.flatMap { it.topicList }
                                    ?.distinctBy { it.shortsChannelModel?.channelId }
                                    ?.take(10) ?: emptyList(),
                            ) { index, item ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier.clickable {
                                            Log.d(
                                                "hbungshin",
                                                "channelId : ${item.shortsChannelModel?.channelId} , " +
                                                    "selectedFilter : $selectedFilter , " +
                                                    "topicId : ${topicData.value?.topicId}",
                                            )
                                            val topicId = topicData.value?.topicId ?: ""
                                            val channelId = item.shortsChannelModel?.channelId ?: ""
                                            val filterType =
                                                when (selectedFilter) {
                                                    0 -> TopicFilterType.Popular
                                                    1 -> TopicFilterType.Views
                                                    2 -> TopicFilterType.Subscriber
                                                    else -> TopicFilterType.Popular
                                                }
//
                                            mainViewModel.goEndContent(
                                                Destination.Home.Main.TopicListDetail.route,
                                                ViewType.TopicChannel,
                                                index,
                                                channelId,
                                                topicId = topicId,
                                                filterType = filterType,
                                            )
                                            mainViewModel.sendGALog(
                                                screenName = GASplashAnalytics.SCREEN_NAME.get(TOPIC_DETAIL) ?: "",
                                                eventName = GASplashAnalytics.Event.SELECT_TOPIC_DETAIL_CHANNEL_ITEM_CLICK,
                                                actionName = GASplashAnalytics.Action.CLICK,
                                                mapOf(
                                                    "viewType" to ViewType.TopicChannel.name,
                                                    "topicId" to topicId,
                                                    "channelId" to channelId,
                                                    "filterType" to filterType.name,
                                                ),
                                            )
                                        },
                                    ) {
                                        val borderBrush =
                                            if (index == 0 || index == 1 || index == 2) {
                                                Brush.sweepGradient(
                                                    listOf(
                                                        Color(0xFFFF6B6B),
                                                        Color(0xFFFFD93D),
                                                        Color(0xFF4D96FF),
                                                        Color(0xFFFF6B6B),
                                                    ),
                                                )
                                            } else {
                                                Brush.sweepGradient(
                                                    listOf(
                                                        Color(0xFF444444),
                                                        Color(0xFF444444),
                                                    ),
                                                )
                                            }

                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(64.dp)
                                                    .border(2.dp, borderBrush, CircleShape),
                                        ) {
//
                                            NetworkImage(
                                                url =
                                                    item.shortsChannelModel?.channelThumbNail
                                                        ?: "",
                                                contentDescription = null,
                                                modifier =
                                                    Modifier
                                                        .clip(CircleShape)
                                                        .width(64.dp)
                                                        .height(64.dp),
                                                ContentScale.Fit,
                                                R.drawable.ic_play_icon,
                                                R.drawable.ic_play_icon,
                                                R.drawable.ic_play_icon,
                                            )
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .wrapContentSize()
                                                    .size(if (index == 0 || index == 1 || index == 2) 24.dp else 16.dp)
                                                    .background(
                                                        if (index == 0 || index == 1 ||
                                                            index == 2
                                                        ) {
                                                            Color.Transparent
                                                        } else {
                                                            APP_FILTER_BACKGROUND
                                                        },
                                                        CircleShape,
                                                    ).align(Alignment.BottomStart),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                getRankNumber(index),
                                                fontSize = if (index == 0 || index == 1 || index == 2) 18.sp else 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.Center),
                                                color = if (index == 0) Color.Black else APP_FILTER_DIABLE_COLOR,
                                                style =
                                                    LocalTextStyle.current.copy(
                                                        platformStyle =
                                                            PlatformTextStyle(
                                                                includeFontPadding = false,
                                                            ),
                                                        lineHeightStyle =
                                                            LineHeightStyle(
                                                                alignment = LineHeightStyle.Alignment.Center,
                                                                trim = LineHeightStyle.Trim.Both,
                                                            ),
                                                    ),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        item.shortsChannelModel?.channelTitle ?: "",
                                        fontSize = 12.sp,
                                        color = Color(0xFFAAAAAA),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(52.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                }
                // 그룹별 훅 타이틀 + 가로 스크롤 영상
                items(currentList?.topicList?.size ?: 0) { cIndex ->
                    RLog.d("hbungshin", "index : $cIndex")
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        border =
                            BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(8.dp))
                            TitleArea(currentList?.topicList[cIndex]?.hookText ?: "")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(
                                    count = currentList?.topicList[cIndex]?.topicList?.size ?: 0,
                                ) { rIndex ->
                                    val item = currentList?.topicList[cIndex]?.topicList[rIndex]

                                    Card(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    var pervSize = 0
                                                    var index = 0
                                                    if (cIndex > 0) {
                                                        for (i in 0 until cIndex) {
                                                            pervSize += (
                                                                currentList?.topicList[i]?.topicList?.size
                                                                    ?: 0
                                                            )
                                                            RLog.d(
                                                                "TopicDetailScreen",
                                                                "pervSize  $pervSize",
                                                            )
                                                        }
                                                        index = (rIndex) + pervSize
                                                    } else {
                                                        index = rIndex
                                                    }

                                                    val topicId = topicData.value?.topicId ?: ""
                                                    val filterType =
                                                        when (selectedFilter) {
                                                            0 -> TopicFilterType.Popular
                                                            1 -> TopicFilterType.Views
                                                            2 -> TopicFilterType.Subscriber
                                                            else -> TopicFilterType.Popular
                                                        }

                                                    mainViewModel.goEndContent(
                                                        route = Destination.Home.Main.TopicListDetail.route,
                                                        viewType = ViewType.TopicGroup,
                                                        selectedIndex = index,
                                                        topicId = topicId,
                                                        filterType = filterType,
                                                    )

                                                    mainViewModel.sendGALog(
                                                        screenName =
                                                            GASplashAnalytics.SCREEN_NAME.get(
                                                                TOPIC_DETAIL,
                                                            ) ?: "",
                                                        eventName = GASplashAnalytics.Event.SELECT_TOPIC_DETAIL_GROUP_ITEM_CLICK,
                                                        actionName = GASplashAnalytics.Action.CLICK,
                                                        mapOf(
                                                            "viewType" to ViewType.TopicChannel.name,
                                                            "topicId" to topicId,
                                                            "selectedIndex" to index.toString(),
                                                            "filterType" to filterType.name,
                                                        ),
                                                    )
                                                }.padding(vertical = 16.dp)
                                                .aspectRatio(9f / 16f),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        var componentWidth by remember { mutableStateOf(0) }
                                        val context = LocalContext.current
                                        val videoTitle =
                                            rememberSaveable(item?.shortsVideoModel) {
                                                item?.shortsVideoModel?.title ?: ""
                                            }
                                        val videoDuration =
                                            rememberSaveable(item?.shortsVideoModel) {
                                                item?.shortsVideoModel?.duration ?: ""
                                            }

                                        Box(
                                            Modifier
                                                .wrapContentSize()
                                                .onGloballyPositioned { coordinates ->
                                                    componentWidth = coordinates.size.width
                                                },
                                        ) {
                                            Box(
                                                modifier = Modifier.widthIn(max = 164.dp),
                                            ) {
                                                var backgroundColor by remember {
                                                    mutableStateOf(
                                                        Color.Black,
                                                    )
                                                }

                                                Card(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(9 / 16f),
                                                    // 가로 길이에 맞춰 높이 1:1 확보 (이미지 크기 결정)
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                                ) {
                                                    val sw =
                                                        LocalConfiguration.current.smallestScreenWidthDp
                                                    NetworkImage(
                                                        modifier =
                                                            if (isTablet(sw)) {
                                                                Modifier.fillMaxWidth()
                                                            } else {
                                                                Modifier
                                                                    .width(194.dp)
                                                                    .height(341.dp)
                                                            },
                                                        contentScale = ContentScale.Crop,
                                                        imageLoader = mainViewModel.imageLoader,
                                                        url =
                                                            item?.shortsVideoModel?.thumbNail
                                                                ?: "",
                                                        contentDescription = null,
                                                        loadComplete = {},
                                                    )
                                                }

                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    verticalAlignment = Alignment.Top,
                                                ) {
                                                    Box(
                                                        modifier = Modifier.size(24.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        NetworkImage(
                                                            url =
                                                                item?.shortsChannelModel?.channelThumbNail
                                                                    ?: "",
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
                                                }

                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .wrapContentSize()
                                                            .align(Alignment.BottomEnd)
                                                            .background(
                                                                brush =
                                                                    Brush.verticalGradient(
                                                                        colors =
                                                                            listOf(
                                                                                Color.Black.copy(
                                                                                    alpha = 0.0f,
                                                                                ),
                                                                                Color.Black.copy(
                                                                                    alpha = 0.4f,
                                                                                ),
                                                                                Color.Black.copy(
                                                                                    alpha = 0.7f,
                                                                                ),
                                                                            ),
                                                                    ),
                                                            ),
                                                ) {
                                                    Column(
                                                        modifier = Modifier.wrapContentSize(),
                                                    ) {
                                                        Box(
                                                            Modifier
                                                                .width(
                                                                    pixelToDp(
                                                                        context,
                                                                        componentWidth.toFloat(),
                                                                    ).dp,
                                                                ).padding(top = 5.dp, end = 5.dp),
                                                            contentAlignment = Alignment.CenterEnd,
                                                        ) {
                                                            Text(
                                                                text = videoTitle,
                                                                Modifier
                                                                    .width(
                                                                        pixelToDp(
                                                                            context,
                                                                            componentWidth.toFloat(),
                                                                        ).dp,
                                                                    ).padding(
                                                                        top = 5.dp,
                                                                        bottom = 10.dp,
                                                                        start = 10.dp,
                                                                        end = 10.dp,
                                                                    ),
                                                                color = Color.White,
                                                                fontSize = 13.sp,
                                                                fontFamily = FontFamily.SansSerif,
                                                                fontStyle = FontStyle.Normal,
                                                                fontWeight = FontWeight.SemiBold,
                                                                style =
                                                                    TextStyle(
                                                                        shadow =
                                                                            Shadow(
                                                                                color = Color.Black,
                                                                                // 그림자의 위치 (x, y)
                                                                                offset =
                                                                                    Offset(
                                                                                        2f,
                                                                                        2f,
                                                                                    ),
                                                                                // 그림자의 흐림 정도
                                                                                blurRadius = 4f,
                                                                            ),
                                                                    ),
                                                                maxLines = 2,
                                                                minLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        }
                                                        TimeArea(videoDuration)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TitleArea(title: String) {
    var textWidth by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.wrapContentSize(),
            ) {
                Text(
                    text = title,
                    Modifier
                        .wrapContentSize(),
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    onTextLayout = { textLayoutResult: TextLayoutResult ->
                        textWidth = textLayoutResult.size.width.toFloat()
                    },
                    style =
                        TextStyle(
                            shadow =
                                Shadow(
                                    color = Color.White,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f,
                                ),
                        ),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TimeArea(duration: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomEnd,
    ) {
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
                                        Color.Black.copy(alpha = 0.0f),
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.5f),
                                    ),
                            ),
                    ),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Text(
                text = duration,
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
    }
}

fun fallbackBrandImage(
    topicId: String,
    topBackgroundThumbNailUrl: String?,
): String {
    return topBackgroundThumbNailUrl
        ?.takeIf { it.isNotEmpty() }
        ?: return getTopicImageUrl(topicId)
}

fun getTopicImageUrl(topicId: String): String =
    when (topicId) {
        "mukBang" -> {
            "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "dog" -> {
            "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "cat" -> {
            "https://images.pexels.com/photos/45201/kitty-cat-kitten-pet-45201.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "camping" -> {
            "https://images.pexels.com/photos/6271625/pexels-photo-6271625.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "vTuber" -> {
            "https://images.pexels.com/photos/7915357/pexels-photo-7915357.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "fashion" -> {
            "https://images.pexels.com/photos/2983464/pexels-photo-2983464.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "coverDance" -> {
            "https://images.pexels.com/photos/1701202/pexels-photo-1701202.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "homeWorkOut" -> {
            "https://images.pexels.com/photos/414029/pexels-photo-414029.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "lookBook" -> {
            "https://images.pexels.com/photos/994523/pexels-photo-994523.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "makeUp" -> {
            "https://images.pexels.com/photos/3373746/pexels-photo-3373746.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "coverSong" -> {
            "https://images.pexels.com/photos/164821/pexels-photo-164821.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "bodyBuilding" -> {
            "https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "baking" -> {
            "https://images.pexels.com/photos/1070946/pexels-photo-1070946.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "trucker" -> {
            "https://images.pexels.com/photos/2199293/pexels-photo-2199293.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "sneakers" -> {
            "https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        "stocks" -> {
            "https://images.pexels.com/photos/6801648/pexels-photo-6801648.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }

        else -> {
            "https://images.pexels.com/photos/5077064/pexels-photo-5077064.jpeg?" +
                "auto=compress&cs=tinysrgb&w=600&h=400&fit=crop"
        }
    }

val rankNumbers =
    listOf(
        "\uD83E\uDD47",
        "\uD83E\uDD48",
        "\uD83E\uDD49",
        "𝟒",
        "𝟓",
        "𝟔",
        "𝟕",
        "𝟖",
        "𝟗",
        "𝟏𝟎",
    )

fun getRankNumber(rank: Int): String = rankNumbers.getOrNull(rank) ?: rank.toString()

//    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
