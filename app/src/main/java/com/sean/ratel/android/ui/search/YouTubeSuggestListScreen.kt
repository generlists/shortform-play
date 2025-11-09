package com.sean.ratel.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.data.common.STRINGS.REMAIN_AD_MARGIN
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.ad.LoadBanner
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun YouTubeSuggestList(
    query: String,
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    if (query.isNotEmpty()) {
        ListItemDisplayUi(adViewModel, searchViewModel, selectItem)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemDisplayUi(
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    val suggests by searchViewModel.searchSuggestList.collectAsState()
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()

    Scaffold(
        containerColor = APP_BACKGROUND,
    ) { innerPadding ->

        if (suggests.isNotEmpty()) {
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
                    SuggestListView(suggests, selectItem)
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SuggestListView(
    items: List<String>,
    selectItem: (String) -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(top = 5.dp, bottom = 5.dp),
    ) {
        items(count = items.size) { index ->
            SuggestsItems(
                index,
                items[index],
                selectItem,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SuggestsItems(
    index: Int,
    item: String,
    selectItem: (String) -> Unit,
) {
    Box(
        Modifier
            .wrapContentSize()
            .background(APP_BACKGROUND)
            .clickable(onClick = {
                selectItem(item)
            }),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = item,
            Modifier.fillMaxSize().padding(top = 10.dp, bottom = 10.dp),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
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

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun RankingPreView() {
    RatelappTheme {
    }
}
