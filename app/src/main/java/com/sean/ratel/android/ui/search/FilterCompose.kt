package com.sean.ratel.android.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun DatePickerCompose(pickerState: DatePickerState) {
    CompositionLocalProvider(
        LocalContentColor provides Color.Black,
    ) {
        DatePicker(
            title = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(APP_BACKGROUND)
                        .height(56.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        stringResource(R.string.search_date),
                        modifier =
                            Modifier
                                .wrapContentSize()
                                .padding(start = 5.dp),
                        // Text의 크기를 내용에 맞게 설정
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            },
            state = pickerState,
            showModeToggle = false,
            colors =
                DatePickerDefaults.colors(
                    containerColor = APP_BACKGROUND,
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.Gray,
                    subheadContentColor = Color.Gray,
                    navigationContentColor = APP_TEXT_COLOR,
                    yearContentColor = Color.White,
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = APP_TEXT_COLOR,
                    currentYearContentColor = APP_TEXT_COLOR,
                    selectedYearContentColor = APP_TEXT_COLOR,
                    disabledDayContentColor = Color.White.copy(alpha = 0.4f),
                ),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryRowLayoutCompose(
    categories: List<YouTubeCategory>,
    changeCategory: (YouTubeCategory) -> Unit,
    onCategorySelected: YouTubeCategory,
) {
    // 1. LazyRow 대신 일반 Row 사용
    // FlowRow를 사용하여 공간이 부족하면 자동으로 다음 줄로 넘어갑니다.

    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth(),
        // .padding(horizontal = 16.dp),
        // 항목 간 가로, 세로 간격 설정
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        categories.forEach { category ->
            val isSelected = onCategorySelected.categoryName == category.categoryName
            RLog.d("hbungshin", "category : ${category.categoryName} ,  selectValue: ${ onCategorySelected.categoryName}")

            // FlowRow 내부에서는 weight(1f)를 사용하지 않습니다.
            // 각 항목은 콘텐츠 크기만큼 너비를 차지합니다.
            Box(
                modifier =
                    Modifier
                        // 최소 너비를 지정하여 너무 작아지는 것을 방지할 수 있습니다. (선택 사항)
                        // .widthIn(min = 60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) APP_TEXT_COLOR else Color.DarkGray,
                            shape = RoundedCornerShape(12.dp),
                        ).background(Color.Black)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable {
                            // onCategorySelected.categoryName = category.categoryName
                            changeCategory(category)
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.categoryName,
                    color = if (isSelected) APP_TEXT_COLOR else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DailyBottomButtonCompose(
    initialSelectDate: Long?,
    pickerState: DatePickerState,
    resetCategory: (Boolean) -> Unit,
    onCategorySelected: YouTubeCategory,
    onApply: (Long?, YouTubeCategory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val context = LocalContext.current

        /** 초기화 버튼 */
        OutlinedButton(
            modifier =
                Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication =
                            ripple(
                                bounded = true,
                                color = Color.White.copy(alpha = 0.2f),
                            ),
                        onClick = {
                        },
                    ),
            onClick = {
                pickerState.selectedDateMillis = initialSelectDate
                resetCategory(true)
            },
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = APP_TEXT_COLOR,
                ),
        ) {
            Text(
                stringResource(R.string.search_daily_reset),
                Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
            )
        }

        /** 적용 버튼 */
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { onApply(pickerState.selectedDateMillis, onCategorySelected) },
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = APP_TEXT_COLOR,
                    containerColor = APP_TEXT_COLOR,
                ),
            border =
                BorderStroke(
                    width = 1.dp,
                    color = APP_TEXT_COLOR,
                ),
        ) {
            Text(
                stringResource(R.string.search_daily_apply),
                Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black,
                style =
                    TextStyle(
                        shadow =
                            Shadow(
                                color = Color.White.copy(alpha = 0.8f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f,
                            ),
                    ),
            )
        }
    }
}
