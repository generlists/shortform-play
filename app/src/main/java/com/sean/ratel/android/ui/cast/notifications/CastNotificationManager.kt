package com.sean.ratel.android.ui.cast.notifications

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.ui.cast.CastEvent
import com.sean.ratel.android.ui.cast.CastSessionState
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.player.demo.ui.cast.notifications.CastNotificationState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastNotificationManager
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val playersManager: YouTubePlayersManager,
    ) {
        private val _state = MutableStateFlow(CastNotificationState())
        val state: StateFlow<CastNotificationState> = _state.asStateFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        init {

            scope.launch {

                combine(
                    playersManager.castEventManager.castSession,
                    playersManager.currentVideo,
                    playersManager.castEventManager.playEvents,
                    playersManager.isFirst,
                    playersManager.isLast,
                ) { session, video, playEvent, isFirst, isLast ->
                    TransferData(session, video, playEvent, isFirst, isLast)
                }.collect { transferData ->

                    RLog.d("CastNotificationManager", "session : ${transferData.session}")
                    RLog.d("CastNotificationManager", "video : ${transferData.currentVideo?.shortsVideoModel?.title}")
                    RLog.d("CastNotificationManager", "playEvent : ${transferData.playEvent}")
                    if (transferData.currentVideo == null && transferData.session is CastSessionState.SessionStart) {
                        _state.value =
                            _state.value.copy(
                                isVisible = true,
                                isFinish = false,
                                title = "캐스트 접속이 완료 되었습니다.",
                                deviceName = transferData.session.castDevice ?: "알수 없음",
                                channelName = transferData.currentVideo?.shortsChannelModel?.channelTitle ?: "",
                                isFirst = transferData.isFirst,
                                isLast = transferData.isLast,
                            )
                        return@collect
                    }

                    when (transferData.session) {
                        is CastSessionState.SessionStart -> {
                            val bitmap =
                                loadBitmapWithRightPadding(
                                    context,
                                    transferData.currentVideo?.shortsVideoModel?.thumbNail ?: "",
                                )
                            _state.value =
                                _state.value.copy(
                                    isVisible = true,
                                    title = transferData.currentVideo?.shortsVideoModel?.title ?: "",
                                    deviceName = transferData.session.castDevice ?: "알수 없음",
                                    channelName = transferData.currentVideo?.shortsChannelModel?.channelTitle ?: "",
                                    thumbnail = bitmap,
                                    isPlaying = transferData.playEvent is CastEvent.Play,
                                    isFirst = transferData.isFirst,
                                    isLast = transferData.isLast,
                                )
                        }

                        is CastSessionState.SessionEnd -> {
                            _state.value = CastNotificationState(isFinish = true)
                        }

                        else -> {}
                    }
                }
            }
        }

        suspend fun loadBitmapWithRightPadding(
            context: Context,
            url: String,
        ): Bitmap? {
            if (url.isEmpty()) return null

            val loader = ImageLoader(context)

            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .allowHardware(false) // Bitmap 필요
                    .build()

            val result = loader.execute(request)

            return (result.drawable as? BitmapDrawable)?.bitmap
        }

        fun release() {
            // playersManager.release()
//        sessionManagerListener?.let {
//            CastContext.getSharedInstance(context)
//                .sessionManager
//                .removeSessionManagerListener(it, CastSession::class.java)
//        }
        }

        fun setNotificationState(state: CastNotificationState) {
            _state.value = state
        }

        data class TransferData(
            val session: CastSessionState,
            val currentVideo: MainShortsModel?,
            val playEvent: CastEvent,
            val isFirst: Boolean,
            val isLast: Boolean,
        )
    }
