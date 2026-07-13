package com.sean.ratel.android.ui.home.shortform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_FILTER_DIABLE_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog

@Suppress("ktlint:standard:function-naming")
@Composable
fun CategoryFilterRow(
    categoryMap: Map<String, List<MainShortsModel>>,
    filterKey: String?,
    adIndexSet: Set<Int>,
    tabHeight: Int,
    adBannerSize: Int,
    listState: LazyListState,
    scope: CoroutineScope,
    modifier: Modifier,
) {
    val categories = categoryMap.keys.toList()
    val categoryList = categoryMap.values.toList()
    var selectedCategory by remember(categories) { mutableStateOf(filterKey ?: (categories.firstOrNull() ?: "")) }
    RLog.d("CategoryFilterRow", "selectedCategory : $filterKey ,  size : ${categories.size}")
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val adMargin = 16
    val bannerHeightPx =
        with(density) {
            (adBannerSize + adMargin).dp.roundToPx()
        }
    val tabPositions = remember { mutableStateMapOf<Int, Int>() }
    Row(
        modifier =
            modifier
                .background(APP_BACKGROUND)
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val density = LocalDensity.current

        val tabHeightPx = with(density) { (tabHeight).dp.roundToPx() } // 실제 탭 높이로 변경
        categories.forEachIndexed { index, category ->

            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .onGloballyPositioned { coordinates ->
                            tabPositions[index] = coordinates.positionInParent().x.toInt()
                        }.background(
                            if (selectedCategory == category) APP_TEXT_COLOR else APP_FILTER_BACKGROUND,
                        ).clickable {
                            scope.launch {
                                selectedCategory = category
                                val adCount = adIndexSet.count { it <= index }

                                val targetIndex = index + adCount + 2
                                RLog.d(
                                    "KKKKMMMMM",
                                    "adCount :  $adBannerSize , targetIndex $targetIndex index : $index category : $category",
                                )
                                listState.animateScrollToItem(
                                    index = targetIndex,
                                    scrollOffset = if (adCount == 0) -tabHeightPx else -(tabHeightPx + bannerHeightPx),
                                )
                            }
                        }.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (categoryList.isNotEmpty()) {
                    Text(
                        text = categoryList[index][0].shortsVideoModel?.categoryName ?: stringResource(R.string.etc),
                        fontSize = 12.sp,
                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedCategory == category) APP_BACKGROUND else APP_FILTER_DIABLE_COLOR,
                    )
                }
            }
        }
        LaunchedEffect(categories) {
            if (categories.isNotEmpty()) {
                scope.launch {
                    val index = categories.indexOf(selectedCategory)

                    if (index != -1) {
                        val adCount = adIndexSet.count { it <= index }
                        val targetIndex = index + adCount
                        RLog.d(
                            "CategoryFilteRow",
                            "adCount : $adCount, , " +
                                "targetIndex : $targetIndex , " +
                                " tabHeightPx : $tabHeightPx ,  " +
                                "bannerHeightPx : $bannerHeightPx",
                        )
                        delay(500)
                        listState.animateScrollToItem(
                            index = targetIndex,
                            scrollOffset = if (adCount == 0) -tabHeightPx else -(tabHeightPx + bannerHeightPx),
                        )
                        tabPositions[index]?.let { x ->

                            scrollState.animateScrollTo(x)
                        }
                    }
                }
            }
        }
    }
}
