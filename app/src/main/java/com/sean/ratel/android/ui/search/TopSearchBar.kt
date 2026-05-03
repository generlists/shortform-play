package com.sean.ratel.android.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SEARCH_FILTER_USELECT
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import kotlinx.coroutines.delay

@Suppress("ktlint:standard:function-naming")
@Composable
fun TopSearchBar(
    modifier: Modifier,
    loading: Boolean,
    query: MutableState<String>,
    queryChange: (String) -> Unit,
    fromSuggestion: MutableState<Boolean>,
    fromDeepLink: MutableState<Boolean>,
    uiState: SearchUiState,
    historyBack: () -> Unit,
) {
    val isCancelButton = remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp)
            .background(APP_BACKGROUND),
        // verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .height(50.dp)
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .border(1.5.dp, APP_TEXT_COLOR, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                    // .padding(start = 8.dp, end = 5.dp, top = 1.dp, bottom = 1.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )

                    SearchTextField(
                        query = query,
                        searchLoading = loading,
                        onQueryChange = {
                            if (it.length <= 50) queryChange(it)

                            isCancelButton.value = it.isNotEmpty()
                        },
                        Modifier,
                        fromSuggestion,
                        fromDeepLink,
                    )
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = APP_BACKGROUND,
                        modifier = Modifier.size(14.dp),
                    )
                }
                if (isCancelButton.value) {
                    SearchCancelButton(
                        modifier,
                        { q ->
                            isCancelButton.value = q.isNotEmpty()
                            queryChange("")
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // 최근 검색 라벨
        if (uiState == SearchUiState.UserSuggest) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.search_recently),
                        fontSize = 11.sp,
                        color = APP_SEARCH_FILTER_USELECT,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SearchTextField(
    query: MutableState<String>,
    searchLoading: Boolean,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fromSuggestion: MutableState<Boolean>,
    fromDeepLink: MutableState<Boolean>,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    if ((fromSuggestion.value || fromDeepLink.value) || query.value.isEmpty()) {
        val text = query.value
        textFieldValue =
            textFieldValue.copy(
                text = text,
                selection = TextRange(text.length),
                composition = null,
            )
        fromSuggestion.value = false
    }

    if (!fromDeepLink.value) {
        LaunchedEffect(Unit) {
            delay(100) // Compose 구성 안정 후 요청
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Box(
        modifier =
            modifier
                .height(56.dp)
                .padding(start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (query.value.isEmpty()) {
            Text(
                text = stringResource(R.string.search_topbar_hint),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 3.dp, bottom = 3.dp),
            )
        }

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onQueryChange(newValue.text) // 필요 시 외부 state로 전달
            },
            singleLine = true,
            textStyle =
                TextStyle(
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    platformStyle = PlatformTextStyle(includeFontPadding = true),
                ),
            cursorBrush = SolidColor(Color.White),
            modifier =
                Modifier
                    .matchParentSize()
                    .padding(5.dp)
                    .focusRequester(focusRequester)
                    .align(Alignment.CenterStart),
            enabled = !searchLoading,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                ),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SearchCancelButton(
    modifier: Modifier,
    resetQuery: (String) -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(end = 5.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        IconButton(
            onClick = {
                resetQuery("")
            },
            modifier =
                modifier
                    .size(24.dp)
                    // 아이콘 크기 설정
                    .padding(start = 5.dp),
        ) {
            Image(
                // 이미지 리소스
                painter = painterResource(id = R.drawable.ic_search_canel),
                contentDescription = "Search Cancel Icon",
                modifier =
                    Modifier
                        .height(32.dp)
                        .width(32.dp),
            )
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
