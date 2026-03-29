package com.sean.ratel.android.ui.home.setting

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingPushDetail(viewModel: PushViewModel) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        ) {
            SettingPushItem(viewModel, SettingsItems.SERVICE_PUSH_APP_UPDATE)
            SettingPushItem(viewModel, SettingsItems.SERVICE_PUSH_VIDEO_UPLOAD)
            SettingPushItem(viewModel, SettingsItems.SERVICE_PUSH_VIDEO_RECOMMEND)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SettingPushItem(
    viewModel: PushViewModel,
    item: SettingsItems,
) {
    val appUpdatePush by viewModel.appUpdatePush.collectAsState()
    val contentUpload by viewModel.uploadPush.collectAsState()
    val recommendPush by viewModel.recommendPush.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    // var currentPushCheck by rememberSaveable{ mutableStateOf<SettingsItems?>(null)}

    Row(
        Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .wrapContentSize()
                .clickable(onClick = { }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(0.8f)) {
                Text(
                    stringResource(item.mainTitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier = Modifier,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .weight(0.2f)
                    .padding(end = 15.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                RSwitch(
                    positiveColor = APP_TEXT_COLOR,
                    buttonHeight = 24.dp,
                    switchValue =
                        if (item == SettingsItems.SERVICE_PUSH_APP_UPDATE) {
                            appUpdatePush
                        } else if (item == SettingsItems.SERVICE_PUSH_VIDEO_UPLOAD) {
                            contentUpload
                        } else {
                            recommendPush
                        },
                    onValueChanged = { check ->
                        val currentAppUpdate = appUpdatePush
                        val currentUpload = contentUpload
                        val currentRecommend = recommendPush

                        val beforeAnyOn = currentAppUpdate || currentUpload || currentRecommend

                        val newAppUpdate =
                            when (item) {
                                SettingsItems.SERVICE_PUSH_APP_UPDATE -> check
                                else -> currentAppUpdate
                            }

                        val newUpload =
                            when (item) {
                                SettingsItems.SERVICE_PUSH_VIDEO_UPLOAD -> check
                                else -> currentUpload
                            }

                        val newRecommend =
                            when (item) {
                                SettingsItems.SERVICE_PUSH_VIDEO_RECOMMEND -> check
                                else -> currentRecommend
                            }

                        val afterAnyOn = newAppUpdate || newUpload || newRecommend
                        val shouldOpenSettings = beforeAnyOn != afterAnyOn
                        // 원복때문에 필요
                        viewModel.setCurrentPushCheck(item)

                        if (shouldOpenSettings) {
                            viewModel.setOpenSettings(true)
                            viewModel.openAppSettings()
                        } else {
                            setUpdateCheck(shouldOpenSettings, check, item, viewModel)
                        }
                    },
                )
            }
        }
    }
}

fun setUpdateCheck(
    shouldOpenSetting: Boolean,
    check: Boolean,
    item: SettingsItems,
    viewModel: PushViewModel,
) {
    // val realCheck = if(shouldOpenSetting) !check else check
    Log.d("hbungshin", "setUpdateCheck check : $check $item")
    when (item) {
        SettingsItems.SERVICE_PUSH_APP_UPDATE -> {
            viewModel.saveAppUpdatePush(check)
        }

        SettingsItems.SERVICE_PUSH_VIDEO_UPLOAD -> {
            viewModel.saveUploadPush(check)
        }

        SettingsItems.SERVICE_PUSH_VIDEO_RECOMMEND -> {
            viewModel.saveRecommendPush(check)
        }

        else -> {
            Unit
        }
    }
}
