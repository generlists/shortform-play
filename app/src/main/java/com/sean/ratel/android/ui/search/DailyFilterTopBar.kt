package com.sean.ratel.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.UiState
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_DISABLE
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_EMTPY_BORDER
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_EMTPY_MESSAGE
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_UNSELECT
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_USELECT
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailyFilterTopBar(
    selectDay: String?,
    selectedCategory: String,
    uiState: UiState<List<MainShortsModel>>,
    filterClick: (Boolean) -> Unit,
) {
    // 날짜 선택 카드
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.outlineVariant)
                .border(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    RoundedCornerShape(14.dp),
                ).padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 날짜
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "📅", fontSize = 14.sp)
            Text(
                text = selectDay ?: stringResource(R.string.main_topic_channel_count),
                fontSize = 12.sp,
                color = if (selectDay != null) APP_SEARCH_FILTER_UNSELECT else APP_SEARCH_FILTER_USELECT,
            )
        }

        // 구분선
        Box(
            modifier =
                Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(Color(0xFF2A2A2A)),
        )

        // 카테고리
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "⚡", fontSize = 14.sp)
            Text(
                text = selectedCategory,
                fontSize = 12.sp,
                color = APP_SEARCH_FILTER_UNSELECT,
            )
        }
        // filterClick(true)
        // 필터 아이콘
        Icon(
            Icons.Default.FilterList,
            contentDescription = null,
            tint = APP_TEXT_COLOR,
            modifier =
                Modifier
                    .size(16.dp)
                    .clickable {
                        filterClick(true)
                    },
        )
    }
    Spacer(Modifier.height(40.dp))

    // 빈 상태 안내
    if (uiState == UiState.Idle) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier =
                    Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(APP_SEARCH_FILTER_EMTPY_MESSAGE)
                            .border(0.5.dp, APP_SEARCH_FILTER_EMTPY_BORDER, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = APP_SEARCH_FILTER_DISABLE,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.search_daily_shorts_blank_text),
                    fontSize = 13.sp,
                    color = APP_SEARCH_FILTER_DISABLE,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
        }
    } else {
        Spacer(Modifier.height(16.dp))
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun TopNavigationBarPreview() {
    RatelappTheme {
        DailyFilterTopBar("20251113", "엔터테인먼트", UiState.Idle, {})
    }
}
