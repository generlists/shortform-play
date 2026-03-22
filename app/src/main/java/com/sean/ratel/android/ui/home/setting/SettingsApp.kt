package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_PACKAGE_NAME
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsApp(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        val context = LocalContext.current
        AppTitle()
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            // 테두리도 살짝 투명도를 주어 번지는 배경과 연결
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            SettingsApp(SettingsItems.SETTING_APP_MANAGER, viewModel) {
                viewModel?.runAppDetail()
                viewModel?.sendGALog(
                    Event.SCREEN_VIEW,
                    Destination.SettingAppManagerDetail.route,
                )
            }

            SettingsApp(SettingsItems.SETTING_APP_RATE, viewModel) {
                PhoneUtil.runAppStore(
                    context,
                    URL_GOOGLE_PLAY_APP(
                        URL_MY_PACKAGE_NAME,
                    ),
                )
            }
        }

        // todo reject 사유?
        // SettingsApp(SettingsItems.SETTING_APP_PRESENT) { viewModel?.runAppDonation(context,STRINGS.DONATION_URL) }
        // Spacer(Modifier.height(3.dp))
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsApp(
    item: SettingsItems,
    viewModel: SettingViewModel?,
    goPage: () -> Unit,
) {
    Row(
        Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clickable {
                    goPage()
                    viewModel?.sendGALog(
                        Event.SCREEN_VIEW,
                        Destination.SettingAppRate.route,
                    )
                }.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                stringResource(item.mainTitle),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                modifier = Modifier,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = 15.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceDim,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AppTitle() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Background_op_10)
            .alpha(0.9f)
            .padding(20.dp),
    ) {
        Text(
            stringResource(R.string.setting_app_info),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.White,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        // SettingsApp()
    }
}
