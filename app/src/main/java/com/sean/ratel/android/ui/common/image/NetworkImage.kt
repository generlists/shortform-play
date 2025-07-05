package com.sean.ratel.android.ui.common.image

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.google.accompanist.drawablepainter.DrawablePainter
import com.sean.ratel.android.R

@Suppress("ktlint:standard:function-naming")
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes errorRes: Int = R.drawable.ad_native_default_background,
    @DrawableRes fallbackRes: Int = R.drawable.ad_native_default_background,
    @DrawableRes placeholderRes: Int = R.drawable.ad_native_default_background,
) {
    val context = LocalContext.current
    val errorDrawable = AppCompatResources.getDrawable(context, errorRes)
    val fallbackResDrawable = AppCompatResources.getDrawable(context, fallbackRes)
    val placeholderDrawable = AppCompatResources.getDrawable(context, placeholderRes)

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        placeholder = placeholderDrawable?.let { DrawablePainter(placeholderDrawable) } ?: painterResource(R.drawable.thumb_placeholder),
        error = errorDrawable?.let { DrawablePainter(errorDrawable) } ?: painterResource(R.drawable.thumb_placeholder),
        fallback = fallbackResDrawable?.let { DrawablePainter(fallbackResDrawable) } ?: painterResource(R.drawable.thumb_placeholder),
        modifier = modifier,
        contentScale = contentScale,
    )
}
