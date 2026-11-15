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
                } else if (navController.previousBackStackEntry != null) {
                    navController.navigateUp()
                } else {
                    // 탭으로 옮겨 온 화면(설정·숏폼)은 뒤가 비어 있다. 이때 navigateUp() 은 앱을 새 작업으로 다시 시작해서
                    // 스플래시를 건너뛴 채 홈이 열리고 홈 데이터가 비어 버린다 → 홈 탭으로 돌아간다
                    navController.navigate(Destination.Home.Main.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }.launchIn(this)

        navigator.end
            .onEach {
                finish()
            }.launchIn(this)
    }
}
