package com.sean.ratel.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun DropDownMenuComposable(
    iconColor: Color,
    imageVector: ImageVector,
    modifer: Modifier,
    contentItem: @Composable (Boolean, () -> Unit) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifer,
    ) {
        IconButton(onClick = { menuExpanded = !menuExpanded }) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = iconColor,
            )
        }
        contentItem(menuExpanded) { menuExpanded = false }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ContentItem(
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
) {
    DropdownMenu(
        modifier =
            Modifier
                .wrapContentSize()
                .padding(5.dp),
        expanded = menuExpanded,
        offset = DpOffset(0.dp, 0.dp),
        onDismissRequest = onMenuDismiss,
    ) {
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun DropDownMenuPreView() {
    RatelappTheme {
        DropDownMenuComposable(Color.White, Icons.Filled.MoreVert, Modifier) { menuExpanded, onMenuDismiss ->
            ContentItem(menuExpanded = menuExpanded, onMenuDismiss = onMenuDismiss)
        }
    }
}
