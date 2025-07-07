package com.sean.ratel.android.ui.home.main.itemview

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TopFiveList
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_WHITE_10
import com.sean.ratel.android.ui.theme.IMAGE_FLOW_DOT_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil.goYoutubeApp
import com.sean.ratel.android.utils.TimeUtil.formatLocalizedDate
import com.sean.ratel.android.utils.UIUtil.formatNumberByLocale
import com.sean.ratel.android.utils.UIUtil.getScreenWidthDp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("ktlint:standard:function-naming")
@Composable
fun AutoScrollImagePager(
    mainViewModel: MainViewModel?,
    topFiveList: TopFiveList?,
) {
    val topItemSize = topFiveList?.fiveList?.keys?.size ?: 5
    val topFrontMainList =
        topFiveList
            ?.fiveList
            ?.values
            ?.map { it[0] }
            ?.toList()

    val pagerState =
        rememberPagerState(pageCount = {
            Int.MAX_VALUE
        })
    var autoScroll by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val heightInPx = with(density) { 150.dp.toPx() } // Dp -> Px 변환

    // 자동 스크롤 기능
    LaunchedEffect(autoScroll) {
        while (autoScroll) {
            delay(4000)
            val nextPage = pagerState.currentPage + 1
            pagerState.animateScrollToPage(nextPage, animationSpec = tween(durationMillis = 1500))
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 512 288
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            autoScroll = false // 터치 시 자동 스크롤 중지
                            coroutineScope.launch {
                                delay(2000L) // 2초 대기 후 자동 스크롤 재개
                                autoScroll = true
                            }
                        }
                    },
        ) { page ->
            // 실제 이미지의 인덱스 계산
            val imageIndex = (page % topItemSize)
            val channelId = topFrontMainList?.get(imageIndex)?.shortsChannelModel?.channelId ?: ""
            val externalUrl =
                topFrontMainList?.get(imageIndex)?.shortsChannelModel?.brandExternalUrl ?: ""

            Surface(
                Modifier
                    .padding(bottom = 10.dp)
                    .clickable {
                        mainViewModel?.goEndContent(
                            Destination.Home.Main.route,
                            ViewType.ImageFlow,
                            imageIndex,
                            channelId,
                        )
                        mainViewModel?.sendGALog(
                            Event.SCREEN_VIEW,
                            Destination.YouTube.dynamicRoute(channelId),
                            ViewType.ImageFlow,
                            channelId,
                        )
                    },
                shadowElevation = 10.dp,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .aspectRatio(1.7f),
                ) {
                    NetworkImage(
                        url = externalUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            // 위쪽은 투명
                                            Color.Transparent,
                                            // 중간은 조금 어둡게
                                            Color.Black.copy(alpha = 0.1f),
                                            Color.Black.copy(alpha = 0.2f),
                                            // 완전히 어두운 색
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Black.copy(alpha = 0.6f),
                                            // Color.Black.copy(alpha = 0.7f),
                                        ),
                                    startY = 0f,
                                    // Box 높이와 맞추기
                                    endY = heightInPx,
                                ),
                            ).height(150.dp)
                            .align(Alignment.BottomCenter)
                            .padding(top = 7.dp),
                    ) {
                        FlowChannelArea(Modifier, topFrontMainList?.get(imageIndex))
                    }
                }
            }
        }

        // 인디케이터 추가
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(bottom = 10.dp),
        ) {
            repeat(topItemSize) { index ->
                // 무한 스크롤에 맞춰 현재 페이지를 인디케이터에 매핑
                val color =
                    if (pagerState.currentPage % topItemSize == index) APP_TEXT_COLOR else IMAGE_FLOW_DOT_BACKGROUND
                Box(
                    modifier =
                        Modifier
                            .size(16.dp)
                            .padding(4.dp)
                            .background(
                                color = color,
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun FlowChannelArea(
    modifier: Modifier,
    mainShortsModel: MainShortsModel?,
) {
    val context = LocalContext.current

    val channelThumbnail =
        rememberSaveable(mainShortsModel?.shortsChannelModel) {
            mainShortsModel?.shortsChannelModel?.channelThumbNail ?: ""
        }
    val channelTitle =
        rememberSaveable(mainShortsModel?.shortsChannelModel) {
            mainShortsModel?.shortsChannelModel?.channelTitle ?: ""
        }
    val subscriptText =
        String.format(
            stringResource(R.string.subscription_count),
            formatNumberByLocale(
                (mainShortsModel?.shortsChannelModel?.subscriberCount ?: "0").toLong(),
            ),
        )
    val searchCount =
        String.format(
            stringResource(R.string.search_count),
            formatNumberByLocale(
                (mainShortsModel?.shortsChannelModel?.viewCount ?: "0").toLong(),
            ),
        )
    val publishDate =
        String.format(
            stringResource(R.string.start_date),
            formatLocalizedDate(
                mainShortsModel?.shortsChannelModel?.publishDate ?: "2006-03-18T15:43:10Z",
                Locale.getDefault(),
            ),
        )
    val description = remember { mainShortsModel?.shortsChannelModel?.channelDescription ?: "" }

    val channelId = mainShortsModel?.shortsChannelModel?.channelId ?: ""
    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                Modifier
                    .wrapContentSize()
                    .padding(top = 5.dp),
            ) {
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

            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 7.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    Box(
                        Modifier
                            .wrapContentSize()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        TextArea(
                            text = channelTitle,
                            fontSize = 17.sp,
                            color = Color.White,
                            lineHeight = 17.sp,
                            bold = FontWeight.Bold,
                            1,
                            Modifier
                                .width(130.dp)
                                .padding(top = 5.dp, bottom = 10.dp),
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 7.dp, end = 10.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Text(
                            text = annotionString(context, channelId = channelId),
                            fontSize = 12.sp,
                            modifier =
                                Modifier
                                    .wrapContentSize()
                                    .align(Alignment.CenterEnd)
                                    .clickable {
                                        annotionString(context, channelId = channelId)
                                            .getStringAnnotations(
                                                tag = "URL",
                                                start = 0,
                                                end =
                                                    annotionString(
                                                        context,
                                                        channelId = channelId,
                                                    ).length,
                                            ).firstOrNull()
                                            ?.let { stringAnnotation ->
                                                // 링크 열기
                                                goYoutubeApp(context, stringAnnotation.item)
                                            }
                                    },
                        )
                    }
                }

                TextArea(
                    subscriptText,
                    11.sp,
                    Color.White,
                    11.sp,
                    FontWeight.SemiBold,
                    1,
                    Modifier.padding(bottom = 3.dp),
                )
                TextArea(
                    searchCount,
                    11.sp,
                    Color.White,
                    11.sp,
                    FontWeight.SemiBold,
                    1,
                    Modifier.padding(bottom = 3.dp),
                )
                TextArea(publishDate, 11.sp, Color.White, 11.sp, FontWeight.SemiBold, 1, Modifier)
                TextArea(
                    text =
                        descriptionText(
                            description,
                        ),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Background_op_WHITE_10,
                    bold = FontWeight.Normal,
                    maxLine = 1,
                    modifier =
                        Modifier
                            .width(getScreenWidthDp(context).dp)
                            .padding(end = 25.dp, top = 5.dp, bottom = 10.dp),
                )
            }
        }
    }
}

private fun descriptionText(description: String): String {
    if (description.length > 50) {
        return description.substring(0, 50).replace("\n", "")
    }

    return description
}

private fun annotionString(
    context: Context,
    channelId: String,
): AnnotatedString {
    val annotatedText =
        buildAnnotatedString {
            // 링크 추가
            pushStringAnnotation(
                tag = "URL",
                annotation = STRINGS.YOUTUBE_APP_BY_CHANNEL_ID(channelId),
            )
            withStyle(
                style =
                    SpanStyle(
                        color = APP_TEXT_COLOR,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    ),
            ) {
                append(context.getString(R.string.main_go_channel))
            }
            pop()
        }
    return annotatedText
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TextArea(
    text: String,
    fontSize: TextUnit = 13.sp,
    color: Color = Color.White,
    lineHeight: TextUnit = TextUnit.Unspecified,
    bold: FontWeight = FontWeight.SemiBold,
    maxLine: Int = 1,
    modifier: Modifier,
) {
    Text(
        text,
        modifier,
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Normal,
        fontWeight = bold,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        maxLines = maxLine,
        overflow = TextOverflow.Ellipsis,
        style =
            TextStyle(
                shadow =
                    Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f,
                    ),
            ),
    )
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun AutoScrollImagePagerPreView() {
    RatelappTheme {
        AutoScrollImagePager(null, null)
    }
}
