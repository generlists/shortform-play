package com.sean.ratel.android.data.repository

import android.content.Context
import com.sean.ratel.android.data.common.STRINGS.NOTIFICATON_LIMIT_COUNT
import com.sean.ratel.android.data.domain.model.push.toPushModel
import com.sean.ratel.android.data.filter.PushFilterGate
import com.sean.ratel.android.data.local.pref.PushPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import so.smartlab.common.push.fcm.data.domain.PushEvent
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushBackgroundManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val pushNotificationManager: PushNotificationManager,
        val pushPreference: PushPreference,
        val pushFilterGate: PushFilterGate,
    ) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun onPushArrived(event: PushEvent) {
            RLog.d("PUSH_TEST", "PushBackgroundManager repo $event")
            when (event) {
                is PushEvent.Message -> {
                    scope.launch {
                        // 추천 푸시가 내 제외 필터에 걸리면 알림도, 알림함 기록도 남기지 않는다.
                        // 판정이 실패하면(필터 읽기 오류 등) 예전처럼 보여 준다
                        val excluded = runCatching { pushFilterGate.excludeMatch(event.data) }.getOrNull()
                        if (excluded != null) {
                            RLog.d("PUSH_TEST", "push filtered by $excluded")
                            return@launch
                        }
                        pushNotificationManager.show(context, event.data)
                        pushPreference.saveNewPush(true)
                        val currentList = pushPreference.getPushList().first()
                        val pushModel = event.data.toPushModel()

                        val existing =
                            currentList.find {
                                it.id == pushModel.id
                            }

                        // 완전히 같은 값이면 아무것도 안 함
                        if (existing == pushModel) {
                            return@launch
                        }

                        // 200 개 limit
                        val updatedList = listOf(pushModel) + currentList.take(NOTIFICATON_LIMIT_COUNT)
                        pushPreference.updatePush(updatedList)
                    }
                }

                is PushEvent.TokenUpdated -> {
                    scope.launch {
                        pushPreference.saveToken(event.token)
                    }
                }

                else -> {
                    Unit
                }
            }
        }
    }
