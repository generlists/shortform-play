package com.sean.ratel.android.ui.home.main.itemview

import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.DrawablePainter
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.TrendsShortFormList
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.MAIN_TITLE_UNDER_LINE
import com.sean.ratel.android.ui.theme.THUMBNAIL_BACKGROUND
import com.sean.ratel.android.utils.ComposeUtil.pxToDp
import com.sean.ratel.android.utils.UIUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun TrendShortsList(
    viewModel: MainViewModel,
    trendShortsData: TrendsShortFormList,
) {
    var textWidth by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    val trendShorts = viewModel.mainTrendShortsList.collectAsState().value
    val initDataKey = trendShortsData.event_list.keys.toList()[0] // 0번째

    Log.d("hbungshin", "initDataKey : $initDataKey")

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
    ) {
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
                    .wrapContentHeight()
                    .padding(bottom = 2.5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .wrapContentSize(),
                ) {
                    Text(
                        text = stringResource(R.string.main_trends_shorts_title),
                        Modifier
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
                            .wrapContentHeight()
                            .clickable {
                                viewModel.goMoreContent(
                                    Destination.Home.Main.TrendShortsMore.route,
                                    ViewType.TrendShortsMore,
                                    initDataKey,
                                )
                                // 로딩바
                                viewModel.setIsHomeVisible(true)
                                viewModel.sendGALog(
                                    Event.SCREEN_VIEW,
                                    Destination.Home.Main.TrendShortsMore.route,
                                    ViewType.MainTrendShorts,
                                )
                            },
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = APP_TEXT_COLOR,
                    )
                }
            }
        }

        RowsList(viewModel, trendShorts)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun RowsList(
    mainViewModel: MainViewModel,
    horizontalItems: List<MainShortsModel>,
) {
    // 각 세로 항목에 가로 스크롤 되는 LazyRow 추가
    val items = horizontalItems

    val listState = rememberLazyListState()

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 5.dp, end = 5.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        LazyRow(
            Modifier.fillMaxSize(),
            listState,
            // 가로 항목 간 간격 설정
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            // 가로 스크롤은 너비를 채우지만, 스크롤 가능
        ) {
            items(items.size) { index ->
                val videoThumbnail =
                    remember(items) {
                        items[index].shortsVideoModel?.thumbNail
                    }
                val channelThumbnail =
                    remember(items) {
                        items[index].shortsChannelModel?.channelThumbNail
                    }
                Box(
                    Modifier
                        .wrapContentSize()
                        .clickable {
                            mainViewModel.goEndContent(
                                Destination.Home.Main.route,
                                ViewType.MainTrendShorts,
                                index,
                            )
                            mainViewModel.sendGALog(
                                Event.SCREEN_VIEW,
                                Destination.YouTube.dynamicRoute(
                                    items[index].shortsChannelModel?.channelId ?: "",
                                ),
                                ViewType.MainTrendShorts,
                                items[index].shortsChannelModel?.channelId,
                                items[index].shortsVideoModel?.videoId,
                            )
                        },
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = 10.dp,
                    ) {
                        // Preview 모드에서 로컬 이미지 사용
                        if (LocalInspectionMode.current) {
                            val context = LocalContext.current
                            val placeholderDrawable =
                                AppCompatResources.getDrawable(context, R.drawable.ad_native_default_background)
                            placeholderDrawable?.let {
                                Image(
                                    // 로컬 이미지
                                    painter = DrawablePainter(placeholderDrawable),
                                    contentDescription = "Preview Image",
                                    contentScale = ContentScale.Fit,
                                    modifier =
                                        Modifier
                                            .width(pxToDp(480).dp)
                                            .height(pxToDp(360).dp)
                                            .background(
                                                THUMBNAIL_BACKGROUND,
                                            ),
                                )
                            }
                        } else {
                            //   실제 앱에서는 네트워크 이미지 사용
                            videoThumbnail?.let {
                                NetworkImage(
                                    url = it,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier =
                                        Modifier
                                            .width(pxToDp(480).dp)
                                            .height(pxToDp(360).dp)
                                            .background(
                                                THUMBNAIL_BACKGROUND,
                                            ),
                                )
                            }
                        }
                    }
                    Column(
                        Modifier
                            .background(Background_op_20)
                            .align(Alignment.BottomEnd)
                            .padding(top = 7.dp),
                    ) {
                        Text(
                            items.get(index).shortsVideoModel?.title ?: "no title",
                            Modifier
                                .width(pxToDp(480).dp)
                                .wrapContentHeight()
                                .padding(start = 7.dp, end = 7.dp, bottom = 5.dp)
                                .background(Color.Transparent),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
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
                        Row(
                            Modifier
                                .width(pxToDp(480).dp)
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
                                            .height(24.dp)
                                            .align(Alignment.CenterVertically),
                                )
                            } else {
                                channelThumbnail?.let {
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
                            Text(
                                text = items.get(index).shortsChannelModel?.channelTitle ?: "",
                                Modifier
                                    .wrapContentHeight()
                                    .width(pxToDp(180).dp)
                                    .alpha(0.9f)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 5.dp),
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
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

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 5.dp, bottom = 7.dp, end = 7.dp),
                                ) {
                                    Text(
                                        text =
                                            items.get(index).shortsVideoModel?.duration
                                                ?: "00:00",
                                        Modifier
                                            .wrapContentSize()
                                            .align(Alignment.CenterEnd)
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
                    }
                }
            }
        }
    }
}
