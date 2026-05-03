@file:OptIn(ExperimentalMaterial3Api::class)

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.data.log.GAKeys.CATEGORY_NAME
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.end.LoadingArea
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.search.DailyFilterTopBar
import com.sean.ratel.android.ui.search.DailySearchFilterBottomSheet
import com.sean.ratel.android.ui.search.DailySearchResultScreen
import com.sean.ratel.android.ui.search.SearchFilterActions
import com.sean.ratel.android.ui.search.SearchViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchFilterScreen(
    searchViewModel: SearchViewModel,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val selectedDate by searchViewModel.selectedDate.collectAsState()
    val selectedCategory by searchViewModel.selectedCategory.collectAsState()
    val apiState = searchViewModel.dailyUiState.collectAsState()
    val categories = searchViewModel.youtubeCategory.collectAsState()
    val context = LocalContext.current
    val dailySearchAll = searchViewModel.dailySearchShortformList.collectAsState()
    val deepLinkSearch = searchViewModel.deepLinkTab.collectAsState()
    val deepLinkDate = searchViewModel.deepLinkDate.collectAsState()
    val deepLinkCategory = searchViewModel.getFindCategory(searchViewModel.deepLinkCategory.collectAsState().value)
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    RLog.d("KKKKKK", "categoryName : $selectedCategory")
    RLog.d("KKKKKK", "categories : $categories")

    Scaffold(
        topBar = {
            DailyFilterTopBar(
                selectedDate ?: stringResource(R.string.search_select_date),
                selectedCategory.categoryName,
                uiState = apiState.value,
                { click ->
                    showFilterSheet = click
                    gaSend(
                        searchViewModel,
                        GASplashAnalytics.Event.SELECT_SEARCH_DAILY_FILTER_BTN_CLICK,
                        GASplashAnalytics.Action.CLICK,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(APP_BACKGROUND)
                    .padding(padding),
        ) {
            if (showFilterSheet) {
                SearchFilterScreen({
                    showFilterSheet = it
                }, searchViewModel, mainViewModel)
            }

            when (apiState.value) {
                is UiState.Loading -> {
                    LoadingArea(true)
                }

                is UiState.Success<*> -> {
                    RLog.d("SearchViewModel", "apiState : Success")
                    DailySearchResultScreen(adViewModel, searchViewModel)
                    mainViewModel.setSearchCategoryDailyShortFormVieo(dailySearchAll.value)
                    LoadingArea(false)
                    gaSend(searchViewModel, GASplashAnalytics.Event.SELECT_SEARCH_DAILY_RESULT, GASplashAnalytics.Action.VIEW)
                }

                is UiState.Error -> {
                    DailyErrorView(context, searchViewModel, apiState.value)
                }

                else -> {
                    Unit
                }
            }
        }
        LaunchedEffect(deepLinkSearch.value) {
            deepLinkSearch.value?.let {
                val date =
                    LocalDate
                        .parse(deepLinkDate.value, dateFormatter)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli()
                applyDailySearch(context, searchViewModel, {}, date, deepLinkCategory)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchFilterScreen(
    showFilterSheet: (Boolean) -> Unit,
    searchViewModel: SearchViewModel,
    mainViewModel: MainViewModel,
) {
    val selectedDate by searchViewModel.selectedDate.collectAsState()
    val categories = searchViewModel.youtubeCategory.collectAsState()
    val selectedCategory by searchViewModel.selectedCategory.collectAsState()
    val context = LocalContext.current

    RLog.d("dailymainscreen", "selectedDate : $selectedDate")

    val searchFilterAction =
        SearchFilterActions(
            changeCategory = { category ->
                searchViewModel.selectCategory(category)
                gaSend(
                    searchViewModel,
                    GASplashAnalytics.Event.SELECT_SEARCH_DAILY_FILTER_CATEGORY_SELECT,
                    category.categoryName,
                )
            },
            resetCategory = { reSet ->
                if (reSet) {
                    searchViewModel.selectCategory(searchViewModel.getInitCategory())
                    searchViewModel.resetDate()
                    gaSend(
                        searchViewModel,
                        GASplashAnalytics.Event.SELECT_SEARCH_DAILY_FILTER_RESET_BTN_CLICK,
                        GASplashAnalytics.Action.CLICK,
                    )
                }
            },
            onApply = { date, category ->

                RLog.d("deeplink", "date : $date")
                mainViewModel.setInterstitialAdStart(Destination.Search.route, true)
                applyDailySearch(context, searchViewModel, showFilterSheet, date, category)
                gaSend(
                    searchViewModel,
                    GASplashAnalytics.Event.SELECT_SEARCH_DAILY_FILTER_APPLY_BTN_CLICK,
                    GASplashAnalytics.Action.CLICK,
                )
            },
            onDismiss = {
                showFilterSheet(false)
            },
        )

    DailySearchFilterBottomSheet(
        selectedDate,
        categories.value,
        selectedCategory = selectedCategory,
        searchFilterAction = searchFilterAction,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailyErrorView(
    context: Context,
    searchViewModel: SearchViewModel,
    uiState: UiState<List<MainShortsModel>>,
) {
    LoadingArea(false)
    LocalSoftwareKeyboardController.current?.hide()
    LaunchedEffect(uiState) {
        val message = (uiState as UiState.Error).message
        RLog.d("SearchComposeUi", "message : $message")
        if (message == context.getString(R.string.api_empty_error)) {
            searchViewModel.setSearchRetry(true)
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

private fun gaSend(
    searchViewModel: SearchViewModel,
    eventName: String,
    actionName: String,
    categoryName: String? = null,
) {
    searchViewModel.sendGALog(
        screenName = GASplashAnalytics.SCREEN_NAME[SEARCH_SCREEN] ?: "",
        eventName = eventName,
        actionName = actionName,
        parameter = if (categoryName != null) mapOf<String, String>(CATEGORY_NAME to categoryName) else mapOf(),
    )
}

private fun applyDailySearch(
    context: Context,
    searchViewModel: SearchViewModel,
    showFilterSheet: (Boolean) -> Unit,
    date: Long?,
    category: YouTubeCategory?,
) {
    searchViewModel.resetData()
    showFilterSheet(false)

    date?.let {
        searchViewModel.onDateSelected(context, it, category ?: searchViewModel.getInitCategory())
    }
}
