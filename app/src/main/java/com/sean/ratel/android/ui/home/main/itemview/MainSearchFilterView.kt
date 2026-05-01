package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.ui.home.TopicFilterType
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_DIABLE_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@Suppress("ktlint:standard:function-naming")
@Composable
fun MainSearchFilterView(
    selectedFilter: Int,
    setSelectedFilter: (Int) -> Unit = {},
    filters: List<String>,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(APP_BACKGROUND),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(filters) { index, filter ->
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedFilter == index) APP_TEXT_COLOR else APP_FILTER_BACKGROUND,
                        ).clickable {
                            setSelectedFilter(index)

                            val filterType =
                                when (selectedFilter) {
                                    0 -> TopicFilterType.Popular
                                    1 -> TopicFilterType.Views
                                    2 -> TopicFilterType.Subscriber
                                    else -> TopicFilterType.Popular
                                }
                        }.padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = filter,
                    fontSize = 12.sp,
                    fontWeight = if (selectedFilter == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedFilter == index) APP_BACKGROUND else APP_FILTER_DIABLE_COLOR,
                )
            }
        }
    }
}
