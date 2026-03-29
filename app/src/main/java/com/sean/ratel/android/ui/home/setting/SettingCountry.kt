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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.getShortFormCountry
import com.sean.ratel.android.data.log.GASettingAnalytics
import com.sean.ratel.android.ui.common.ShortFormSelectDialog
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil.newActivity
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
            ShortFormCountry(SettingsItems.SETTING_SHORTFORM_COUNTRY, viewModel)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ShortFormCountry(
    item: SettingsItems,
    viewModel: SettingViewModel?,
) {
    var showPopup by remember { mutableStateOf(false) }
    val options =
        listOf(
            Pair(stringResource(R.string.select_country_korea), "KR"),
            Pair(stringResource(R.string.select_country_usa), "US"),
            Pair(stringResource(R.string.select_country_japan), "JP"),
            Pair(stringResource(R.string.select_country_taiwan), "TW"),
            Pair(stringResource(R.string.select_country_indonesia), "ID"),
            Pair(stringResource(R.string.select_country_tailand), "TH"),
            Pair(stringResource(R.string.select_country_canada_en), "CA"),
        )
    val locale = viewModel?.locale?.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        coroutineScope.launch {
            // locale = viewModel?.getLocale() ?: "KR"
        }
    }
    val value = locale?.value ?: "KR"
    val country = getShortFormCountry(context).first { it.second == value }
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
                    showPopup = true
                    val value = locale?.value ?: "KR"
                    viewModel?.sendGALog(
                        screenName = GASettingAnalytics.SCREEN_NAME,
                        eventName = GASettingAnalytics.Event.SETTING_MAIN_COUNTY_ITEM_CLICK,
                        actionName = GASettingAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASettingAnalytics.Param.COUNTY_CODE to value,
                            ),
                    )
                }.align(Alignment.CenterVertically),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(Modifier) {
                Text(
                    "${country.first}(${country.second})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier = Modifier,
                )
                Text(
                    stringResource(item.description ?: 0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = APP_SUBTITLE_TEXT_COLOR,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
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

    if (showPopup) {
        ShortFormSelectDialog(
            defaultCountryCode = locale?.value ?: "KR",
            options = options,
            onClick = { countryCode ->
                // Log.d("LLLLLLLLLL","${locale?.value} countryCode!! $countryCode")
                if (locale?.value != countryCode) {
                    viewModel?.sendGALog(
                        screenName = GASettingAnalytics.SCREEN_NAME,
                        eventName = GASettingAnalytics.Event.SELECT_COUNTY_CLICK,
                        actionName = GASettingAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASettingAnalytics.Param.COUNTY_CODE to countryCode,
                            ),
                    )

                    coroutineScope.launch {
                        RLog.d("SPLASH", "${locale?.value} countryCode!! $countryCode")
                        viewModel?.removeLocalCache("shorts_main_list_$locale.json")
                        viewModel?.removeLocalCache("shorts_trailer_list_$locale.json")
                        viewModel?.removeCategory()

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
            stringResource(R.string.setting_select_shortform_country),
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
