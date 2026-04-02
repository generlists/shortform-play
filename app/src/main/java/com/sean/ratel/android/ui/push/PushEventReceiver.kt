package com.sean.ratel.android.ui.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.repository.PushReceiverEntryPoint
import dagger.hilt.android.EntryPointAccessors
import so.smartlab.common.push.fcm.data.domain.PushEvent

class PushEventReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                PushReceiverEntryPoint::class.java,
            )

        val pushRepository = entryPoint.pushRepository()

        if (intent.action == "com.sean.ratel.PUSH_EVENT") {
            val type = intent.getStringExtra("type")
            val title = intent.getStringExtra("title")
            val body = intent.getStringExtra("body")

            @Suppress("UNCHECKED_CAST")
            val data = intent.getSerializableExtra("data") as? HashMap<String, String> ?: hashMapOf()

            RLog.d("PUSH_TEST", "RECEIVER type=$type title=$title body=$body data=$data")

            val event =
                PushEvent.Message(
                    type = type,
                    title = title,
                    body = body,
                    data = data,
                )
            pushRepository.onPushArrived(event)
        } else if (intent.action == "com.sean.ratel.PUSH_UPDATE_TOKEN") {
            val token = intent.getStringExtra("token")
            token?.let {
                val event =
                    PushEvent.TokenUpdated(
                        token = token,
                    )
                pushRepository.onPushArrived(event)
            }
        }
    }
}
