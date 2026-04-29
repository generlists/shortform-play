package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.TopicItem
import com.sean.ratel.android.data.dto.TopicList
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun TopicCardPager(
    mainViewModel: MainViewModel,
    topicList: TopicList,
) {
    if (topicList.topicList.isEmpty()) return

    val title = topicList.title
    val topicItems = topicList.topicList

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
        ) {
            Spacer(Modifier.height(8.dp))
            TitleArea(mainViewModel, title)
            val startIndex = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % topicItems.size
            val listState = rememberLazyListState(startIndex)
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(count = Int.MAX_VALUE) { index ->
                        val selectedIndex = index % topicItems.keys.size

                        val topicItem = topicItems.values.toList()[selectedIndex]
                        TopicCard(mainViewModel, topicItem)
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TitleArea(
    mainViewModel: MainViewModel,
    title: String,
) {
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
fun TopicCard(
    mainViewModel: MainViewModel,
    topic: TopicItem,
) {
    Box(
        Modifier
            .fillMaxSize()
            .clickable {
                mainViewModel.goMoreContent(
                    route = Destination.Home.Main.TopicListDetail.route,
                    viewType = ViewType.MainTopic,
                    trendsShortsKey = null,
                    topicId = topic.topicId,
                )
            },
    ) {
        Card(
            modifier = Modifier.width(130.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec =
                                infiniteRepeatable(
                                    animation = tween(3000, easing = LinearEasing),
                                ),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .size(26.dp)
                                    .rotate(rotation)
                                    .background(
                                        brush =
                                            Brush.sweepGradient(
                                                colors =
                                                    listOf(
                                                        Color(0xFFFF6B6B),
                                                        Color(0xFFFFD93D),
                                                        Color(0xFF6BCB77),
                                                        Color(0xFF4D96FF),
                                                        Color(0xFFFF6B6B),
                                                    ),
                                            ),
                                        shape = CircleShape,
                                    ),
                        )

                        topic.iconUrl?.let {
                            NetworkImage(
                                url = it,
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
                    Column(Modifier, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            text = topic.topicName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp,
                            style =
                                LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                        )
                        Text(
                            text = "${formatNumberByLocale(topic.channel_count.toLong())} 개",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Light,
                            color = APP_SUBTITLE_TEXT_COLOR,
                            lineHeight = 10.sp,
                            style =
                                LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                ),
                        )
                    }
                }

                Text(
                    text = topic.hookText,
                    fontSize = 11.sp,
                    color = APP_SUBTITLE_TEXT_COLOR,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}
