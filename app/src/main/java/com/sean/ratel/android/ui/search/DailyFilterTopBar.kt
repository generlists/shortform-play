package com.sean.ratel.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun DailyFilterTopBar(
    selectDay: String,
    selectedCategory: String,
    filterClick: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(APP_BACKGROUND),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .wrapContentHeight(),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(APP_BACKGROUND),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.search_date) + " : ",
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(5.dp),
                    // Text의 크기를 내용에 맞게 설정
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    selectDay,
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(start = 35.dp),
                    // Text의 크기를 내용에 맞게 설정
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(APP_BACKGROUND),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.search_category) + " : ",
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(5.dp),
                    // Text의 크기를 내용에 맞게 설정
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    selectedCategory,
                    modifier =
                        Modifier
                            .wrapContentSize()
                            .padding(start = 5.dp),
                    // Text의 크기를 내용에 맞게 설정
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .weight(0.2f)
                .align(Alignment.CenterVertically),
        ) {
            Box(
                Modifier,
                contentAlignment = Alignment.CenterEnd,
            ) {
                IconButton(onClick = { filterClick(true) }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_sort_list),
                        contentDescription = null,
                        tint = APP_TEXT_COLOR,
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun TopNavigationBarPreview() {
    RatelappTheme {
        DailyFilterTopBar("20251113", "엔터테인먼트", {})
    }
}
