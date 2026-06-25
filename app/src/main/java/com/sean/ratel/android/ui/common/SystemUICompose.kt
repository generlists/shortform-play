package com.sean.ratel.android.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_NAVIGATION_COLOR

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

@SuppressLint("ContextCastToActivity")
@Suppress("ktlint:standard:function-naming")
@Composable
fun SystemBars(isDarkMode: Boolean) {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(isDarkMode) {
        activity?.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    Color.Black.toArgb(),
                ),
            navigationBarStyle =
                SystemBarStyle.dark(
                    Color.Black.toArgb(),
                ),
        )

        onDispose { }
    }
}

fun Context.findActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@SuppressLint("ContextCastToActivity")
@Suppress("ktlint:standard:function-naming")
@Composable
fun SystemBars() {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(Unit) {
        activity?.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    APP_NAVIGATION_COLOR.toArgb(),
                ),
            navigationBarStyle =
                SystemBarStyle.dark(
                    APP_NAVIGATION_COLOR.toArgb(),
                ),
        )

        onDispose { }
    }
}
