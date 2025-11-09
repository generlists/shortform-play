package com.sean.ratel.android.ui.splash

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.FirebaseStorage
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
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.utils.PhoneUtil
import com.sean.ratel.android.utils.PhoneUtil.StatusBarHeight
import com.sean.ratel.android.utils.UIUtil.getCountryCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@Suppress("ktlint:standard:function-naming")
@Composable
fun Splash(
    splashViewModel: SplashViewModel,
    adViewModel: AdViewModel,
) {
    BackHandler { splashViewModel.navigator.finish() }
    NetworkAlert(splashViewModel)
    AuthCheckAlert(splashViewModel)
    InitialDataAndAD(adViewModel, splashViewModel)
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
            contentDescription = "Text",
            modifier =
                Modifier.wrapContentSize(),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NetworkAlert(splashViewModel: SplashViewModel) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(!splashViewModel.isNetWorkAvailable(context)) }

    if (showDialog) {
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
private fun AuthCheckAlert(splashViewModel: SplashViewModel) {
    val context = LocalContext.current
    val showDialog = splashViewModel.authCheck.collectAsState()

    if (showDialog.value != 0) {
        ShortFormCommonAlertDialog(
            onDismiss = { buttonClick ->
                if (buttonClick) {
                    if (showDialog.value == 1000) {
                        PhoneUtil.runAppStore(
                            context,
                            URL_GOOGLE_PLAY_APP(
                                URL_MY_PACKAGE_NAME,
                            ),
                        )
                    } else {
                        PhoneUtil.sendEmail(context, STRINGS.FEEDBACK_TITLE, qnaResource(context))
                    }
                    splashViewModel.setAuthCheck(0)
                }
            },
            if (showDialog.value == 1000)stringResource(R.string.go_app_store) else stringResource(R.string.auth_error),
            if (showDialog.value == 1000) stringResource(R.string.store_update_btn) else stringResource(R.string.alert_ok),
        )
    }
}

fun qnaResource(context: Context): String {
    val str = StringBuilder()
    str.append("App Version : ")
    str.append(PhoneUtil.getAppVersionName(context))
    str.append("\n")
    str.append(PhoneUtil.getEnvironment())
    return str.toString()
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun InitialDataAndAD(
    adViewModel: AdViewModel,
    splashViewModel: SplashViewModel,
) {
    var showSplash by remember { mutableStateOf(true) }
    var locale by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val isAMobInitialComplete by remember { mutableStateOf(adViewModel.adMobInitialComplete) }
    val isAdComplete = isAMobInitialComplete.collectAsState().value
    val options = getShortFormCountry(LocalContext.current)
    var loadLocale by remember { mutableStateOf(false) }
    val forceRefresh = adViewModel.forceClearCache.collectAsState().value
    val authCheck by splashViewModel.authCheck.collectAsState()

    LaunchedEffect(isAMobInitialComplete) {
        coroutineScope.launch {
            locale = splashViewModel.getLocale()
            loadLocale = true
        }

        combine(
            splashViewModel.mainDataComplete,
            splashViewModel.trendsShortsComplete,
        ) { mainData, trendsShortsData ->

            Pair(mainData, trendsShortsData)
        }.collect { combinedResult ->
            val (main, trends) = combinedResult
            if (authCheck > 0) return@collect

            if (main && trends) {
                delay(1500)
                showSplash = false
                delay(500)
                adViewModel.goMainHome()
            }
        }
    }
    if (isAdComplete && loadLocale) {
        if (locale.isEmpty()) {
            ShortFormSelectDialog(
                defaultCountryCode = getCountryCode(),
                options = options,
                onClick = { countryCode ->
                    locale = countryCode
                    splashViewModel.sendGALog(
                        screenName = GASplashAnalytics.SCREEN_NAME.get(SPLASH_SCREEN) ?: "",
                        eventName = GASplashAnalytics.Event.SELECT_COUNTY_CLICK,
                        actionName = GASplashAnalytics.Action.CLICK,
                        parameter =
                            mapOf(
                                GASplashAnalytics.Param.COUNTY_CODE to locale,
                            ),
                    )

                    coroutineScope.launch {
                        splashViewModel.setLocale(countryCode)
                        splashViewModel.requestYouTubeVideos(
                            SplashViewModel.RequestType.TODAY,
                            FirebaseStorage.getInstance(),
                            getCountryCode(countryCode),
                            forceRefresh,
                        )
                        splashViewModel.requestYouTubeTrendShorts(
                            SplashViewModel.RequestType.TODAY,
                            FirebaseStorage.getInstance(),
                            getCountryCode(locale),
                            forceRefresh,
                        )
                    }
                },
                onDismiss = {},
            )
        } else {
            LaunchedEffect(Unit) {
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

    AnimatedVisibility(
        visible = showSplash,
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
        SplashView()
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    // LoadingMainPlaceholderTest(loading = true)
    // SplashView(modifier = Modifier,null)
}
