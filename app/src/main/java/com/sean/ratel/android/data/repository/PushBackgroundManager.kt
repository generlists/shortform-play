package com.sean.ratel.android.data.repository

import android.content.Context
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.common.STRINGS.NOTIFICATON_LIMIT_COUNT
import com.sean.ratel.android.data.domain.model.push.toPushModel
import com.sean.ratel.android.data.local.pref.PushPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import so.smartlab.common.push.fcm.data.domain.PushEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushBackgroundManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val pushNotificationManager: PushNotificationManager,
        val pushPreference: PushPreference,
    ) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        fun onPushArrived(event: PushEvent) {
            RLog.d("PUSH_TEST", "PushBackgroundManager repo $event")
            when (event) {
                is PushEvent.Message -> {
                    pushNotificationManager.show(context, event.data)
                    scope.launch {
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

                        // 같은 항목(예: 같은 타입 + 같은 링크)은 지우고 새 값으로 교체
                        val filteredList =
                            currentList.filterNot {
                                it.type == pushModel.type &&
                                    it.linkUrl == pushModel.linkUrl
                            }
                        // 200 개 limit
                        val updatedList = listOf(pushModel) + filteredList.take(NOTIFICATON_LIMIT_COUNT)
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
