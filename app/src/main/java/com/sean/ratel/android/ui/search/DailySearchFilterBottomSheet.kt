@file:OptIn(ExperimentalMaterial3Api::class)

package com.sean.ratel.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.SERVICE_START_DATE
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SEARCH_BOTTOM_CONTAIN_COLOR
import com.sean.ratel.android.ui.theme.APP_SEARCH_BOTTOM_SCRIM_COLOR
import com.sean.ratel.android.ui.theme.APP_SEARCH_LINE_COLOR
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun DailySearchFilterBottomSheet(
    startDate: String?,
    categories: List<YouTubeCategory>,
    selectedCategory: YouTubeCategory,
    searchFilterAction: SearchFilterActions,
) {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val today = LocalDate.now().format(dateFormatter)
    val todayDate = LocalDate.now()
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = APP_SEARCH_BOTTOM_CONTAIN_COLOR,
        scrimColor = APP_SEARCH_BOTTOM_SCRIM_COLOR,
        onDismissRequest = searchFilterAction.onDismiss,
        modifier = Modifier.fillMaxWidth(),
        dragHandle = null,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(APP_BACKGROUND)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
        ) {
            RLog.d("bottomsheet", "startMillis : $startDate")

            val initialDisplayStartDate =
                if (startDate == null) {
                    getInitialDate(
                        LocalDate.parse(
                            SERVICE_START_DATE,
                            dateFormatter,
                        ),
                    )
                } else {
                    LocalDate
                        .parse(startDate, dateFormatter)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                        .toEpochMilli()
                }

            // display 범위 관련
            val rangeStartDate = LocalDate.parse(SERVICE_START_DATE, dateFormatter)
            val startMillis = getStartMillis(rangeStartDate)
            val nonSelectableStartMillis = getNonDisplaySelectedDate()

            RLog.d("bottomsheet", "initialDisplayStartDate : $initialDisplayStartDate")

            val datePickerState =
                rememberDatePickerState(
                    initialSelectedDateMillis = initialDisplayStartDate,
                    initialDisplayedMonthMillis = initialDisplayStartDate,
                    yearRange = rangeStartDate.year..todayDate.year,
                    selectableDates =
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                utcTimeMillis >= startMillis && utcTimeMillis < nonSelectableStartMillis
                        },
                )
            LaunchedEffect(initialDisplayStartDate) {
                RLog.d("bottomsheet", "[LaunchedEffect] 갱신 시도: $initialDisplayStartDate")
                datePickerState.selectedDateMillis = initialDisplayStartDate
                datePickerState.displayedMonthMillis = initialDisplayStartDate
            }

            BottomSheetContent(
                initialDisplayStartDate,
                pickerState = datePickerState,
                selectedCategory = selectedCategory,
                categories = categories,
                changeCategory = searchFilterAction.changeCategory,
                resetCategory = searchFilterAction.resetCategory,
                onApply = searchFilterAction.onApply,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BottomSheetContent(
    initialSelectDate: Long?,
    pickerState: DatePickerState,
    selectedCategory: YouTubeCategory,
    changeCategory: (YouTubeCategory) -> Unit,
    resetCategory: (Boolean) -> Unit,
    categories: List<YouTubeCategory>,
    onApply: (Long?, YouTubeCategory) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .background(APP_BACKGROUND)
                .padding(16.dp),
    ) {
        DatePickerCompose(pickerState)

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(APP_SEARCH_LINE_COLOR),
            contentAlignment = Alignment.BottomCenter,
        ) {}
        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                stringResource(R.string.search_category),
                modifier =
                    Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(20.dp))
        val scrollState = rememberScrollState()
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .verticalScroll(scrollState),
        ) {
            CategoryRowLayoutCompose(
                categories = categories,
                changeCategory = changeCategory,
                onCategorySelected = selectedCategory,
            )
        }

        Spacer(Modifier.height(20.dp))

        DailyBottomButtonCompose(
            initialSelectDate = initialSelectDate,
            pickerState = pickerState,
            resetCategory = resetCategory,
            onCategorySelected = selectedCategory,
            onApply = onApply,
        )
    }
}

private fun getInitialDate(startDate: LocalDate): Long {
    val startDate = startDate
    val todayDate = LocalDate.now()
    val yesterdayDate = todayDate.minusDays(1)

    val startMillis =
        startDate
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

    // 2025년 11월 26일 00:00 UTC
    val yesterdayMillis =
        yesterdayDate
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

    // 2. 초기 선택 날짜 결정 (yesterdayDate가 startDate보다 이전인 경우 대비)
    val finalInitialSelectedMillis =
        if (yesterdayMillis >= startMillis) {
            yesterdayMillis
        } else {
            startMillis
        }
    return finalInitialSelectedMillis
}

private fun getDisplayStartDate() =
    LocalDate
        .now()
        .withDayOfMonth(15)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

private fun getNonDisplaySelectedDate() =
    LocalDate
        .now()
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

private fun getStartMillis(startDate: LocalDate) =
    startDate
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
