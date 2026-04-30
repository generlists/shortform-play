package com.sean.ratel.android.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import so.smartlab.common.ad.admob.data.model.AdMobBannerState

@Suppress("ktlint:standard:function-naming")
@Composable
fun Loading(
    modifier: Modifier = Modifier,
    loader: Loader,
) {
    val loading = loader.loading.collectAsStateWithLifecycle().value
    LoadingView(modifier, loading)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LoadingView(
    modifier: Modifier = Modifier,
    loading: Boolean,
) {
    if (!loading) return
    LinearProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.tertiary,
        trackColor = MaterialTheme.colorScheme.onPrimary,
    )
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun LoadingPlaceholder(
    modifier: Modifier = Modifier,
    loading: Boolean,
) {
    if (!loading) return
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(APP_BACKGROUND),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieLoader(
            rawRes = R.raw.loading,
            forever = true,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun LoadingMainPlaceholder(
    modifier: Modifier = Modifier,
    loading: Boolean,
) {
    if (!loading) return
    val modifiers = modifier.wrapContentSize()

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LottieLoader(
            modifiers
                .width(80.dp)
                .height(80.dp),
            rawRes = R.raw.loading,
            forever = true,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun TrendShortsMenuButton(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
    loading: Boolean,
    onclick: () -> Unit,
) {
    if (!loading) return

    val adFixedBannerState by mainViewModel.fixedBannerState.collectAsState()
    var adSize by remember { mutableStateOf(64) }
    adSize =
        when {
            adFixedBannerState is AdMobBannerState.AdLoadComplete -> {
                (adFixedBannerState as AdMobBannerState.AdLoadComplete).adSize.height
            }

            else -> {
                0
            }
        }
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = adSize.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        LottieLoader(
            modifier
                .wrapContentSize()
                .clickable(onClick = onclick),
            rawRes = R.raw.trend_shorts,
            forever = true,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun LoadingPlaceholderPreview() {
    // TrendShortsMenuButton(loading = true, onclick = {})
}

// @Preview(showBackground = true)
// @Composable
// private fun LoadingPreview() {
//    LoadingView(loading = true)
// }
