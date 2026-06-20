package com.sean.ratel.android.ui.end

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.YOUTUBE_APP_BY_CHANNEL_ID
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.utils.ComposeUtil.LinkedText
import com.sean.ratel.android.utils.PhoneUtil.openBrowsere
import com.sean.ratel.android.utils.PhoneUtil.searchButton
import com.sean.ratel.android.utils.PhoneUtil.sendEmail
import com.sean.ratel.android.utils.TimeUtil.formatLocalizedDate
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("ktlint:standard:function-naming")
fun YouTubeEndMoreView(
    mainViewModel: MainViewModel,
    currentShortsModel: MainShortsModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val mainShorts = mainViewModel.mainShorts.collectAsState()
    val topicCategory =
        mainShorts
            .value
            .first
            .topicList
            .topicCategory

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
        containerColor = APP_BACKGROUND,
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFF444444), RoundedCornerShape(999.dp)),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
        ) {
            // 채널 배너 + 그라디언트 페이드
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            ) {
                NetworkImage(
                    url = currentShortsModel.shortsChannelModel?.brandExternalUrl ?: "",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    imageLoader = mainViewModel.imageLoader,
                    loadComplete = {},
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startY = 80f,
                                ),
                            ),
                )
            }

            // 채널 섹션
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-24).dp),
            ) {
                Text(
                    text = stringResource(R.string.more_channel_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 0.08.em,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 썸네일 + 채널명
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    currentShortsModel.shortsChannelModel?.channelThumbNail?.let {
                        NetworkImage(
                            url = it,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .clip(CircleShape)
                                    .width(48.dp)
                                    .height(48.dp)
                                    .border(1.5.dp, APP_TEXT_COLOR, CircleShape),
                            ContentScale.Fit,
                            R.drawable.ic_play_icon,
                            R.drawable.ic_play_icon,
                            R.drawable.ic_play_icon,
                        )
                    }
                    Column {
                        val handle =
                            currentShortsModel.shortsChannelModel?.customUrl
                                ?: currentShortsModel.shortsChannelModel?.channelId ?: ""
                        Text(
                            text = currentShortsModel.shortsChannelModel?.channelTitle ?: "",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                        )
                        Text(
                            text = handle,
                            Modifier.clickable {
                                val url = YOUTUBE_APP_BY_CHANNEL_ID(handle)
                                openBrowsere(context, url)
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 구독자 / 조회수
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatCard(
                        label = stringResource(R.string.main_rank_subscription),
                        value = currentShortsModel.shortsChannelModel?.subscriberCount ?: "",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.more_channel_view_count),
                        value = currentShortsModel.shortsChannelModel?.viewCount ?: "",
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // 채널 시작일
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.more_channel_start_date),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text =
                            formatLocalizedDate(
                                currentShortsModel.shortsChannelModel?.publishDate
                                    ?: "2006-03-18T15:43:10Z",
                                Locale.getDefault(),
                            ),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isTopicCategory =
                    currentShortsModel.shortsVideoModel?.categoryName == currentShortsModel.shortsVideoModel?.topicName
                val categoryName =
                    if (isTopicCategory) {
                        currentShortsModel.shortsVideoModel?.copyCategory
                    } else {
                        currentShortsModel.shortsVideoModel?.categoryName
                    }

                categoryName?.let {
                    // 카테고리
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = Color(0xFF555555),
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stringResource(R.string.search_category),
                            fontSize = 12.sp,
                            color = Color(0xFF555555),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TopicChip(label = categoryName)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 채널 설명
                if (!currentShortsModel.shortsChannelModel?.channelDescription.isNullOrEmpty()) {
                    LinkedText(
                        text = currentShortsModel.shortsChannelModel?.channelDescription ?: "",
                        color = Color.White.copy(alpha = 0.7f),
                        onEmailClick = {
                            sendEmail(context, emailAddress = it)
                        },
                        onUrlClick = {
                            openBrowsere(context, it)
                        },
                        onHashtagClick = {
                            searchButton(
                                context,
                                topicCategory = topicCategory,
                                query = it,
                                tab = "keyword",
                            )
                        },
                        onMentionClick = {
                            searchButton(
                                context,
                                topicCategory = topicCategory,
                                query = it,
                                tab = "keyword",
                            )
                        },
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 구분선
                HorizontalDivider(
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )

                // 영상 섹션
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.more_video_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        letterSpacing = 0.08.em,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentShortsModel.shortsVideoModel?.title ?: "",
                        fontSize = 13.sp,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    // 영상 통계
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        VideoStatCard(
                            icon = Icons.Default.Favorite,
                            label = stringResource(R.string.more_video_like),
                            value = currentShortsModel.shortsVideoModel?.likeCount ?: "",
                            modifier = Modifier.weight(1f),
                        )
                        VideoStatCard(
                            icon = Icons.Default.Visibility,
                            label = stringResource(R.string.more_video_search),
                            value = currentShortsModel.shortsVideoModel?.viewCount ?: "",
                            modifier = Modifier.weight(1f),
                        )
                        VideoStatCard(
                            icon = Icons.Default.ChatBubbleOutline,
                            label = stringResource(R.string.more_video_comment),
                            value = currentShortsModel.shortsVideoModel?.commentCount ?: "",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!currentShortsModel.shortsVideoModel?.description.isNullOrBlank()) {
                        // 비디오 설명
                        LinkedText(
                            text = currentShortsModel.shortsVideoModel?.description ?: "",
                            color = Color(0xFFCCCCCC),
                            onEmailClick = {
                                sendEmail(context, emailAddress = it)
                            },
                            onUrlClick = {
                                openBrowsere(context, it)
                            },
                            onHashtagClick = {
                                searchButton(context, topicCategory = topicCategory, query = it, tab = "keyword")
                            },
                            onMentionClick = {
                                searchButton(context, topicCategory = topicCategory, query = it, tab = "keyword")
                            },
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 토픽
                    currentShortsModel.shortsVideoModel?.topicName?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = stringResource(R.string.more_topic),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TopicChip(label = it)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(10.dp),
                ).border(
                    0.5.dp,
                    APP_TEXT_COLOR.copy(alpha = 0.2f),
                    RoundedCornerShape(10.dp),
                ).padding(12.dp),
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(4.dp))
            // Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = APP_TEXT_COLOR)
            AnimatedCountText(targetValue = value.toLong())
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun VideoStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(Color(0xFF0D0D0D), RoundedCornerShape(10.dp))
                .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(10.dp))
                .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = APP_TEXT_COLOR,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(2.dp))
            AnimatedCountText(targetValue = value.toLong(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun TopicChip(label: String) {
    Box(
        modifier =
            Modifier
                .background(
                    APP_TEXT_COLOR.copy(alpha = 0.1f),
                    RoundedCornerShape(999.dp),
                ).border(
                    0.5.dp,
                    APP_TEXT_COLOR.copy(alpha = 0.3f),
                    RoundedCornerShape(999.dp),
                ).padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text = label, fontSize = 11.sp, color = APP_TEXT_COLOR)
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
fun AnimatedCountText(
    targetValue: Long,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    color: Color = Color(0xFFABFF43),
    durationMillis: Int = 1200,
    formatter: (Long) -> String = { formatNumberByLocale(it) },
) {
    var animatedValue by remember { mutableLongStateOf(0L) }

    LaunchedEffect(targetValue) {
        animate(
            initialValue = 0f,
            targetValue = targetValue.toFloat(),
            animationSpec =
                tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing,
                ),
        ) { value, _ ->
            animatedValue = value.toLong()
        }
    }

    Text(
        text = formatter(animatedValue),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier,
    )
}
