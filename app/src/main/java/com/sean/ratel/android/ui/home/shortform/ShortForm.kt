package com.sean.ratel.android.ui.home.shortform

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.DrawablePainter
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.YouTubeUtils.getCategoryName
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.InLineAdaptiveBanner
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.common.preview.ShortsVideoParameterProvider
import com.sean.ratel.android.ui.home.ViewType
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.Background
import com.sean.ratel.android.ui.theme.Background_op_20
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.ui.theme.THUMBNAIL_BACKGROUND
import com.sean.ratel.android.utils.ComposeUtil.isAtBottom
import com.sean.ratel.android.utils.ComposeUtil.pxToDp
import com.sean.ratel.android.utils.UIUtil.validationIndex
import com.sean.ratel.ui.theme.Red
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ShortForm"

@Suppress("ktlint:standard:function-naming")
// 카테고리 별로
@Composable
fun ShortForm(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    viewModel: ShortFormViewModel,
    adViewModel: AdViewModel,
) {
    val data = viewModel.categoryByContents.collectAsState()
    ShortFormView(modifier, data.value, mainViewModel, viewModel, adViewModel)
    val coroutine = rememberCoroutineScope()

    LaunchedEffect(mainViewModel.tabClicked) {
        coroutine.launch {
            mainViewModel.tabClicked.collect { s ->
                s?.let {
                    if (s == Destination.Home.ShortForm.route) {
                        viewModel.initData()
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormView(
    modifier: Modifier = Modifier,
    data: Map<String, List<MainShortsModel>>,
    mainViewModel: MainViewModel,
    viewModel: ShortFormViewModel,
    adViewModel: AdViewModel,
) {
    // val bottomBarHeight = remember { adViewModel.bottomBarHeight.value } //구글정책상 수정
    Column(
        modifier
            .fillMaxSize()
            .background(Background),
        // .padding(bottom = bottomBarHeight.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(Modifier.fillMaxSize()) {
                ScrollableTabBar()
                VerticalScrollWithHorizontalItems(data, mainViewModel, viewModel, adViewModel)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun VerticalScrollWithHorizontalItems(
    items: Map<String, List<MainShortsModel>>,
    mainViewModel: MainViewModel,
    viewModel: ShortFormViewModel,
    adViewModel: AdViewModel?,
) {
    val categorySize = items.values.size
    val targetIndexList =
        remember { validationIndex(Destination.Home.ShortForm.route, categorySize) }

    LazyColumn(
        // 세로 스크롤 전체 화면 채우기
        modifier = Modifier.fillMaxSize(),
        // 세로 항목 간 간격 설정
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        itemsIndexed(items.values.toList()) { index, _ ->

            if (targetIndexList.contains(index) &&
                RemoteConfig.getRemoteConfigBooleanValue(
                    RemoteConfig.BANNER_AD_VISIBILITY,
                )
            ) {
                InLineAdaptiveBanner(adViewModel)
            }
            ShortFormList(index, mainViewModel, items, viewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormList(
    currentIndex: Int,
    mainViewModel: MainViewModel,
    items: Map<String, List<MainShortsModel>>,
    viewModel: ShortFormViewModel,
) {
    val categoryTitle = remember { items.keys.toList() }

    val categoryList = items.values.toList()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                // 각 세로 항목은 가로로 꽉 채움
                .wrapContentHeight(),
    ) {
        Box(Modifier.background(Background)) {
            Text(
                text =
                    categoryList[currentIndex][0].shortsVideoModel?.categoryName
                        ?: stringResource(R.string.etc),
                Modifier
                    .padding(start = 7.dp, top = 7.dp, bottom = 7.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
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

        RowCategoryList(currentIndex, categoryTitle, categoryList, viewModel, mainViewModel)
    }

    val size = items.size
    if (size - 1 == currentIndex) {
        Spacer(
            Modifier
                .height(15.dp)
                .fillMaxWidth(),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun RowCategoryList(
    categoryIndex: Int,
    categoryTitle: List<String>,
    horizontalItems: List<List<MainShortsModel>>,
    viewModel: ShortFormViewModel,
    mainViewModel: MainViewModel,
) {
    // 각 세로 항목에 가로 스크롤 되는 LazyRow 추가
    val items = horizontalItems[categoryIndex] // rember 로 더보기 묶으면 갱신이 안된다
    val categoryTitleKey = remember { categoryTitle[categoryIndex] }

    val listState = rememberLazyListState()
    var moreLoading by remember { mutableStateOf(false) }
    val indexList = viewModel.moreIndex.collectAsState()
    val coroutine = rememberCoroutineScope()

    val isAtBottom = listState.isAtBottom()
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            RLog.d(
                TAG,
                "isBottom categoryIndex : ${getCategoryName(categoryTitleKey)}, index : ${
                    indexList.value.get(categoryTitleKey)
                }",
            )
            moreLoading = true
            val indexValue = indexList.value[categoryTitleKey] ?: 0
            viewModel.setMoreEvent(categoryTitleKey, indexValue + 1)

            viewModel.moreIndex.collectLatest { index ->
                if (index.isNotEmpty()) {
                    val value = index[categoryTitleKey] ?: 0
                    val maxIndex = viewModel.maxMoreIndex(categoryTitleKey)

                    if (value in 1..<maxIndex) {
                        RLog.d(
                            TAG,
                            "moreIndex : $index,  maxIndex : ${
                                viewModel.maxMoreIndex(categoryTitleKey)
                            }",
                        )

                        viewModel.moreContent(categoryTitleKey, value)
                        coroutine.launch {
                            delay(100)
                            moreLoading = false
                            listState.scrollBy(50f)
                        }
                    } else {
                        moreLoading = false
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        LazyRow(
            Modifier.fillMaxSize(),
            listState,
            // 가로 항목 간 간격 설정
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            mainViewModel.shortFormVideoData(viewModel.categoryByContents.value)
                            mainViewModel.goEndContent(
                                Destination.Home.ShortForm.route,
                                ViewType.ShortFormVideo,
                                index,
                                categoryTitleKey,
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
                                AppCompatResources.getDrawable(context, R.drawable.clip_lg_default)
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
                                modifier = Modifier.fillMaxWidth(),
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
        if (moreLoading) {
            ProgressBar()
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ProgressBar() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.CenterEnd,
    ) {
        CircularProgressIndicator(
            Modifier
                .size(18.dp)
                .padding(1.dp),
            strokeWidth = 3.dp,
            color = Red,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ScrollableTabBar() {
    val tabs = remember { ShortFormTab.entries.toTypedArray().asList() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Red,
        // 탭 바의 좌우 여백
        edgePadding = 8.dp,
        indicator = {},
    ) {
        tabs.forEachIndexed { index, item ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
            ) {
                // 아이콘과 텍스트를 가로로 배치
                Row(
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(top = 3.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        // 아이콘 설정
                        painter = painterResource(item.icon),
                        // 아이콘 설명
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier =
                            Modifier
                                .width(24.dp)
                                .height(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 아이콘과 텍스트 사이에 간격 추가
                    Text(
                        text = stringResource(item.title),
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontFamily = FontFamily.SansSerif,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun ShortFormPreView() {
    RatelappTheme {
        ScrollableTabBar()
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun RowCategoryListPreView(
    @PreviewParameter(
        ShortsVideoParameterProvider::class,
        limit = 1,
    ) list: List<List<MainShortsModel>>,
) {
    RLog.d("", "$list")
    RatelappTheme {
        // RowCategoryList(categoryIndex = 0, list, null,null)
    }
}
