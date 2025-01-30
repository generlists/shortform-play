package com.sean.ratel.android.data.android.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.sean.player.utils.log.RLog
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

/*
 isGranted       isRationale         result
*  ------------------------------------------------------------
*  true            false               granted
*  true            true                granted
*  false           false               before first permission deny or "never ask again checked"
*  false           true                after first deny
 카메라, 위치, 마이크는 좀 더 민감 3가지로 분류
*/
class PermissionManager
    @Inject
    constructor() : PermissionProvider {
        override fun requestPermissions(
            permissionLauncher: ActivityResultLauncher<Array<String>>,
            permissions: Array<String>,
        ) {
            permissionLauncher.launch(permissions)
        }

        override fun handlePermissionResult(
            @ActivityContext activity: Context,
            permissions: Map<String, Boolean>,
        ): PermissionState {
            val deniedPermissionList = permissions.filter { !it.value }.map { it.key }

            when {
                deniedPermissionList.isNotEmpty() -> {
                    val map =
                        deniedPermissionList.groupBy { permission ->
                            if (shouldShowRequestPermissionRationale(
                                    activity as Activity,
                                    permission,
                                )
                            ) {
                                DENIED
                            } else {
                                EXPLAINED
                            }
                        }
                    map[DENIED]?.let {
                        val findPhone = it.filter { s -> s == Manifest.permission.READ_PHONE_STATE }
                        if (findPhone.isNotEmpty()) {
                            // 전화는 필수 권한입니다. 권한을 승인해주세요.
                            RLog.d(TAG, "전화 권한이 거부 되었을 때 알림 $it")
                            return PermissionState.PHONE_DENIED
                        }
                        // 앱 진입 시작
                    }
                    map[EXPLAINED]?.let {
                        // 전화 권한 요청이 완전히 막혔을 때(주로 앱 상세 창 열기)
                        val findPhone = it.filter { s -> s == Manifest.permission.READ_PHONE_STATE }
                        if (findPhone.isNotEmpty()) {
                            RLog.d(TAG, "권한 요청이 완전히 막혔을 때(주로 앱 상세 창 열기) $it")
                            return PermissionState.PHONE_EXPLAINED
                        }
                    }
                }
                else -> {
                    // 모든 권한이 허가 되었을 때
                    // 앱 진입
                    RLog.d(TAG, "모든 권한 요청 완료 앱 진입 ")
                    return PermissionState.ACCESS_PERMISSION
                }
            }
            return PermissionState.ACCESS_PERMISSION
        }

        companion object {
            private const val TAG = "PermissionManager"

            // 요청할 권한 목록
            val permissions =
                arrayOf(
                    // 필수
                    Manifest.permission.READ_PHONE_STATE,
                    // 옵션: 주소록 연동시에 다시 권한 요청해야함
                    Manifest.permission.READ_CONTACTS,
                    // 필수(앱 사용 못함)//target 34 이상
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            private const val EXPLAINED = "EXPLAINED"
            private const val DENIED = "DENIED"
        }

        enum class PermissionState {
            INIT,
            PHONE_DENIED,
            PHONE_EXPLAINED,
            ACCESS_PERMISSION,
        }
    }
