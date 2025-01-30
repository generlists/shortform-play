package com.sean.ratel.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("ktlint:standard:function-naming")
@Composable
internal fun NavHandler(
    navController: NavController,
    navigator: Navigator,
    finish: () -> Unit,
) {
    LaunchedEffect("navigation") {
        navigator.navigate
            .onEach {
                if (it.popBackstack) navController.popBackStack()
                navController.navigate(it.route) {
                    launchSingleTop = true // 중복된 인스턴스 생성 방지
                    restoreState = true // 이전 상태 복원
                }
            }.launchIn(this)

        navigator.back
            .onEach {
                if (it.recreate) {
                    navController.previousBackStackEntry?.destination?.route?.let { route ->
                        navController.navigate(route) {
                            popUpTo(route) { inclusive = true }
                        }
                    }
                } else {
                    navController.navigateUp()
                }
            }.launchIn(this)

        navigator.end
            .onEach {
                finish()
            }.launchIn(this)
    }
}
