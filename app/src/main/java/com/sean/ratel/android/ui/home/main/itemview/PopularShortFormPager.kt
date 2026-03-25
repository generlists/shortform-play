package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil.pixelToDp

@OptIn(ExperimentalFoundationApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun PopularShortFormPager(
    viewModel: MainViewModel,
    videoSearchList: ShortFormVideoSearchList,
) {
    if (videoSearchList.searchList.isEmpty()) return

    val configuration = LocalConfiguration.current
    val title = videoSearchList.title
    val shortFormSearchList =
        if (videoSearchList.searchList.size > 10) {
            videoSearchList.searchList.subList(0, 9)
        } else {
            videoSearchList.searchList
        }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
        ) {
            Spacer(Modifier.height(8.dp))
            TitleArea(viewModel, title)
            val startIndex = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % shortFormSearchList.size
            val listState = rememberLazyListState(startIndex)

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = Int.MAX_VALUE) { index ->
                    val selectedIndex = index % shortFormSearchList.size
                    val item = shortFormSearchList[selectedIndex]
                    PopularVideoItem(
                        viewModel,
                        selectedIndex,
                        item,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PopularVideoItem(
    viewModel: MainViewModel?,
    selectedIndex: Int,
    item: MainShortsModel?,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
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
            Modifier.wrapContentSize().onGloballyPositioned { coordinates ->
                componentWidth = coordinates.size.width
            },
        ) {
            Box(
                modifier = Modifier.widthIn(max = 220.dp),
            ) {
                var backgroundColor by remember { mutableStateOf(Color.Black) }

                Card(
                    modifier = Modifier.fillMaxWidth().aspectRatio(9 / 16f),
                    // 가로 길이에 맞춰 높이 1:1 확보 (이미지 크기 결정)
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                ) {
                    if (LocalInspectionMode.current) {
                        // Preview 모드에서 로컬 이미지 사용
                        Image(
                            // 로컬 이미지
                            painter = painterResource(id = R.drawable.sample_image),
                            contentDescription = "Preview Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.aspectRatio(0.5625f).fillMaxSize(),
                        )
                    } else {
                        viewModel?.imageLoader?.let {
                            NetworkImage(
                                modifier = Modifier.aspectRatio(0.5625f).fillMaxSize(),
                                imageLoader = it,
                                url = item?.shortsVideoModel?.thumbNail ?: "",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                loadComplete = {},
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val resourceId = R.drawable.ic_youtube
                        Image(
                            painter = painterResource(resourceId),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                Box(
                    modifier =
                        Modifier.wrapContentSize().align(Alignment.BottomEnd).background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Black.copy(alpha = 0.0f),
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.7f),
                                        ),
                                ),
                        ),
                ) {
                    Column(
                        modifier = Modifier.wrapContentSize(),
                    ) {
                        Box(
                            Modifier
                                .width(pixelToDp(context, componentWidth.toFloat()).dp)
                                .padding(top = 5.dp, end = 5.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Text(
                                text = videoTitle,
                                Modifier
                                    .width(pixelToDp(context, componentWidth.toFloat()).dp)
                                    .padding(
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
                                                offset = Offset(2f, 2f),
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

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TimeArea(duration: String) {
    Box(
        Modifier.fillMaxWidth().fillMaxHeight(),
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

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TitleArea(
    viewModel: MainViewModel,
    title: String,
) {
    var textWidth by remember { mutableStateOf(0f) }

    Box(
        Modifier.fillMaxWidth().wrapContentHeight(),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(
            Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
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
                Modifier.fillMaxWidth().wrapContentHeight().padding(end = 7.dp),
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
@Preview(showBackground = true)
@Composable
fun HorizontalScrollViewPreView() {
    RatelappTheme {
        // TitleArea("인기 숏폼(조회순)")
        // HorizontalScrollView(imageUrls)
        // ItemUi(null, 0, null)
    }
}
