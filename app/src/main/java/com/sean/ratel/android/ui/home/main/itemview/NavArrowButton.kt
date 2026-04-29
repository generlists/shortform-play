package com.sean.ratel.android.ui.home.main.itemview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("ktlint:standard:function-naming")
@Composable
fun NavArrowButton(
    modifier: Modifier,
    isHome: Boolean = false,
    size: Dp = 16.dp,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isHome) Icons.AutoMirrored.Default.ArrowForward else Icons.AutoMirrored.Default.ArrowRightAlt,
                contentDescription = null,
                modifier = Modifier.size(size),
                tint = Color.White,
            )
        }
    }
}
