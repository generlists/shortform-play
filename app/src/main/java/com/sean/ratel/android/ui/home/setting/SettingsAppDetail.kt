package com.sean.ratel.android.ui.home.setting

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
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil.getAppVersionName

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsAppDetail(viewModel: SettingViewModel?) {
    // val scrollState = rememberScrollState()
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        SettingsApp(SettingsItems.SETTING_APP_VERSION, viewModel, isArrow = false)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsApp(SettingsItems.SETTING_APP_OPEN_SOURCE, viewModel)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsApp(
    item: SettingsItems,
    viewModel: SettingViewModel?,
    isArrow: Boolean = true,
) {
    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            if (isArrow) {
                Modifier.then(
                    Modifier.clickable {
                        viewModel?.goAppSettingsOpenSourceLicense(context)
                        viewModel?.sendGALog(
                            Event.SCREEN_VIEW,
                            Destination.SettingAppLicense.route,
                        )
                    },
                )
            } else {
                Modifier
                    .align(Alignment.CenterVertically)
            },
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                stringResource(item.mainTitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
                modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
            )
            if (isArrow) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(end = 15.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Image(
                        // 이미지 리소스
                        painter = painterResource(id = R.drawable.ic_link_index),
                        contentDescription = "Link",
                        modifier =
                            Modifier
                                .height(32.dp)
                                .width(32.dp),
                    )
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(end = 25.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        getAppVersionName(context),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = APP_TEXT_COLOR,
                        modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
                    )
                }
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
            .height(30.dp)
            .background(Background_op_10)
            .alpha(0.9f)
            .padding(start = 5.dp, top = 5.dp, bottom = 5.dp),
    ) {
        Text(
            stringResource(R.string.setting_app_manager),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.Black,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingsAppDetail(null)
    }
}
