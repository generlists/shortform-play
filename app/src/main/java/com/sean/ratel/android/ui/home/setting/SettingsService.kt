package com.sean.ratel.android.ui.home.setting

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import java.util.Locale

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsService(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(Color.White),
    ) {
        ServiceTitle()
        Spacer(Modifier.height(3.dp))
        SettingsService(SettingsItems.SERVICE_TITLE_NOTICE, viewModel)
        Spacer(Modifier.height(3.dp))
        SettingsService(SettingsItems.SERVICE_TITLE_QNA, viewModel)
        Spacer(Modifier.height(3.dp))
        SettingsService(SettingsItems.SERVICE_TITLE_REGAL, viewModel)
        Spacer(Modifier.height(3.dp))
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsService(
    item: SettingsItems,
    viewModel: SettingViewModel?,
) {
    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clickable {
                    runLinkItem(context, item, viewModel)
                }.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                stringResource(item.mainTitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.Black,
                modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = 15.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                // 이미지 리소스
                Image(
                    painter = painterResource(id = R.drawable.ic_link_index),
                    contentDescription = "Link",
                    modifier =
                        Modifier
                            .height(32.dp)
                            .width(32.dp),
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ServiceTitle() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Background_op_10)
            .alpha(0.9f)
            .padding(start = 5.dp, top = 3.dp, bottom = 3.dp),
    ) {
        Text(
            stringResource(R.string.setting_service),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.Black,
        )
    }
}

private fun runLinkItem(
    context: Context,
    item: SettingsItems,
    viewModel: SettingViewModel?,
) {
    when (item) {
        SettingsItems.SERVICE_TITLE_NOTICE -> {
            val country = Locale.getDefault().country
            viewModel?.runAppLink(
                context,
                if (country == "US") {
                    STRINGS.NOTICES_URL_EN
                } else if (country == "KR") {
                    STRINGS.NOTICES_URL
                } else {
                    STRINGS.NOTICES_URL_EN
                },
            )
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.Notices.route,
            )
        }
        SettingsItems.SERVICE_TITLE_QNA -> {
            viewModel?.runQNA(context)
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.QNA.route,
            )
        }
        SettingsItems.SERVICE_TITLE_REGAL -> {
            val country = Locale.getDefault().country
            viewModel?.runAppLink(
                context,
                if (country == "US") {
                    STRINGS.REGAL_URL_EN
                } else if (country == "KR") {
                    STRINGS.REGAL_URL
                } else {
                    STRINGS.REGAL_URL_EN
                },
            )
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.Regal.route,
            )
        }
        else -> Unit
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingsService(null)
    }
}
