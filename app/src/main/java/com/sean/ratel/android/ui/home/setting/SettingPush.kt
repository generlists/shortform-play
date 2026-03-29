package com.sean.ratel.android.ui.home.setting

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingPush(
    item: SettingsItems,
    pushViewModel: PushViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(true) }
    val pushNotification by pushViewModel.hasPermission.collectAsState()

    val hasPermission by pushViewModel.hasPermission.collectAsState()
    val requestedBefore by pushViewModel.requestedBefore.collectAsState(initial = false)
    val openSetting by pushViewModel.goOpenSetting.collectAsStateWithLifecycle()

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            pushViewModel.onNotificationPermissionResult(granted)
        }

    Column(Modifier.fillMaxWidth()) {
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
                    Text(
                        stringResource(item.description ?: 0),
                        fontSize = 10.sp,
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
                        .weight(0.2f)
                        .padding(end = 15.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    RSwitch(
                        positiveColor = APP_TEXT_COLOR,
                        buttonHeight = 24.dp,
                        switchValue = pushNotification,
                        onValueChanged = {
                            when {
                                hasPermission -> {
                                    Log.d("hbungshin", "설정이동 1")
                                    pushViewModel.setOpenSettings(true)
                                    Log.d("hbungshin", "설정이동1 openSetting : $openSetting")
                                    pushViewModel.openAppSettings()
                                }

                                !requestedBefore && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                    pushViewModel.setRequestedBefore(true)
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }

                                else -> {
                                    pushViewModel.setOpenSettings(true)
                                    pushViewModel.openAppSettings()
                                }
                            }
                        },
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = { expanded = !expanded },
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceDim,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                SettingPushDetail(pushViewModel)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val goOpenSetting by pushViewModel.goOpenSetting.collectAsStateWithLifecycle()
    val currentPushCheck by pushViewModel.currentPushItemCheck.collectAsState()
    val latestCurrentPushCheckSetting by rememberUpdatedState(currentPushCheck)

    val scope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        // 설정에서 프로세스가 죽어서 다시 오면 딜레이를 주고 받아야 정상적으로 데이터를 가져온다.
                        delay(500)

                        if (!goOpenSetting) return@launch

                        pushViewModel.refreshPermission(latestCurrentPushCheckSetting)
                        val currentPermission = pushViewModel.hasPermission.value
                        pushViewModel.setOpenSettings(false)

                        if (latestCurrentPushCheckSetting != null) {
                            when (latestCurrentPushCheckSetting) {
                                SettingsItems.SERVICE_PUSH_APP_UPDATE -> {
                                    pushViewModel.saveAppUpdatePush(currentPermission)
                                }

                                SettingsItems.SERVICE_PUSH_VIDEO_UPLOAD -> {
                                    pushViewModel.saveUploadPush(currentPermission)
                                }

                                SettingsItems.SERVICE_PUSH_VIDEO_RECOMMEND -> {
                                    pushViewModel.saveRecommendPush(currentPermission)
                                }

                                else -> {
                                    Unit
                                }
                            }
                        }
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
