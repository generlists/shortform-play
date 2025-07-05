package com.sean.ratel.android.ui.splash

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.ShortFormCommonAlertDialog
import com.sean.ratel.android.ui.progress.LottieLoader
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.utils.PhoneUtil.getStatusBarHeight
import com.sean.ratel.android.utils.UIUtil.toDp
import kotlinx.coroutines.flow.combine

@Suppress("ktlint:standard:function-naming")
@Composable
fun Splash(
    modifier: Modifier,
    splashViewModel: SplashViewModel,
    adViewModel: AdViewModel,
) {
    BackHandler { splashViewModel.navigator.finish() }
    NetworkAlert(splashViewModel)
    InitialDataAndAD(modifier, adViewModel, splashViewModel)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SplashView(modifier: Modifier) {
    val context = LocalContext.current
    val statusBarPadding =
        getStatusBarHeight(context).toDp() // WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

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
fun InitialDataAndAD(
    modifier: Modifier,
    adViewModel: AdViewModel,
    splashViewModel: SplashViewModel,
) {
    val isDataReady by remember {
        combine(
            splashViewModel.mainDataComplete,
            adViewModel.adMobInitialComplete,
        ) { isComplete, isInitialized ->

            // todo if(isInitialized) adViewModel.openAdLoad(context)

            isComplete && isInitialized
        }
    }.collectAsState(initial = false)
    if (isDataReady) {
        // adViewModel.showAppOpenAd(context)
        adViewModel.goMainHome()
    } else {
        SplashView(modifier)
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    // LoadingMainPlaceholderTest(loading = true)
    // SplashView(modifier = Modifier,null)
}
