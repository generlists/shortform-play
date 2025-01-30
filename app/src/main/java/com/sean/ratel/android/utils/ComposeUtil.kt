package com.sean.ratel.android.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
                    .background(Color.Transparent)
                    .padding(bottom = adLoadingComplete.value.second.dp)
            } else {
                Modifier
                    .fillMaxSize()
            },
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
        return remember(this) {
            derivedStateOf {
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                if (layoutInfo.totalItemsCount == 0) {
                    false
                } else {
                    val lastVisibleItem = visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
                    val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                    (lastVisibleItem.index == layoutInfo.totalItemsCount - 1) &&
                        (lastVisibleItem.offset + lastVisibleItem.size <= viewportHeight)
                }
            }
        }.value
    }
}
