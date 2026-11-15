package com.sean.ratel.android.data.watch

import android.os.SystemClock
import com.sean.ratel.android.data.dto.MainShortsModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 재생 화면이 부르는 입구. 재생 중인 영상의 시간을 [WatchTracker] 로 재고, 나온 기록을 순서대로 저장한다.
 * 저장은 별도 스코프에서 하므로 화면이 닫혀도 마지막 기록이 남는다.
 */
@Singleton
class WatchRecorder
    @Inject
    constructor(
        private val repository: WatchStatsRepository,
    ) {
        // 저장 작업을 한 줄로 세워 순서대로 한다(시간 기록과 "뺀 영상" 기록이 섞여도 서로 덮어쓰지 않게)
        private val queue = Channel<suspend () -> Unit>(Channel.UNLIMITED)
        private var lastHidden: Set<String> = emptySet()
        private val tracker =
            WatchTracker(
                nowMs = { SystemClock.elapsedRealtime() },
                today = { LocalDate.now() },
                emit = { delta -> queue.trySend { repository.record(delta) } },
            )

        init {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                for (job in queue) {
                    runCatching { job() }
                }
            }
        }

        /** 이 영상이 지금 재생 중인지(그리고 화면에 선택된 영상인지) 알린다. */
        fun update(
            video: MainShortsModel?,
            playing: Boolean,
        ) {
            val id = video?.shortsVideoModel?.videoId.orEmpty()
            tracker.update(id, video?.shortsChannelModel?.channelId.orEmpty(), video?.shortsChannelModel?.channelTitle.orEmpty(), playing)
        }

        /** 내 필터가 지금 목록에서 뺀 영상들을 알린다(오늘 센 것과 같으면 다시 저장하지 않는다). */
        @Synchronized
        fun recordHidden(ids: Set<String>) {
            if (ids.isEmpty() || ids == lastHidden) return
            lastHidden = ids
            queue.trySend { repository.recordHidden(LocalDate.now(), ids) }
        }

        fun tick() = tracker.tick()

        fun stop(video: MainShortsModel? = null) = tracker.stop(video?.shortsVideoModel?.videoId)
    }
