package com.sean.ratel.android.ui.home.setting

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.UIUtil.hasPipPermission
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsVideo(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(Color.White),
    ) {
        SettingVideoTitle()
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsVideo(viewModel, SettingsItems.SERVICE_VIDEO_AUTO_PLAY)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsVideo(viewModel, SettingsItems.SERVICE_VIDEO_LOOP_PLAY)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsVideo(viewModel, SettingsItems.SERVICE_VIDEO_PIP_PLAY)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsVideo(viewModel, SettingsItems.SERVICE_VIDEO_SOUND)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
        SettingsVideo(viewModel, SettingsItems.SERVICE_VIDEO_WIFI_STATE)
        Spacer(Modifier.background(Color.Black).height(3.dp).fillMaxWidth())
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingsVideo(
    viewModel: SettingViewModel?,
    item: SettingsItems,
) {
    val context = LocalContext.current
    var autoPlaySwitchValue by rememberSaveable { mutableStateOf(false) }
    var loopSwitchValue by rememberSaveable { mutableStateOf(false) }
    var wifiOnlySwitchValue by rememberSaveable { mutableStateOf(false) }
    var pipModeSwitchValue by rememberSaveable { mutableStateOf(false) }
    var soundOnOffSwitchValue by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope() // 코루틴 스코프 생성

    LaunchedEffect(Unit) {
        viewModel?.setPIPPlay(context.hasPipPermission())

        autoPlaySwitchValue = viewModel?.getAutoPlay() ?: true
        loopSwitchValue = viewModel?.getLoopPlay() ?: true
        pipModeSwitchValue = viewModel?.getPIPPlay() ?: context.hasPipPermission()
        soundOnOffSwitchValue = viewModel?.getSoundOnOff() ?: true
        wifiOnlySwitchValue = viewModel?.getWifiOnlyPlay() ?: true
    }
    val pipSettingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { _ ->
            pipModeSwitchValue = context.hasPipPermission()
            coroutineScope.launch {
                viewModel?.setPIPPlay(context.hasPipPermission())
            }
        }
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .wrapContentSize()
                .background(APP_BACKGROUND)
                .clickable(onClick = { }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(start = 5.dp, top = 5.dp)) {
                Text(
                    stringResource(item.mainTitle),
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
                RSwitch(
                    positiveColor = APP_TEXT_COLOR,
                    buttonHeight = 24.dp,
                    switchValue =
                        if (item == SettingsItems.SERVICE_VIDEO_AUTO_PLAY) {
                            autoPlaySwitchValue
                        } else if (item == SettingsItems.SERVICE_VIDEO_LOOP_PLAY) {
                            loopSwitchValue
                        } else if (item == SettingsItems.SERVICE_VIDEO_WIFI_STATE) {
                            wifiOnlySwitchValue
                        } else if (item == SettingsItems.SERVICE_VIDEO_PIP_PLAY) {
                            pipModeSwitchValue
                        } else if (item == SettingsItems.SERVICE_VIDEO_SOUND) {
                            soundOnOffSwitchValue
                        } else {
                            false
                        },
                    onValueChanged = { s ->
                        if (item == SettingsItems.SERVICE_VIDEO_AUTO_PLAY) {
                            autoPlaySwitchValue = s
                        } else if (item == SettingsItems.SERVICE_VIDEO_LOOP_PLAY) {
                            loopSwitchValue = s
                        } else if (item == SettingsItems.SERVICE_VIDEO_WIFI_STATE) {
                            wifiOnlySwitchValue = s
                        } else if (item == SettingsItems.SERVICE_VIDEO_SOUND) {
                            soundOnOffSwitchValue = s
                        } else if (item == SettingsItems.SERVICE_VIDEO_PIP_PLAY) {
                            val intent =
                                Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS").apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            pipSettingsLauncher.launch(intent)
                        }
                        coroutineScope.launch {
                            settingPlayOption(viewModel, item, s) // 변경된 값을 저장
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(5.dp))
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingVideoTitle() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(APP_BACKGROUND)
            .alpha(0.9f)
            .padding(start = 5.dp, top = 3.dp, bottom = 3.dp),
    ) {
        Text(
            stringResource(R.string.setting_play),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.White,
        )
    }
}

suspend fun gettingPlayOption(
    viewModel: SettingViewModel?,
    item: SettingsItems,
): Boolean {
    when (item) {
        // SettingsItems.SERVICE_VIDEO_AUTO_PLAY-> return viewModel?.getAutoPlay()?:true
        SettingsItems.SERVICE_VIDEO_LOOP_PLAY -> return viewModel?.getLoopPlay() ?: true
        SettingsItems.SERVICE_VIDEO_WIFI_STATE -> return viewModel?.getWifiOnlyPlay() ?: true
        else -> return false
    }
}

suspend fun settingPlayOption(
    viewModel: SettingViewModel?,
    item: SettingsItems,
    switchValue: Boolean,
) {
    runBlocking {
        when (item) {
            SettingsItems.SERVICE_VIDEO_AUTO_PLAY -> viewModel?.setAutoPlay(switchValue)
            SettingsItems.SERVICE_VIDEO_LOOP_PLAY -> viewModel?.setLoopPlay(switchValue)
            SettingsItems.SERVICE_VIDEO_SOUND -> viewModel?.setSoundOff(switchValue)
            SettingsItems.SERVICE_VIDEO_WIFI_STATE -> viewModel?.setWifiOnlyPlay(switchValue)
            else -> Unit
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        SettingsVideo(null)
    }
}
