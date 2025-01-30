package com.sean.ratel.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.ui.theme.Red

@Suppress("ktlint:standard:function-naming")
@Composable
fun FullScreenToggleView(route: String) {
    // SystemUiController 사용하여 시스템 UI 제어
    val systemUiController = rememberSystemUiController()

    // ViewModel에서 isFullScreen 상태를 관찰
    // val isFullScreen by viewModel.isFullScreen.collectAsState()
    // 시스템 UI 설정
    if (route == Destination.Splash.route) {
        // 전체 화면 모드
        systemUiController.setSystemBarsColor(
            color = Color.White,
            darkIcons = false,
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = true,
        )
        systemUiController.isStatusBarVisible = false
        systemUiController.isNavigationBarVisible = false
    } else {
        when (route) {
            Destination.YouTube.route -> {
                systemUiController.setSystemBarsColor(
                    // 상태바 색상 복구
                    color = Color.Black,
                    // 흰색 아이콘 (어두운 배경에서 사용)
                    darkIcons = false,
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Black,
                    darkIcons = true,
                )
                systemUiController.isStatusBarVisible = true
                systemUiController.isNavigationBarVisible = true
            }
            else -> {
                systemUiController.setSystemBarsColor(
                    // 상태바 색상 복구
                    color = Red,
                    // 흰색 아이콘 (어두운 배경에서 사용)
                    darkIcons = false,
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent,
                    darkIcons = true,
                )
                systemUiController.isStatusBarVisible = true
                systemUiController.isNavigationBarVisible = true
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun UpdateStateBar() {
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(
            // 상태바 색상 복구
            color = Color.Black,
            // 흰색 아이콘 (어두운 배경에서 사용)
            darkIcons = false,
        )
        systemUiController.setNavigationBarColor(
            color = Color.Black,
            darkIcons = true,
        )
        systemUiController.isStatusBarVisible = true
        systemUiController.isNavigationBarVisible = true
    }
}
