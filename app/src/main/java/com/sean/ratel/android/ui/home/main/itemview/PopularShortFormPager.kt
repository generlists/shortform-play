package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.ShortFormVideoSearchList
import com.sean.ratel.android.data.dto.ShortsVideoModel
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.MAIN_TITLE_UNDER_LINE
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil
import kotlinx.coroutines.flow.distinctUntilChanged

val RATO = 0.5625

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun PopularShortFormPager(
    viewModel: MainViewModel,
    videoSearchList: ShortFormVideoSearchList,
) {
    if (videoSearchList.searchList.isEmpty()) return
    val pagerState =
        rememberPagerState(pageCount = {
            Int.MAX_VALUE
        })

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val contentPadding = (screenWidthDp * RATO) / 2
    val title = videoSearchList.title
    val shortFormSearchList =
        if (videoSearchList.searchList.size > 10) {
            videoSearchList.searchList.subList(0, 9)
        } else {
            videoSearchList.searchList
        }
    val page = viewModel.popularShortFormPager.collectAsState(initial = 0)
    Column(
        Modifier
            .fillMaxSize(),
    ) {
        TitleArea(viewModel, title)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                HorizontalPager(
                    modifier =
                        Modifier
                            .wrapContentHeight()
                            .width((screenWidthDp - 10).dp),
                    contentPadding = PaddingValues(horizontal = contentPadding.dp),
                    pageSpacing = 3.dp,
                    state = pagerState,
                ) { index ->
                    shortFormSearchList
                        .getOrNull(
                            index % (shortFormSearchList.size),
                        )?.let { item ->
                            val selectedIndex = index % (shortFormSearchList.size)
                            ItemUi(viewModel, selectedIndex, item)
                        }
                }
            }
        }

        LaunchedEffect(key1 = Unit, block = {
            var initPage = Int.MAX_VALUE / 2
            while (initPage % shortFormSearchList.size != 0) {
                initPage++
            }
            if (page.value == 0) {
                pagerState.scrollToPage(initPage)
            } else {
                pagerState.scrollToPage(page.value)
            }
        })

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .distinctUntilChanged() // 페이지 변경 이벤트만 수신
                .collect { currentPage ->
                    viewModel.setPopularShortFormPager(currentPage)
                }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ItemUi(
    viewModel: MainViewModel?,
    selectedIndex: Int,
    item: MainShortsModel?,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    viewModel?.goEndContent(
                        Destination.Home.Main.route,
                        ViewType.PopularSearchShortForm,
                        selectedIndex,
                    )
                    viewModel?.sendGALog(
                        Event.SCREEN_VIEW,
                        Destination.YouTube.dynamicRoute(item?.shortsChannelModel?.channelId ?: ""),
                        ViewType.PopularSearchShortForm,
                        item?.shortsChannelModel?.channelId,
                        item?.shortsVideoModel?.videoId,
                    )
                }.clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            PopularVideoArea(item?.shortsVideoModel)
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

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TitleArea(
    viewModel: MainViewModel,
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
                Modifier.wrapContentSize(),
            ) {
                Text(
                    text = title,
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
                                    color = Color.Black,
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
                        .clickable {
                            viewModel.goMoreContent(
                                Destination.Home.Main.PoplarShortFormMore.route,
                                ViewType.PopularSearchShortForm,
                            )
                            // 로딩바
                            viewModel.setIsHomeVisible(true)
                            viewModel.sendGALog(
                                Event.SCREEN_VIEW,
                                Destination.Home.Main.PoplarShortFormMore.route,
                                ViewType.PopularSearchShortForm,
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
fun PopularVideoArea(shortVideoModel: ShortsVideoModel?) {
    Box(Modifier.wrapContentSize()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
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
                    url = shortVideoModel?.thumbNail ?: "",
                    contentDescription = null,
                    modifier =
                        Modifier
                            .aspectRatio(0.5625f)
                            .fillMaxSize(),
                    ContentScale.Crop,
                    R.drawable. vertical_background,
                    R.drawable. vertical_background,
                    R.drawable. vertical_background,
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Background_op_20)
                .align(Alignment.BottomEnd)
                .padding(top = 7.dp, bottom = 7.dp),
        ) {
            Video(shortVideoModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun Video(shortVideoModel: ShortsVideoModel?) {
    val videoTitle =
        rememberSaveable(shortVideoModel) {
            shortVideoModel?.title ?: ""
        }
    val videoDuration =
        rememberSaveable(shortVideoModel) {
            shortVideoModel?.duration ?: ""
        }
    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            videoTitle,
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 7.dp, end = 7.dp, bottom = 7.dp)
                .background(Color.Transparent),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 3,
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

    TimeArea(videoDuration)
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun HorizontalScrollViewPreView() {
    RatelappTheme {
        // TitleArea("인기 숏폼(조회순)")
        // HorizontalScrollView(imageUrls)
        // ItemUi(null, 0, null)
    }
}
