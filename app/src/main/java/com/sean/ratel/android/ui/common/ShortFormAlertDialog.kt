package com.sean.ratel.android.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelDialogThemeOverlay

@Suppress("ktlint:standard:function-naming")
@Composable
fun ShortFormCommonAlertDialog(
    onDismiss: (Boolean) -> Unit,
    bodyText: String,
    confirmText: String,
    cancelText: String? = null,
) {
    RatelDialogThemeOverlay {
        AlertDialog(
            onDismissRequest = {},
            text = {
                Text(
                    bodyText,
                    fontFamily = FontFamily.SansSerif,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = APP_TEXT_COLOR,
                )
            },
            buttons = {
                Divider(
                    Modifier.wrapContentSize(),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                )
                Row(Modifier.wrapContentHeight().wrapContentHeight(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { onDismiss(true) },
                        shape = RectangleShape,
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().weight(1f),
                    ) {
                        Text(confirmText)
                    }
                    cancelText?.let {
                        Divider(
                            Modifier.width(1.dp).height(56.dp).padding(vertical = 5.dp),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                        )
                        TextButton(
                            onClick = { onDismiss(false) },
                            shape = RectangleShape,
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().weight(1f),
                        ) {
                            Text(cancelText)
                        }
                    }
                }
            },
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun ShortFormCommonAlertDialogPreView() {
    RatelDialogThemeOverlay {
        ShortFormCommonAlertDialog(
            {},
            "네트워크가 WIFI 상태가 아닙니다.\n 재생 하시겠습니까?",
            "확인",
            "취소",
        )
    }
}
