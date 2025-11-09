package com.sean.ratel.android.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.REMAIN_AD_MARGIN
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

    if (userSuggestList.isNotEmpty()) {
        UserSuggestUI(adViewModel, searchViewModel, userSuggestList, selectItem)
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.main_shorts_user_suggest),
                Modifier.wrapContentSize().padding(top = 10.dp, bottom = 10.dp),
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

    Scaffold(
        containerColor = APP_BACKGROUND,
    ) { innerPadding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding(),
                    top = 0.dp,
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            val topMargin = 20.dp

            Box(
                Modifier
                    .wrapContentSize()
                    .padding(top = topMargin),
            ) {
                LoadBanner(Destination.Search.route, adViewModel, AdBannerLocation.TOP)
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (adBannerLoadingComplete.value.first) {
                            Modifier.padding(top = adBannerLoadingComplete.value.second.dp + topMargin + REMAIN_AD_MARGIN)
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
            .height(48.dp)
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
            .padding(start = 7.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.wrapContentSize().weight(0.3f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                ) {
                    NetworkImage(
                        url = item.thumbnail ?: "",
                        contentDescription = null,
                        modifier =
                            Modifier
                                .aspectRatio(1.7f)
                                .wrapContentHeight(),
                    )
                }
            }
//            NetworkImage(
//                url = item.thumbnail ?: "",
//                contentDescription = null,
//                modifier =
//                    Modifier
//                        .clip(CircleShape)
//                        .width(24.dp)
//                        .height(24.dp),
//                ContentScale.Fit,
//                R.drawable.ic_play_icon,
//                R.drawable.ic_play_icon,
//                R.drawable.ic_play_icon,
//            )
            Text(
                text =
                    item.searchKeyword,
                Modifier
                    .padding(start = 5.dp, top = 5.dp, bottom = 5.dp)
                    .width(200.dp),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
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

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .weight(0.2f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = 5.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(
                    onClick = {
                        searchViewModel.removeSuggestKeyWord(item)
                    },
                    modifier =
                        Modifier
                            .size(24.dp)
                            // 아이콘 크기 설정
                            .padding(start = 5.dp),
                ) {
                    Image(
                        // 이미지 리소스
                        painter = painterResource(id = R.drawable.ic_suggest_small_close),
                        contentDescription = "Search Cancel Icon",
                        modifier =
                            Modifier
                                .height(32.dp)
                                .width(32.dp),
                    )
                }
            }
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
