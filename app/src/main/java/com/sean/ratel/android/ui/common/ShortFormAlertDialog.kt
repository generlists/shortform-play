package com.sean.ratel.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
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
                Row(
                    Modifier
                        .wrapContentHeight()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onDismiss(true) },
                        shape = RectangleShape,
                        contentPadding = PaddingValues(16.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(1f),
                    ) {
                        Text(confirmText)
                    }
                    cancelText?.let {
                        Divider(
                            Modifier
                                .width(1.dp)
                                .height(56.dp)
                                .padding(vertical = 5.dp),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                        )
                        TextButton(
                            onClick = { onDismiss(false) },
                            shape = RectangleShape,
                            contentPadding = PaddingValues(16.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .weight(1f),
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
@Composable
fun ShortFormSelectDialog(
    defaultCountryCode: String,
    options: List<Pair<String, String>>,
    onClick: (String) -> Unit,
    onDismiss: (Boolean) -> Unit,
    showDescription: Boolean = true,
) {
    var selectedCountryCode by remember { mutableStateOf(defaultCountryCode) }
    RatelDialogThemeOverlay {
        Dialog(onDismissRequest = { onDismiss(true) }) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(0.dp),
                elevation = CardDefaults.cardElevation(20.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(
                                androidx.compose.ui.graphics
                                    .Color(0xFF1F1F1F),
                            ),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.select_shortform_country_title),
                        modifier = Modifier.padding(all = 18.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        options.forEach { option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .clickable {
                                            selectedCountryCode = option.second
                                        }.padding(16.dp),
                            ) {
                                RadioButton(
                                    selected = selectedCountryCode == option.second,
                                    onClick = null,
                                    colors =
                                        RadioButtonDefaults.colors(
                                            selectedColor = APP_TEXT_COLOR,
                                            unselectedColor = androidx.compose.ui.graphics.Color.Gray,
                                            disabledColor = androidx.compose.ui.graphics.Color.LightGray,
                                        ),
                                )
                                Text(
                                    option.first,
                                    Modifier.padding(start = 6.dp),
                                    fontFamily = FontFamily.SansSerif,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = androidx.compose.ui.graphics.Color.White,
                                )
                            }
                        }
                    }
                    if (showDescription) {
                        Text(
                            stringResource(R.string.select_country_description),
                            Modifier.padding(16.dp),
                            fontFamily = FontFamily.SansSerif,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = APP_SUBTITLE_TEXT_COLOR,
                        )
                    }
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .padding(vertical = 5.dp),
                        color =
                            MaterialTheme.colors.onSurface
                                .copy(alpha = 0.2f),
                    )

                    TextButton(
                        onClick = {
                            onClick(selectedCountryCode)
                            onDismiss(true)
                        },
                        contentPadding = PaddingValues(16.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                    ) {
                        Text(stringResource(R.string.alert_ok))
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun ShortFormCommonAlertDialogPreView() {
    RatelDialogThemeOverlay {
        ShortFormSelectDialog(
            "KR",
            listOf(Pair<String, String>("대한 민국", "KR"), Pair<String, String>("미국", "US")),
            {},
            {},
        )
    }
}
