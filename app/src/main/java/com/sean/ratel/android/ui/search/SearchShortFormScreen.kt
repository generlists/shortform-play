package com.sean.ratel.android.ui.search

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.SearchActivity
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.dto.SearchShortsResponse
import com.sean.ratel.android.data.log.GAKeys.SEARCH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.FullScreenToggleView
import com.sean.ratel.android.ui.end.LoadingArea
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil.getAppLocaleByStringResource
import com.sean.ratel.android.utils.UIUtil.localeFromCountryCode

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchComposeUi(
    searchViewModel: SearchViewModel,
    adViewModel: AdViewModel,
    finish: () -> Unit,
) {
    RatelappTheme {
        val context = LocalContext.current as SearchActivity
        val insetPaddingValue = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val query = remember { mutableStateOf("") }
        val searchDataComplete = searchViewModel.searchDataComplete.collectAsState()
        val searchSuggestComplete = searchViewModel.searchSuggestComplete.collectAsState()
        val currentLocale by searchViewModel.currentLocale.collectAsState()

        val uiState = rememberSaveable { mutableStateOf(SearchUiState.UserSuggest) }
        val fromSelection = remember { mutableStateOf(false) }
        val sessionId by searchViewModel.sessionId.collectAsState()
        val searchRetryState = searchViewModel.searchRetry.collectAsState()
        val searchLoading = searchViewModel.searchLoading.collectAsState()
        val apiState = searchViewModel.uiState.collectAsState()
        Scaffold(
            modifier =
                Modifier
                    .background(APP_BACKGROUND)
                    .padding(top = insetPaddingValue),
            topBar = {
                TopSearchBar(
                    Modifier,
                    searchLoading.value,
                    query.value,
                    queryChange = { q ->
                        query.value = q
                    },
                    fromSelection,
                    historyBack = {
                        searchViewModel.requestResetSession(sessionId)
                        finish()
                    },
                )
            },
        ) { innerPaddingModifier ->

            FullScreenToggleView(Destination.Search.route)

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(APP_BACKGROUND)
                        .padding(innerPaddingModifier),
            ) {
                RLog.d("SearchComposeUi", "current uiState: ${uiState.value}")
                when (uiState.value) {
                    // 처음 저장된 자동완성 리스트
                    SearchUiState.UserSuggest -> {
                        UserSuggestListScreen(adViewModel, searchViewModel) { userSelect ->
                            val locale = localeFromCountryCode(currentLocale)
                            val keyword = "$userSelect + ${getAppLocaleByStringResource(context, currentLocale)}"
                            query.value = userSelect
                            fromSelection.value = true

                            searchViewModel.requestYouTubeSearchResult(context,keyword, 0, locale.country, locale.toLanguageTag())
                            uiState.value = SearchUiState.Searching
                        }
                    }

                    // 타이핑 중 → 서버 서제스트 리스트
                    SearchUiState.TypingSuggest -> {
                        val keyword = query.value
                        val locale = localeFromCountryCode(currentLocale)

                        searchViewModel.requestYoutubeSuggestList(keyword, locale.language)

                        when (apiState.value) {
                            is UiState.Success<*> -> {
                                RLog.d("SearchComposeUi","TypingSuggest apiState : Success")
                                LaunchedEffect(searchDataComplete.value) {
                                    if (searchViewModel.searchDataComplete.value) {
                                        searchViewModel.searchDataComplete(false)
                                    }
                                }
                            }
                            is UiState.Error -> ErrorView(context,searchViewModel,apiState.value)


                            else -> Unit

                        }

                        if (searchSuggestComplete.value) {
                            YouTubeSuggestList(
                                query.value,
                                adViewModel,
                                searchViewModel,
                            ) { suggest ->
                                val newKeyword = "$suggest + ${getAppLocaleByStringResource(context, currentLocale)}"
                                query.value = suggest
                                fromSelection.value = true
                                RLog.d("SearchComposeUi","클릭 자동완성 아이템 : ${uiState.value}")

                                searchViewModel.requestYouTubeSearchResult(context,newKeyword, 0, locale.country, locale.toLanguageTag())
                                uiState.value = SearchUiState.Searching
                            }
                        }
                    }

                    // 로딩 중 (검색 요청)
                    SearchUiState.Searching -> {
                        when (apiState.value) {
                            is UiState.Loading -> {
                                LoadingArea(true)
                                LocalSoftwareKeyboardController.current?.hide()
                            }
                            is UiState.Success<*> -> {
                                LaunchedEffect(searchDataComplete.value) {
                                    if (searchViewModel.searchDataComplete.value) {
                                        uiState.value = SearchUiState.Result
                                        searchViewModel.searchDataComplete(false)
                                    }
                                }
                            }
                            is UiState.Error -> {
                                ErrorView(context,searchViewModel,apiState.value)

                            }
                            is UiState.Idle -> Unit
                        }
                    }

                    // 검색 결과 화면
                    SearchUiState.Result -> {
                        KeyWordSearchListScreen(query.value, adViewModel, searchViewModel)
                        searchViewModel.sendGALog(
                            screenName = GASplashAnalytics.SCREEN_NAME.get(SEARCH_SCREEN) ?: "",
                            eventName = GASplashAnalytics.Event.SEARCH_MORE_VIEW,
                            actionName = GASplashAnalytics.Action.VIEW,
                            parameter =
                                mapOf(
                                    GASplashAnalytics.Param.SEARCH_TYPE to uiState.value.toString(),
                                ),
                        )
                    }
                }

                // 공통 로딩 처리 (서버 요청 중)
                if (uiState.value == SearchUiState.Searching) {
                    LoadingArea(true)
                } else {
                    LoadingArea(false)
                }
            }

            LaunchedEffect(query.value) {
                    if (query.value.isNotEmpty() && uiState.value != SearchUiState.Searching) {
                        uiState.value = SearchUiState.TypingSuggest

                    } else if (query.value.isEmpty() && uiState.value != SearchUiState.Searching) {
                        uiState.value = SearchUiState.UserSuggest
                    }
            }
        }

        if(searchRetryState.value){
            RetryButton() {
                val locale = localeFromCountryCode(currentLocale)
                val newKeyword = "${query.value} + ${getAppLocaleByStringResource(context, currentLocale)}"
                fromSelection.value = true
                searchViewModel.requestYouTubeSearchResult(context,newKeyword, 0, locale.country, locale.toLanguageTag())
                uiState.value = SearchUiState.Searching
                searchViewModel.setSearchRetry(false)

            }

        }

    }
}

@Composable
fun ErrorView(context: Context, searchViewModel: SearchViewModel, uiState: UiState<SearchShortsResponse>) {
    LoadingArea(false)
    LocalSoftwareKeyboardController.current?.hide()
    LaunchedEffect(uiState) {
        val message = (uiState as UiState.Error).message
        RLog.d("SearchComposeUi","message : $message")
        if(message == context.getString(R.string.api_empty_error)){
            searchViewModel.setSearchRetry(true)
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}

enum class SearchUiState {
    UserSuggest, // 처음 저장된 데이터
    TypingSuggest, // 타이핑 중
    Searching, // 로딩 중
    Result, // 결과 표시
}
