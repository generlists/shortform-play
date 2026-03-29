package com.sean.ratel.android.ui.search

import SearchFilterScreen
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.preview.ShortsVideoParameterProvider
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

private const val TAG = "ShortForm"

@Suppress("ktlint:standard:function-naming")
// 카테고리 별로
@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    finish: () -> Unit,
) {
    val sessionId by searchViewModel.sessionId.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BackHandler(enabled = true) {
        searchViewModel.requestResetSession(sessionId)
        finish()
    }
    RatelappTheme {
        Scaffold(
            modifier =
                Modifier
                    .padding(top = insetPaddingValue)
                    .background(APP_BACKGROUND),
            topBar = {
                TopNavigationBar(
                    titleString = stringResource(R.string.search),
                    historyBack = {
                        searchViewModel.requestResetSession(sessionId)
                        finish()
                    },
                    isShareButton = false,
                    runSetting = {},
                    filterButton = false,
                    onFilterChange = {},
                    items = listOf(),
                )
            },
        ) { paddingValues ->

            FullScreenToggleView(Destination.Search.route)

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND)
                        .padding(paddingValues),
            ) {
                SearchMain(searchViewModel, adViewModel, mainViewModel)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchMain(
    searchViewModel: SearchViewModel,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
) {
    val searchType = rememberSaveable { mutableStateOf(SearchType.VideoSearch) }
    Spacer(Modifier.height(10.dp))
    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                ScrollableTabBar(
                    searchViewModel = searchViewModel,
                    selectedIndx = { currentIndex ->
                        when (currentIndex) {
                            0 -> {
                                searchType.value = SearchType.VideoSearch
                            }

                            1 -> {
                                searchType.value = SearchType.ArchiveSearch
                            }
                        }
                    },
                )
            }
            Spacer(Modifier.height(16.dp))

            when (searchType.value) {
                SearchType.VideoSearch -> {
                    SearchComposeUi(searchViewModel, adViewModel, {})
                }

                SearchType.ArchiveSearch -> {
                    SearchFilterScreen(
                        searchViewModel,
                        adViewModel,
                        mainViewModel,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ScrollableTabBar(
    searchViewModel: SearchViewModel,
    selectedIndx: (Int) -> Unit,
) {
    val tabs = remember { SearchTabs.entries.toTypedArray().asList() }
    val deepLinkTab by searchViewModel.deepLinkTab.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(if (deepLinkTab != null) 1 else 0) }
    RLog.d("deepLink", "selectedTabIndex : $selectedTabIndex ,  deepLinkTab $deepLinkTab")
    if (deepLinkTab != null) {
        selectedIndx(1)
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
        containerColor = MaterialTheme.colorScheme.outlineVariant,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier =
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(start = 40.dp, end = 20.dp),
                color = APP_TEXT_COLOR,
                height = 3.dp,
            )
        },
        divider = {},
    ) {
        tabs.forEachIndexed { index, item ->

            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    selectedIndx(index)
                    RLog.d("hbungshin", "selectedTabIndex : $selectedTabIndex")
                    sendGA(selectedTabIndex, searchViewModel)
                },
                modifier =
                    Modifier
                        .wrapContentHeight(),
            ) {
                TabUi(searchTabs = item, selectedTabIndex == index)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TabUi(
    searchTabs: SearchTabs,
    selectedItem: Boolean,
) {
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(
            Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 3.dp, bottom = 3.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(searchTabs.icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier =
                                Modifier
                                    .size(24.dp),
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = stringResource(searchTabs.title),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private fun sendGA(
    selectedTabIndex: Int,
    searchViewModel: SearchViewModel,
) {
    searchViewModel.sendGALog(
        screenName = GASplashAnalytics.SCREEN_NAME[SEARCH_SCREEN] ?: "",
        eventName =
            if (selectedTabIndex ==
                0
            ) {
                GASplashAnalytics.Event.SELECT_SEARCH_TAB_KEYWORD_CLICK
            } else {
                GASplashAnalytics.Event.SELECT_SEARCH_TAB_DAILY_CLICK
            },
        actionName = GASplashAnalytics.Action.CLICK,
        parameter = mapOf(),
    )
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun ShortFormPreView() {
    RatelappTheme {
        // ScrollableTabBar()
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SearchTabPreView(
    @PreviewParameter(
        ShortsVideoParameterProvider::class,
        limit = 1,
    ) list: List<List<MainShortsModel>>,
) {
    RLog.d("", "$list")
    RatelappTheme {
        TabUi(SearchTabs.ARCHIVE, false)
        // RowCategoryList(categoryIndex = 0, list, null,null)
    }
}

@Keep
enum class SearchTabs(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val destRoute: String,
) {
    SHORTS(
        R.string.search_type_keyword,
        R.drawable.ic_video_search,
        Destination.Home.ShortForm.route,
        Destination.Home.ShortForm.route,
    ),
    ARCHIVE(
        R.string.search_type_daily,
        R.drawable.ic_achive_search,
        Destination.Search.route,
        Destination.Search.route,
    ),
}

enum class SearchType {
    VideoSearch,
    ArchiveSearch,
}

data class SearchFilterActions(
    val changeCategory: (YouTubeCategory) -> Unit,
    val resetCategory: (Boolean) -> Unit,
    val onDismiss: () -> Unit,
    val onApply: (Long?, YouTubeCategory) -> Unit,
)
