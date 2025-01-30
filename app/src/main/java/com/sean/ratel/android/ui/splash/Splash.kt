package com.sean.ratel.android.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.common.ShortFormCommonAlertDialog
import com.sean.ratel.android.ui.progress.LoadingMainPlaceholder
import com.sean.ratel.android.ui.theme.Red
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
    val statusBarPadding = getStatusBarHeight(context).toDp() // WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .offset(y = -statusBarPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SplashScreenAnimation()
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
        combine(splashViewModel.mainDataComplete, adViewModel.adMobInitialComplete) { isComplete, isInitialized ->

            // todo if(isInitialized) adViewModel.openAdLoad(context)

            isComplete && isInitialized
        }
    }.collectAsState(initial = false)
    if (isDataReady) {
        // adViewModel.showAppOpenAd(context)
        adViewModel.goMainHome()
    } else {
        SplashView(modifier)
        LoadingMainPlaceholder(loading = true)
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SplashScreenAnimation() {
    // 스케일 애니메이션을 위한 값 설정
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
    )

    // LaunchedEffect를 사용하여 애니메이션 시작
    LaunchedEffect(Unit) {
        isExpanded = true // 애니메이션을 트리거
    }

    // 텍스트를 가운데에 배치
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        // 접혔다가 펴지는 텍스트 애니메이션
        // 스케일 애니메이션 적용
        Text(
            text = "ShortForm Play",
            fontSize = 36.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = Red,
            modifier =
                Modifier
                    .scale(scale),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    // LoadingMainPlaceholderTest(loading = true)
    // SplashView(modifier = Modifier,null)
}
