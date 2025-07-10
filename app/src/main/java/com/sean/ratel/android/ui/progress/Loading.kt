package com.sean.ratel.android.ui.progress

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
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
    val modifier = modifier.wrapContentSize()
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LottieLoader(
            modifier
                .width(80.dp)
                .height(80.dp),
            rawRes = R.raw.loading,
            forever = true,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun LoadingPlaceholderPreview() {
    LoadingMainPlaceholder(loading = true)
}

// @Preview(showBackground = true)
// @Composable
// private fun LoadingPreview() {
//    LoadingView(loading = true)
// }
