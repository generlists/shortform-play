package com.sean.ratel.android.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.SearchActivity
import com.sean.ratel.android.ui.ad.AdBannerLocation
import com.sean.ratel.android.ui.ad.AdBannerView
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme
import so.smartlab.common.ad.admob.data.model.AdMobBannerState

@Suppress("ktlint:standard:function-naming")
@Composable
fun YouTubeSuggestList(
    query: String,
    adViewModel: AdViewModel,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    if (query.isNotEmpty()) {
        ListItemDisplayUi(searchViewModel, selectItem)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemDisplayUi(
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val suggests by searchViewModel.searchSuggestList.collectAsState()

    val context = LocalContext.current as SearchActivity
    val adFixedBannerState by mainViewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }
    when {
        adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
            adSize = (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
        }

        else -> {
            adSize = 0
        }
    }
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
                        top = 10.dp,
                    ),
            ) {
                Box(
                    Modifier
                        .wrapContentSize()
                        .padding(top = adSize.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    SuggestListView(suggests, selectItem)
                }
                Box(
                    Modifier.fillMaxSize(),
                ) {
                    AdBannerView(context, Destination.Search.route, AdBannerLocation.TOP)
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
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
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
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication =
                    ripple(
                        bounded = true,
                        color = Color.White.copy(alpha = 0.2f),
                    ),
                onClick = {
                    selectItem(item)
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = item,
            Modifier.fillMaxSize().padding(10.dp),
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
