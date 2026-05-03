package com.sean.ratel.android.ui.search

import SearchFilterScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdTarget
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.InterstitialAdPage
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.common.preview.ShortsVideoParameterProvider
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_BORDER
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_DISABLE
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
    var loading by remember { mutableStateOf(true) }
    val adLoading by mainViewModel.interstitialAdStart.collectAsState(initial = null)

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

    // 광고
    if (adLoading?.route == Destination.Search.route) {
        InterstitialAdPage(
            adTarget =
                AdTarget(
                    Destination.Home.Main.TopicListDetail.route,
                    adLoading?.adStart ?: true,
                ),
            interstitialAdManager = mainViewModel.interstitialAdManager,
            setAdLoading = {
                it?.let {
                    mainViewModel.setInterstitialAdStart(it.route, it.adStart)
                }
            },
            adInitState = mainViewModel.adMobinitState,
            loading = loading,
            setLoading = {
                loading = it
            },
            itemSize = Integer.MAX_VALUE,
        )
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
                    SearchComposeUi(
                        mainViewModel,
                        searchViewModel,
                        adViewModel,
                        {},
                    )
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
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF161616))
                .padding(4.dp),
    ) {
        Row {
            tabs.forEachIndexed { index, searchTabs ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTabIndex == index) APP_FILTER_BACKGROUND else Color.Transparent,
                            ).border(
                                width = if (selectedTabIndex == index) 0.5.dp else 0.dp,
                                color = if (selectedTabIndex == index) APP_SEARCH_FILTER_BORDER else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                            ).clickable {
                                selectedTabIndex = index
                                selectedIndx(index)
                                RLog.d("hbungshin", "selectedTabIndex : $selectedTabIndex")
                                sendGA(selectedTabIndex, searchViewModel)
                            }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(searchTabs.title),
                        fontSize = 13.sp,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Medium else FontWeight.Normal,
                        color = if (selectedTabIndex == index) Color.White else APP_SEARCH_FILTER_DISABLE,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
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
        // TabUi(SearchTabs.ARCHIVE, false)
        // RowCategoryList(categoryIndex = 0, list, null,null)
    }
}
