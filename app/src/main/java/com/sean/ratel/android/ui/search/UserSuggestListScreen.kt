package com.sean.ratel.android.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.LoadBanner
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun UserSuggestListScreen(
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    val userSuggestList by searchViewModel.userSuggestList.collectAsState()
    val isLoading by searchViewModel.isSuggestLoading.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    if (!isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.main_shorts_user_suggest),
                Modifier
                    .wrapContentSize()
                    .padding(top = 10.dp, bottom = 10.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = APP_TEXT_COLOR,
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
    if (userSuggestList.isNotEmpty()) {
        UserSuggestUI(adViewModel, searchViewModel, userSuggestList, selectItem)
    } else {
        Box(Modifier.fillMaxWidth().height(48.dp))
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UserSuggestUI(
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    items: List<SearchResultModel>,
    selectItem: (String) -> Unit,
) {
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()

    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .wrapContentSize()
                .padding(top = 10.dp),
        ) {
            LoadBanner(Destination.Search.route, adViewModel, AdBannerLocation.TOP)
        }

        Box(
            Modifier
                .fillMaxSize()
                .then(
                    if (adBannerLoadingComplete.value.first) {
                        Modifier.padding(top = adBannerLoadingComplete.value.second.dp + 20.dp)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            UserSuggestListView(items, searchViewModel, selectItem)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UserSuggestListView(
    items: List<SearchResultModel>,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(top = 5.dp, bottom = 5.dp),
    ) {
        items(count = items.size) { index ->
            UserSuggestsItems(
                index,
                items[index],
                searchViewModel,
                selectItem,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun UserSuggestsItems(
    index: Int,
    item: SearchResultModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = Color.White.copy(alpha = 0.2f)),
            ) {
                selectItem(item.searchKeyword)
                searchViewModel.sendGALog(
                    screenName = GASplashAnalytics.SCREEN_NAME.get(SEARCH_SCREEN) ?: "",
                    eventName = GASplashAnalytics.Event.SELECT_SEARCH_USER_SUGGEST_ITEM_CLICK,
                    actionName = GASplashAnalytics.Action.SELECT,
                    parameter =
                        mapOf(
                            GASplashAnalytics.Param.VIDEO_ID to (item.videoId ?: ""),
                        ),
                )
            }.background(Color.Black)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 왼쪽 썸네일 + 텍스트
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(0.85f) // weight 대신 비율 기반
                    .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 썸네일
            Surface(
                shape = RoundedCornerShape(6.dp),
                modifier =
                    Modifier
                        .height(34.dp)
                        .aspectRatio(1.7f),
            ) {
                NetworkImage(
                    url = item.thumbnail ?: "",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 텍스트
            Text(
                text = item.searchKeyword,
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFEEEEEE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    LocalTextStyle.current.copy(
                        shadow =
                            Shadow(
                                color = Color(0x80000000),
                                offset = Offset(1f, 1f),
                                blurRadius = 4f,
                            ),
                    ),
            )
        }

        // 오른쪽 X 아이콘
        IconButton(
            onClick = { searchViewModel.removeSuggestKeyWord(item) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(end = 4.dp)
                    .align(Alignment.CenterVertically),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_suggest_small_close),
                contentDescription = "Remove",
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun UserSuggestPreView() {
    RatelappTheme {
        // TitleArea("인기 숏폼(조회순)")
        // HorizontalScrollView(imageUrls)
//        ListItemList(
//            ViewType.ChannelSearchRanking,
//            Destination.Home.Main.RankingChannelMore.route,
//            listOf(MainShortsModel()),
//            null,
//            null,
//            null,
//            { } , rememberLazyListState()
//        )
        // RecentlyWatchItems(null,null,)
    }
}
