package com.sean.ratel.android.ui.splash

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.FirebaseStorage
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_PACKAGE_NAME
import com.sean.ratel.android.data.common.STRINGS.getShortFormCountry
import com.sean.ratel.android.data.log.GAKeys.SPLASH_SCREEN
import com.sean.ratel.android.data.log.GASplashAnalytics
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.ShortFormCommonAlertDialog
import com.sean.ratel.android.ui.common.ShortFormSelectDialog
import com.sean.ratel.android.ui.progress.LottieLoader
import com.sean.ratel.android.ui.push.PushViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.PhoneUtil.StatusBarHeight
import com.sean.ratel.android.utils.PhoneUtil.qnaResource
import com.sean.ratel.android.utils.UIUtil.getCountryCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import so.smartlab.common.ad.admob.data.model.AdMobInitState

enum class SplashStep {
    NETWORK,
    NOTIFICATION,
    AUTH,
    INIT,
    DONE,
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun Splash(
    splashViewModel: SplashViewModel,
    adViewModel: AdViewModel,
    mainViewModel: MainViewModel,
    pushViewModel: PushViewModel,
) {
    var step by remember { mutableStateOf(SplashStep.NETWORK) }
    LaunchedEffect(Unit) {
        step = SplashStep.NETWORK
    }
    val autoCheck by splashViewModel.authCheck.collectAsState()

    BackHandler { splashViewModel.navigator.finish() }

    Box(modifier = Modifier.fillMaxSize()) {
        RLog.d("SPLASH", "pass  $step ,  autoCheck : $autoCheck")

        when (step) {
            SplashStep.NETWORK -> {
                NetworkAlert(
                    splashViewModel = splashViewModel,
                    pass = {
                        RLog.d("SPLASH", "move NETWORK -> NOTIFICATION")
                        step = SplashStep.NOTIFICATION
                    },
                )
            }

            SplashStep.NOTIFICATION -> {
                RLog.d("SPLASH", "pass : NOTIFICATION $step")
                NotificationPermission(
                    splashViewModel = splashViewModel,
                    pushViewModel = pushViewModel,
                    pass = { pass ->
                        if (pass) step = SplashStep.AUTH
                    },
                )
            }

            SplashStep.AUTH -> {
                RLog.d("SPLASH", "pass : AUTH")
                AuthCheckAlert(
                    splashViewModel = splashViewModel,
                    pass = { pass ->
                        RLog.d("SPLASH", "pass : A $pass")
                        if (pass) step = SplashStep.INIT
                    },
                )
            }

            SplashStep.INIT -> {
                InitialDataAndAD(
                    mainViewModel = mainViewModel,
                    adViewModel = adViewModel,
                    splashViewModel = splashViewModel,
                    pass = { pass ->
                        if (pass) step = SplashStep.DONE
                    },
                )
            }

            SplashStep.DONE -> {
                LaunchedEffect(Unit) {
                    delay(500)
                    adViewModel.goMainHome()
                }
            }
        }
        AnimatedVisibility(
            visible = step != SplashStep.DONE,
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
        ) {
            SplashView()
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NotificationPermission(
    splashViewModel: SplashViewModel,
    pushViewModel: PushViewModel,
    pass: (Boolean) -> Unit,
) {
    val permissionManager = splashViewModel.permissionManager
    val requestPermission = permissionManager.requiredPermissions()

    var hasRequested by rememberSaveable { mutableStateOf(false) }

    val notificationLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            // result 권한 획득 유무와 관계 없이 이동
            RLog.d("SPLASH", "notificationLauncher granted $granted")
            if (granted) {
                pushViewModel.registerPush()
            } else {
                pushViewModel.unRegisterPush()
            }
            pushViewModel.refreshPermission()
            pushViewModel.onNotificationPermissionResult(granted)
            pass(true)
        }

    LaunchedEffect(Unit) {
        if (!hasRequested) {
            hasRequested = true
            RLog.d("KKKKKK", "grant")
            // 33 이상만 권한 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val grant = permissionManager.has(permission = requestPermission)
                val rationale =
                    permissionManager.shouldShowRationale(permission = requestPermission)

                RLog.d("KKKKKK", "grant $grant , rationale : $rationale")
                if (!grant) {
                    kotlinx.coroutines.android.awaitFrame()
                    notificationLauncher.launch(requestPermission)
                } else {
                    pass(true)
                }
            } else {
                // 33 미만 은 권한이 팝업이  안뜨기 때문에 현재 가지고 있는 권한에 따라 등록 및 취소를 해야한다.
                val grant = permissionManager.has(permission = requestPermission)

                if (grant) {
                    pushViewModel.registerPush()
                } else {
                    pushViewModel.unRegisterPush()
                }
                pass(true)
            }

            pushViewModel.refreshPermission()
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SplashView() {
    val statusBarPadding = StatusBarHeight()

    Box(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .offset(y = -statusBarPadding),
        contentAlignment = Alignment.Center,
    ) {
        LottieLoader(
            Modifier
                .wrapContentSize()
                .width(240.dp)
                .height(240.dp),
            rawRes = R.raw.splash,
            forever = true,
        )
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painterResource(R.drawable.splash_text),
            contentDescription = "splash",
            modifier = Modifier.wrapContentSize(),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NetworkAlert(
    splashViewModel: SplashViewModel,
    pass: () -> Unit,
) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val noNetwork = !splashViewModel.isNetWorkAvailable(context)
        showDialog = noNetwork
        checked = true

        if (!noNetwork) {
            pass()
        }
    }

    if (checked && showDialog) {
        ShortFormCommonAlertDialog(
            onDismiss = { buttonClick ->
                if (buttonClick) {
                    splashViewModel.exitApp()
                }
                showDialog = false
            },
            stringResource(R.string.alert_no_network),
            stringResource(R.string.alert_ok),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AuthCheckAlert(
    splashViewModel: SplashViewModel,
    pass: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val showDialog by splashViewModel.authCheck.collectAsState()

    RLog.d("SPLASH", "showDialog value : $showDialog")
    if (showDialog != null && showDialog != 0) {
        ShortFormCommonAlertDialog(
            onDismiss = { buttonClick ->
                if (buttonClick) {
                    if (showDialog == 1000) {
                        PhoneUtil.runAppStore(
                            context,
                            URL_GOOGLE_PLAY_APP(URL_MY_PACKAGE_NAME),
                        )
                    } else {
                        PhoneUtil.sendEmail(
                            context,
                            STRINGS.FEEDBACK_TITLE,
                            qnaResource(context),
                        )
                    }
                    splashViewModel.exitApp()
                }
            },
            if (showDialog == 1000) {
                stringResource(R.string.go_app_store)
            } else {
                stringResource(R.string.auth_error)
            },
            if (showDialog == 1000) {
                stringResource(R.string.store_update_btn)
            } else {
                stringResource(R.string.alert_ok)
            },
        )
    } else {
        pass(true)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun InitialDataAndAD(
    mainViewModel: MainViewModel,
    adViewModel: AdViewModel,
    splashViewModel: SplashViewModel,
    pass: (Boolean) -> Unit,
) {
    val locale by splashViewModel.locale.collectAsState(initial = null)
    val hasLoadedOnce by splashViewModel.hasLoadedOnce.collectAsState()
    var showCheck by remember(hasLoadedOnce) { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val isAdComplete by mainViewModel.adMobinitState.collectAsState()
    val options = getShortFormCountry(LocalContext.current)
    val forceRefresh by adViewModel.forceClearCache.collectAsState()
    val authCheck by splashViewModel.authCheck.collectAsState()

    LaunchedEffect(isAdComplete) {
        combine(
            splashViewModel.mainDataComplete,
            splashViewModel.trendsShortsComplete,
        ) { mainData, trendsShortsData ->

            Pair(mainData, trendsShortsData)
        }.collect { combinedResult ->
            val (main, trends) = combinedResult
            if (authCheck != null && (authCheck ?: 0) > 0) return@collect
            RLog.d("SPLASH", "main : $main trends : $trends")
            if (main && trends) {
                pass(true)
            }
        }
    }

    if (isAdComplete is AdMobInitState.InitComplete) {
        LaunchedEffect(hasLoadedOnce) {
            showCheck = false
            delay(100)
            showCheck = true
        }
        RLog.d("SPASH", "start locale : $locale")
        if (locale == null && showCheck) {
            ShortFormSelectDialog(
                defaultCountryCode = getCountryCode(),
                options = options,
                onClick = { countryCode ->
                    RLog.d("LLLLLLLLLL", "befor locale : $locale , after countryCode : $countryCode")
                    coroutineScope.launch {
                        splashViewModel.setLocale(countryCode)
                    }

                    val value = locale ?: "KR"
                    splashViewModel.sendGALog(
                        screenName = GASplashAnalytics.SCREEN_NAME.get(SPLASH_SCREEN) ?: "",
                        eventName = GASplashAnalytics.Event.SELECT_COUNTY_CLICK,
                        actionName = GASplashAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASplashAnalytics.Param.COUNTY_CODE to value,
                            ),
                    )

                    coroutineScope.launch {
                        splashViewModel.setLocale(countryCode)
                        // RLog.d("SPLASH", "newUpdate : $newUpdate")
                    }
                },
                onDismiss = {},
            )
        } else {
            LaunchedEffect(locale) {
                delay(100)
                val aa = getCountryCode(locale)
                splashViewModel.requestYouTubeVideos(
                    SplashViewModel.RequestType.TODAY,
                    FirebaseStorage.getInstance(),
                    getCountryCode(locale),
                    forceRefresh,
                )

                splashViewModel.requestYouTubeTrendShorts(
                    SplashViewModel.RequestType.TODAY,
                    FirebaseStorage.getInstance(),
                    getCountryCode(locale),
                    forceRefresh,
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    // LoadingMainPlaceholderTest(loading = true)
    // SplashView(modifier = Modifier,null)
}
