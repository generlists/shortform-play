package com.sean.ratel.android.ui.home.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainActivity
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.getShortFormCountry
import com.sean.ratel.android.data.log.GASettingAnalytics
import com.sean.ratel.android.ui.common.ShortFormSelectDialog
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsCountry(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        val context = LocalContext.current
        AppTitle()
        Spacer(Modifier.height(3.dp))
        ShortFormCountry(SettingsItems.SETTING_SHORTFORM_COUNTRY, viewModel) {
            viewModel?.runAppDetail()
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.SettingAppManagerDetail.route,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ShortFormCountry(
    item: SettingsItems,
    viewModel: SettingViewModel?,
    goPage: () -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }
    val options = listOf(Pair(stringResource(R.string.select_country_korea), "KR"), Pair(stringResource(R.string.select_country_usa), "US"))
    var locale by remember { mutableStateOf("KR") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        coroutineScope.launch {
            locale = viewModel?.getLocale() ?: "KR"
        }
    }
    val country = getShortFormCountry(context).first { it.second == locale }
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clickable {
                    showPopup = true
                    viewModel?.sendGALog(
                        screenName = GASettingAnalytics.SCREEN_NAME,
                        eventName = GASettingAnalytics.Event.SETTING_MAIN_COUNTY_ITEM_CLICK,
                        actionName = GASettingAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASettingAnalytics.Param.COUNTY_CODE to locale,
                            ),
                    )
                }.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(Modifier.padding(start = 5.dp, top = 5.dp)) {
                Text(
                    "${country.first}(${country.second})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    stringResource(item.description ?: 0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = APP_SUBTITLE_TEXT_COLOR,
                    modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
                )
            }
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
        }
    }

    if (showPopup) {
        ShortFormSelectDialog(
            defaultCountryCode = locale,
            options = options,
            onClick = { countryCode ->
                if (locale != countryCode) {
                    viewModel?.sendGALog(
                        screenName = GASettingAnalytics.SCREEN_NAME,
                        eventName = GASettingAnalytics.Event.SELECT_COUNTY_CLICK,
                        actionName = GASettingAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASettingAnalytics.Param.COUNTY_CODE to countryCode,
                            ),
                    )
                    locale = countryCode
                    coroutineScope.launch {
                        viewModel?.setLocale(countryCode)
                        newActivity(context)
                    }
                }

                showPopup = false
            },
            onDismiss = {
                showPopup = false
            },
            showDescription = false,
        )
    }
}

private fun newActivity(context: Context) {
    val activity = (context as? Activity)

    activity?.let {
        val intent =
            Intent(it, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("clear_cache", true)
            }
        it.startActivity(intent)
        it.finish()
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
            .padding(start = 5.dp, top = 3.dp, bottom = 3.dp),
    ) {
        Text(
            stringResource(R.string.setting_select_shortform_country),
            fontSize = 12.sp,
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
