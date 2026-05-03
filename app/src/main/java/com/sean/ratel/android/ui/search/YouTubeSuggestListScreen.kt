package com.sean.ratel.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.SearchActivity
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_DISABLE
import com.sean.ratel.android.ui.theme.APP_SEARCH_SUGGEST_DIVIDER
import com.sean.ratel.android.ui.theme.APP_SEARCH_SUGGEST_TEXT
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil.getChosung

@Suppress("ktlint:standard:function-naming")
@Composable
fun YouTubeSuggestList(
    query: String,
    searchViewModel: SearchViewModel,
    selectItem: (String) -> Unit,
) {
    if (query.isNotEmpty()) {
        ListItemDisplayUi(searchViewModel, query, selectItem)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ListItemDisplayUi(
    searchViewModel: SearchViewModel,
    query: String,
    selectItem: (String) -> Unit,
) {
    val suggests by searchViewModel.searchSuggestList.collectAsState()

    val context = LocalContext.current as SearchActivity

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
                        .wrapContentSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    SuggestListView(suggests, query, selectItem)
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SuggestListView(
    items: List<String>,
    query: String,
    selectItem: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161616))
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    RoundedCornerShape(12.dp),
                ),
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 5.dp, bottom = 5.dp),
        ) {
            items(count = items.size) { index ->

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectItem(items[index]) }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = APP_SEARCH_FILTER_DISABLE,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text =
                            buildAnnotatedString {
                                val item = items[index]
                                val isChosungMatch =
                                    !item.startsWith(
                                        query,
                                        ignoreCase = true,
                                    ) && getChosung(item).startsWith(query)

                                if (item.startsWith(query, ignoreCase = true)) {
                                    // 일반 하이라이트
                                    withStyle(
                                        SpanStyle(
                                            color = APP_TEXT_COLOR,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    ) {
                                        append(item.take(query.length))
                                    }
                                    append(item.substring(query.length))
                                } else if (isChosungMatch) {
                                    // 초성 매칭 - query 길이만큼 첫 글자 하이라이트
                                    withStyle(
                                        SpanStyle(
                                            color = APP_TEXT_COLOR,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    ) {
                                        append(item.substring(0, query.length))
                                    }
                                    append(item.substring(query.length))
                                } else {
                                    append(item)
                                }
                            },
                        fontSize = 13.sp,
                        color = APP_SEARCH_SUGGEST_TEXT,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = APP_SEARCH_SUGGEST_DIVIDER,
                        thickness = 0.5.dp,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
fun RankingPreView() {
    RatelappTheme {
    }
}
