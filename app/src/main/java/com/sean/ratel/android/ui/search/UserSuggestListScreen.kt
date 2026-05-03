package com.sean.ratel.android.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.SearchResultModel
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.home.setting.SettingViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_DISABLE
import com.sean.ratel.android.ui.theme.APP_SEARCH_SUGGEST_DIVIDER
import com.sean.ratel.android.ui.theme.APP_SEARCH_SUGGEST_TEXT
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.TimeUtil.RelativeLang
import com.sean.ratel.android.utils.TimeUtil.formatRelativeDate
import com.sean.ratel.android.utils.TimeUtil.toRelativeLangByCountry

@Suppress("ktlint:standard:function-naming")
@Composable
fun UserSuggestListScreen(
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
        UserSuggestUI(searchViewModel, userSuggestList, selectItem)
    } else {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UserSuggestUI(
    searchViewModel: SearchViewModel,
    items: List<SearchResultModel>,
    selectItem: (String) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
    ) {
        Box(
            Modifier
                .wrapContentSize(),
            contentAlignment = Alignment.CenterStart,
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
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = items.size) { index ->
            SearchHistoryItem(items[index], searchViewModel, selectItem)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchHistoryItem(
    item: SearchResultModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
    settingViewModel: SettingViewModel = hiltViewModel(),
) {
    val locale by settingViewModel.locale.collectAsState()
    val lang = locale?.toRelativeLangByCountry() ?: RelativeLang.KO

    val displayDate =
        formatRelativeDate(
            createdAtMillis = item.saveTime,
            lang = lang,
        )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication =
                        ripple(
                            bounded = true,
                            color = Color.White.copy(alpha = 0.2f),
                        ),
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
                }.clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(14.dp),
                ).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(APP_SEARCH_SUGGEST_DIVIDER)
                    .border(0.5.dp, APP_FILTER_BACKGROUND, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            NetworkImage(
                url = item.thumbnail ?: "",
                imageLoader = mainViewModel.imageLoader,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                loadComplete = {
                },
            )
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = APP_TEXT_COLOR,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.searchKeyword,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = APP_SEARCH_SUGGEST_TEXT,
            )
            Text(
                text = displayDate,
                fontSize = 11.sp,
                color = APP_SEARCH_FILTER_DISABLE,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        // 오른쪽 X 아이콘
        IconButton(
            onClick = { searchViewModel.removeSuggestKeyWord(item) },
            modifier =
                Modifier
                    .wrapContentWidth()
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
