package com.sean.ratel.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormBottomSheetDialog(
    items: Array<String>?,
    selectedValue: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = APP_BACKGROUND,
        contentColor = APP_SUBTITLE_TEXT_COLOR,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(APP_BACKGROUND),
        ) {
            items?.let {
                items(items, key = { it }) { item ->
                    Text(
                        text = item,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item == selectedValue) APP_TEXT_COLOR else androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }
    }
}
