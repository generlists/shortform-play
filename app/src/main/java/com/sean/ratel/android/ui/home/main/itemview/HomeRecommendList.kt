package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.MAIN_AD_KEY
import com.sean.ratel.android.data.common.RemoteConfig.MAX_RECOMMEND_SIZE
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.RecommendList
import com.sean.ratel.android.data.dto.ShortsChannelModel
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.common.preview.MainParameterProvider
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.MAIN_TITLE_UNDER_LINE
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeRecommendList(
    mainViewModel: MainViewModel,
    recommendList: RecommendList,
) {
    TitleArea(mainViewModel, recommendList.title)
    val list = recommendList.recommendList
    val items =
        if (list.isNotEmpty()) {
            list.subList(
                0,
                RemoteConfig.getRemoteConfigIntValue(MAX_RECOMMEND_SIZE),
            )
        } else {
            return
        }
    var i = 0
    while (i < items.size) {
        if (i + 2 < items.size) {
            RecommendItemBoxRow(
                3,
                listOf(
                    items[i].apply { itemPosition = i },
                    items[i + 1].apply { itemPosition = i + 1 },
                    items[i + 2].apply { itemPosition = i + 2 },
                ),
                mainViewModel,
            )
            i += 3
        } else {
            val isOdd = isOddMainList(i)
            if (i == items.size - 1) {
                RecommendItemBoxRow(
                    3,
                    listOf(
                        items[i].apply { itemPosition = items.size - 2 },
                        items[i].apply { itemPosition = items.size - 1 },
                        MainShortsModel(items.size - 1),
                    ),
                    mainViewModel,
                    isLastOdd = isOdd,
                )
            }
            i += 1
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TitleArea(
    viewModel: MainViewModel?,
    title: String,
) {
    var textWidth by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 10.dp, bottom = 20.dp, top = 20.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Box(
            Modifier
                .width(UIUtil.pixelToDp(context, textWidth).dp)
                .height(8.dp)
                .background(MAIN_TITLE_UNDER_LINE),
            contentAlignment = Alignment.BottomCenter,
        ) {}
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .wrapContentSize(),
            ) {
                Column(Modifier.wrapContentSize()) {
                    Text(
                        text =
                        title,
                        Modifier
                            // .padding(start = 7.dp, top = 7.dp, bottom = 7.dp)
                            .wrapContentSize(),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        onTextLayout = { textLayoutResult: TextLayoutResult ->
                            textWidth = textLayoutResult.size.width.toFloat() // 렌더링된 픽셀 크기
                        },
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
                            viewModel?.goMoreContent(
                                Destination.Home.Main.RecommendMore.route,
                                ViewType.Recommend,
                            )
                            // 로딩바
                            viewModel?.setIsHomeVisible(true)
                            viewModel?.sendGALog(
                                Event.SCREEN_VIEW,
                                Destination.Home.Main.RecommendMore.route,
                                ViewType.Recommend,
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

@Suppress("ktlint:standard:function-naming")
@Composable
fun RecommendItemBoxRow(
    rowSize: Int,
    items: List<MainShortsModel>,
    viewModel: MainViewModel?,
    isLastOdd: Boolean = false,
) {
    Row(
        // 좌우 패딩 추가
        Modifier.padding(vertical = 1.5.dp, horizontal = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        for (i in 0 until rowSize) {
            val shortVideoModel = remember { items[i].shortsVideoModel }
            val shortChannelModel = remember { items[i].shortsChannelModel }
            val position = remember { items[i].itemPosition }

            val videoThumbnail =
                rememberSaveable(items[i].shortsVideoModel) {
                    shortVideoModel?.thumbNail ?: ""
                }
            val videoDuration =
                rememberSaveable(items[i].shortsVideoModel) {
                    shortVideoModel?.duration ?: "00:00"
                }

            Box(
                Modifier.weight(1f) then (
                    if (shortVideoModel != null) {
                        Modifier.clickable {
                            viewModel?.setItemClicked(Destination.Home.Main.route, position)
                            viewModel?.goEndContent(
                                Destination.Home.Main.route,
                                ViewType.Recommend,
                                position,
                            )
                            viewModel?.sendGALog(
                                Event.SCREEN_VIEW,
                                Destination.YouTube.dynamicRoute(
                                    items[i].shortsChannelModel?.channelId ?: "",
                                ),
                                ViewType.Recommend,
                                items[i].shortsChannelModel?.channelId,
                                items[i].shortsVideoModel?.videoId,
                            )
                        }
                    } else {
                        Modifier
                    }
                ) then (
                    if (isLastOdd) {
                        Modifier.aspectRatio(
                            0.5625f,
                        )
                    } else {
                        Modifier
                    }
                ),
            ) {
                if (videoThumbnail.isNotEmpty()) {
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
                            )
                        }
                    }

                    Column(
                        if (isLastOdd) {
                            Modifier
                                .background(Color.Transparent)
                                .align(Alignment.BottomEnd)
                                .padding(top = 7.dp)
                        } else {
                            Modifier
                                .background(Background_op_20)
                                .align(Alignment.BottomEnd)
                                .padding(top = 7.dp)
                        },
                    ) {
                        VideoArea(shortVideoModel)
                        ChannelArea(modifier = Modifier, shortChannelModel)

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

@Suppress("ktlint:standard:function-naming")
@Composable
fun VideoArea(shortVideoModel: ShortsVideoModel?) {
    val videoTitle =
        rememberSaveable(shortVideoModel) {
            shortVideoModel?.title ?: ""
        }
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
        fontSize = 12.sp,
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
fun ChannelArea(
    modifier: Modifier,
    channel: ShortsChannelModel?,
) {
    val channelThumbnail =
        rememberSaveable(channel) {
            channel?.channelThumbNail ?: ""
        }
    val channelTitle =
        rememberSaveable(channel) {
            channel?.channelTitle ?: ""
        }
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

private fun isOddMainList(index: Int) = (index + listOf(RemoteConfig.getRemoteConfigIntValue(MAIN_AD_KEY)).size) % 2 != 0

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun HeaderPreview(
    @PreviewParameter(
        MainParameterProvider::class,
        limit = 1,
    ) list: List<MainShortsModel>,
) {
    RLog.d("", "$list")
    RatelappTheme {
        TitleArea(null, "당신이 좋아할만한")
    }
}
