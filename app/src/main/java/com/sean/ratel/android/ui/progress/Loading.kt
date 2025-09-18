package com.sean.ratel.android.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.REMAIN_AD_MARGIN
import com.sean.ratel.android.ui.ad.AdViewModel
import com.sean.ratel.android.ui.theme.APP_BACKGROUND

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
    adViewModel: AdViewModel,
    modifier: Modifier = Modifier,
    loading: Boolean,
    onclick: () -> Unit,
) {
    if (!loading) return
    val adBannerLoadingComplete = adViewModel.adBannerLoadingCompleteAndGetAdSize.collectAsState()
    val insetPaddingValue = WindowInsets.statusBars.asPaddingValues()
    Box(
        Modifier
            .fillMaxSize()
            .then(
                if (adBannerLoadingComplete.value.first) {
                    Modifier.padding(
                        bottom =
                            adBannerLoadingComplete.value.second.dp + insetPaddingValue.calculateTopPadding() +
                                REMAIN_AD_MARGIN + 20.dp,
                        end = 20.dp,
                    )
                } else {
                    Modifier.padding(
                        bottom =
                            insetPaddingValue.calculateTopPadding() +
                                REMAIN_AD_MARGIN + 20.dp,
                        end = 20.dp,
                    )
                },
            ),
        contentAlignment = Alignment.BottomEnd,
    ) {
        LottieLoader(
            modifier.wrapContentSize().clickable(onClick = onclick),
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
