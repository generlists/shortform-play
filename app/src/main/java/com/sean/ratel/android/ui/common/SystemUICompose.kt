package com.sean.ratel.android.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.sean.ratel.android.ui.navigation.Destination

@Suppress("ktlint:standard:function-naming")
@Composable
fun FullScreenToggleView(route: String) {
    val context = LocalContext.current
    val activity = remember { context.findActivity() }
    val window = remember(activity) { activity?.window }
    val controller =
        remember(window) {
            window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        }

    if (route == Destination.Splash.route) {
        // 스플래시는 완전 풀스크린(바 숨김)
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        controller?.hide(
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars(),
        )
    } else {
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        controller?.show(WindowInsetsCompat.Type.systemBars())
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdateStateBar() {
    FullScreenToggleView(Destination.YouTube.route)
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
