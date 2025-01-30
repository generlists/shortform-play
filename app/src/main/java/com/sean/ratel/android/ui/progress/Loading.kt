package com.sean.ratel.android.ui.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R

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
                .background(MaterialTheme.colorScheme.background),
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

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier =
                modifier
                    .wrapContentSize()
                    .wrapContentHeight()
                    .aspectRatio(1.0f)
                    .align(Alignment.BottomEnd)
                    .background(Color.Transparent),
        ) {
            LottieLoader(
                rawRes = R.raw.loading,
                forever = true,
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun YouTubeLoader(
    modifier: Modifier = Modifier,
    loading: Boolean,
) {
    if (!loading) return

    // Define infinite transition for the rotation effect
    val infiniteTransition = rememberInfiniteTransition()

    // Animate the rotation angle using a repeating animation
    val animatedRotation by infiniteTransition.animateFloat(
        // Start from 0 degrees
        initialValue = 0f,
        // Rotate to 360 degrees
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                // Adjust duration and easing as needed
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
    )

    // Image composable with rotation effect applied
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            // Replace with your image resource
            painter = painterResource(id = R.drawable.youtube_shorts_logo),
            contentDescription = "Rotating Image",
            modifier =
                modifier
                    .size(64.dp) // Adjust size as needed
                    .rotate(animatedRotation),
            // Apply the rotation animation
            contentScale = ContentScale.Crop,
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
