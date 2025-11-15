package com.sean.ratel.android.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.ui.ad.AdViewModel

object ComposeUtil {
    @Composable
    fun pxToDp(px: Int): Int {
        // LocalDensity를 사용하여 px를 dp로 변환
        with(LocalDensity.current) {
            return px.toDp().value.toInt()
        }
    }

    @Suppress("ktlint:standard:function-naming")
    @Composable
    fun ViewBottomMargin(adViewModel: AdViewModel?) {
        val adLoadingComplete = adViewModel?.adBannerLoadingCompleteAndGetAdSize?.collectAsState()
        Box(
            if (adLoadingComplete?.value?.first == true) {
                Modifier
                    .fillMaxSize()
                    .padding(bottom = adLoadingComplete.value.second.dp)
            } else {
                Modifier
                    .fillMaxSize()
            },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Spacer(
                if (adLoadingComplete?.value?.first == true) {
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = adLoadingComplete.value.second.dp)
                } else {
                    Modifier.fillMaxSize()
                },
            )
        }
    }

    @Composable
    fun LazyListState.isAtBottom(): Boolean {
        // 스크롤 방향 감지를 위한 previousOffset
        var previousOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }

        // 아래 방향 스크롤인지
        val isScrollingDown by remember {
            derivedStateOf {
                val current = firstVisibleItemScrollOffset
                val down = current > previousOffset
                previousOffset = current
                down
            }
        }

        return remember(this) {
            derivedStateOf {
                val layoutInfo = layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo

                if (layoutInfo.totalItemsCount == 0 || visibleItems.isEmpty()) {
                    return@derivedStateOf false
                }

                val lastVisibleItem = visibleItems.last()

                // viewport end (실제 화면 맨 아래)
                val viewportEnd = layoutInfo.viewportEndOffset

                // 마지막 아이템이 리스트의 마지막인지
                val isLastItem = lastVisibleItem.index == layoutInfo.totalItemsCount - 1

                // 마지막 아이템이 화면에 완전히 보이는지
                val isFullyVisible =
                    lastVisibleItem.offset + lastVisibleItem.size <= viewportEnd

                RLog.d(
                    "AT_BOTTOM",
                    "isScrollingDown=$isScrollingDown, lastIndex=${lastVisibleItem.index}, " +
                            "isLastItem=$isLastItem, isFullyVisible=$isFullyVisible, viewportEnd=$viewportEnd"
                )

                // 아래로 스크롤 중이고 + 마지막 아이템 완전 노출일 때만 true
                isLastItem && isFullyVisible && isScrollingDown
            }
        }.value
    }
}
