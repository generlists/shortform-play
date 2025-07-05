package com.sean.ratel.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND

@Suppress("ktlint:standard:function-naming")
@Composable
fun FullScreenToggleView(route: String) {
    // SystemUiController 사용하여 시스템 UI 제어
    val systemUiController = rememberSystemUiController()

    if (route == Destination.Splash.route) {
        // 전체 화면 모드
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false,
        )
        systemUiController.setNavigationBarColor(
            color = Color.Black,
            darkIcons = true,
        )
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = false
    } else {
        systemUiController.setSystemBarsColor(
            // 상태바 색상 복구
            color = APP_BACKGROUND,
            // 흰색 아이콘 (어두운 배경에서 사용)
            darkIcons = false,
        )
        systemUiController.setNavigationBarColor(
            color = APP_BACKGROUND,
            darkIcons = false,
        )
        systemUiController.isStatusBarVisible = true
        systemUiController.isNavigationBarVisible = true
    }
}
