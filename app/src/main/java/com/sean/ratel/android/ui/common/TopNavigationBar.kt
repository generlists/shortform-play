package com.sean.ratel.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun TopNavigationBar(
    titleResourceId: Int = R.string.title,
    titleString: String? = null,
    historyBack: () -> Unit,
    isShareButton: Boolean,
    runSetting: () -> Unit,
    filterButton: Boolean,
    // 필터 상태 변경을 위한 콜백 추가
    onFilterChange: (Int) -> Unit = {},
    items: List<String> = listOf(),
    isTopicScreen: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (isTopicScreen) {
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                // 80%
                                Color(0xCC000000),
                                Color.Transparent,
                            ),
                    )
                } else {
                    Brush.linearGradient(
                        // 동일한 빨간색으로 단일 색상 처리
                        colors = listOf(APP_BACKGROUND, APP_BACKGROUND),
                    )
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier =
                Modifier
                    .size(32.dp)
                    .clickable {
                        historyBack()
                    },
        )

        Text(
            titleString ?: stringResource(titleResourceId),
            modifier =
                Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp),
            // Text의 크기를 내용에 맞게 설정
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        if (isShareButton) {
            Column(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
                    .padding(end = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val context = LocalContext.current
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    Icon(
                        imageVector = Icons.Outlined.IosShare,
                        contentDescription = null,
                        tint = Color.White,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clickable {
                                    runSetting()
                                },
                    )
                }
            }
        }
        if (filterButton) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
            ) {
                DropDownMenuComposable(
                    Color.White,
                    ImageVector.vectorResource(R.drawable.ic_sort_list),
                    modifer = Modifier.align(Alignment.End),
                ) { menuExpanded, onMenuDismiss ->
                    FilterItem(
                        menuExpanded = menuExpanded,
                        onFilterChange = { newState ->
                            onFilterChange(newState) // 필터 상태 변경
                            onMenuDismiss()
                        },
                        onMenuDismiss = onMenuDismiss,
                        items,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FilterItem(
    menuExpanded: Boolean,
    onFilterChange: (Int) -> Unit,
    onMenuDismiss: () -> Unit,
    items: List<String>,
) {
    DropdownMenu(
        modifier =
            Modifier
                .wrapContentSize()
                .padding(5.dp),
        expanded = menuExpanded,
        offset = DpOffset(0.dp, 0.dp),
        onDismissRequest = onMenuDismiss,
    ) {
        Column(Modifier.wrapContentSize()) {
            for (i in 0 until items.size) {
                Row(Modifier.clickable(onClick = { onFilterChange(i) })) {
                    Text(items.get(i))
                } // 각 아이템에 대해 Compose 함수 호출
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun TopNavigationBarPreview() {
    RatelappTheme {
//        TopNavigationBar(titleResourceId = R.string.setting, historyBack = {
//        }, isShareButton = false, runSetting = {}, filterButton = true, filterAction = 0, onFilterChange = {},listOf("a","b"),{})
    }
}
