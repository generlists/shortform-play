package com.sean.ratel.android.ui.common.image

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.accompanist.drawablepainter.DrawablePainter
import com.sean.ratel.android.R

@Suppress("ktlint:standard:function-naming")
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes errorRes: Int = R.drawable.thumb_placeholder,
    @DrawableRes fallbackRes: Int = R.drawable.thumb_placeholder,
    @DrawableRes placeholderRes: Int = R.drawable.thumb_placeholder,
    imageLoader: ImageLoader? = null,
) {
    val context = LocalContext.current
    val errorDrawable = AppCompatResources.getDrawable(context, errorRes)
    val fallbackResDrawable = AppCompatResources.getDrawable(context, fallbackRes)
    val placeholderDrawable = AppCompatResources.getDrawable(context, placeholderRes)

    imageLoader?.let {
        AsyncImage(
            imageLoader = imageLoader,
            model = url,
            contentDescription = contentDescription,
            placeholder =
                placeholderDrawable?.let { DrawablePainter(placeholderDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            error =
                errorDrawable?.let { DrawablePainter(errorDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            fallback =
                fallbackResDrawable?.let { DrawablePainter(fallbackResDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            modifier = modifier,
            contentScale = contentScale,
        )
    } ?: run {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            placeholder =
                placeholderDrawable?.let { DrawablePainter(placeholderDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            error =
                errorDrawable?.let { DrawablePainter(errorDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            fallback =
                fallbackResDrawable?.let { DrawablePainter(fallbackResDrawable) }
                    ?: painterResource(R.drawable.thumb_placeholder),
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun NetworkImage(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader?,
    url: String,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes placeholderRes: Int = R.drawable.vertical_background,
    loadComplete: () -> Unit,
) {
    if (imageLoader == null) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
        return
    }
    SubcomposeAsyncImage(
        model = url,
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    ) {
        val state = painter.state
        var showRealImage by remember(url) { mutableStateOf(false) }
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = alpha)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(placeholderRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillHeight,
                    )
                }
            }

            is AsyncImagePainter.State.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = alpha)),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Image(
                        painter = painterResource(R.drawable.image_flow_loading),
                        contentDescription = null,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillHeight,
                    )
                }
            }

            is AsyncImagePainter.State.Success -> {
//                LaunchedEffect(state.result) {
//                    showRealImage = false
//                    delay(5000)
//                    showRealImage = true
//                    loadComplete()
//                }

//                if (showRealImage) {
                SubcomposeAsyncImageContent()
//                }
//                else {
//                    Box(
//                        modifier = Modifier.background(Color.Red)
//                            .fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Image(
//                            painter = painterResource(R.drawable.vertical_background),
//                            contentDescription = null,
//                            modifier =modifier,
//                            contentScale = ContentScale.Fit
//                        )
//                    }
//                }
                loadComplete()
            }

            else -> {
                SubcomposeAsyncImageContent()
                loadComplete()
            }
        }
    }
}
