package com.sean.ratel.android.utils

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import so.smartlab.common.review.EmotionAction
import so.smartlab.common.review.ReviewManager
import so.smartlab.common.review.ReviewTrigger

/**
 * 영상 시청 완료 추적
 * @param watchPercentage 시청 비율 (0.0 ~ 1.0)
 * @param consecutiveCount 현재 연속 시청 횟수 (앱에서 관리)
 * @return 업데이트된 연속 시청 횟수
 */
fun ReviewManager.trackVideoWatch(
    watchPercentage: Float,
    consecutiveCount: Int,
): Int =
    when {
        watchPercentage >= 0.7f -> {
            trackEmotionAction(EmotionAction.WATCH_COMPLETE)
            consecutiveCount + 1
        }

        watchPercentage >= 0.3f -> {
            trackEmotionAction(EmotionAction.WATCH_70_PERCENT)
            consecutiveCount + 1
        }

        else -> {
            // 스킵으로 간주
            trackEmotionAction(EmotionAction.SKIP_QUICK)
            0 // 연속 카운트 리셋
        }
    }

/**
 * 연속 시청 체크 및 리뷰 요청
 * @param activity 현재 Activity
 * @param consecutiveCount 연속 시청 횟수
 * @param scope Coroutine Scope
 * @param threshold 리뷰 요청 임계값 (기본 10개)
 */
fun ReviewManager.checkContinuousWatching(
    activity: Activity,
    consecutiveCount: Int,
    scope: CoroutineScope,
    threshold: Int = 10,
    delayMillis: Long = 800L,
) {
    if (consecutiveCount == threshold) {
        scope.launch {
            delay(delayMillis) // 영상 전환 애니메이션 대기
            requestReview(activity, ReviewTrigger.ContinuousWatching(consecutiveCount))
        }
    }
}

/**
 * 좋아요 연타 추적
 * @param activity 현재 Activity
 * @param sessionLikeCount 세션 내 좋아요 횟수 (앱에서 관리)
 * @param scope Coroutine Scope
 * @param threshold 임계값 (기본 3개)
 * @return 업데이트된 좋아요 횟수
 */
fun ReviewManager.trackLike(
    activity: Activity,
    sessionLikeCount: Int,
    scope: CoroutineScope,
    threshold: Int = 3,
    delayMillis: Long = 800L,
): Int {
    trackEmotionAction(EmotionAction.LIKE)
    val newCount = sessionLikeCount + 1

    if (newCount == threshold) {
        scope.launch {
            delay(delayMillis)
            requestReview(activity, ReviewTrigger.LikeSpree(newCount))
        }
    }

    return newCount
}

/**
 * 저장 액션 (강력한 긍정 신호)
 * @param activity 현재 Activity
 * @param scope Coroutine Scope
 */
fun ReviewManager.trackSave(
    activity: Activity,
    scope: CoroutineScope,
    delayMillis: Long = 800L,
) {
    trackEmotionAction(EmotionAction.SAVE)

    scope.launch {
        delay(delayMillis)
        requestReview(activity, ReviewTrigger.SaveAction)
    }
}

/**
 * 셋팅이나 바로 요청하고 싶을때
 * @param activity 현재 Activity
 * @param scope Coroutine Scope
 */
fun ReviewManager.trackDirect(
    activity: Activity,
    scope: CoroutineScope,
    delayMillis: Long = 0L,
) {
    // trackEmotionAction(EmotionAction.DIRECT)

    scope.launch {
        delay(delayMillis)
        requestReview(activity, ReviewTrigger.DirectAction)
    }
}

/**
 * 공유 액션 (최강 긍정 신호)
 * @param activity 현재 Activity
 * @param scope Coroutine Scope
 */
fun ReviewManager.trackShare(
    activity: Activity,
    scope: CoroutineScope,
    delayMillis: Long = 800L,
) {
    trackEmotionAction(EmotionAction.SHARE)

    scope.launch {
        delay(delayMillis)
        requestReview(activity, ReviewTrigger.ShareAction)
    }
}

/**
 * 크리에이터 팔로우
 * @param activity 현재 Activity
 * @param totalFollows 총 팔로우 수 (앱에서 관리)
 * @param scope Coroutine Scope
 * @return 업데이트된 팔로우 수
 */
fun ReviewManager.trackFollow(
    activity: Activity,
    totalFollows: Int,
    scope: CoroutineScope,
    delayMillis: Long = 800L,
): Int {
    trackEmotionAction(EmotionAction.FOLLOW)
    val newCount = totalFollows + 1

    if (newCount == 3 || newCount == 10) {
        scope.launch {
            delay(delayMillis)
            requestReview(activity, ReviewTrigger.CreatorFollow(newCount))
        }
    }

    return newCount
}

/**
 * 댓글 작성
 */
fun ReviewManager.trackComment() {
    trackEmotionAction(EmotionAction.COMMENT)
}

/**
 * 일일 사용 시간 체크
 * @param activity 현재 Activity
 * @param sessionMinutes 세션 누적 분
 * @param scope Coroutine Scope
 * @param threshold 임계값 (기본 30분)
 */
fun ReviewManager.checkDailyEngagement(
    activity: Activity,
    sessionMinutes: Int,
    scope: CoroutineScope,
    threshold: Int = 30,
) {
    if (sessionMinutes == threshold) {
        scope.launch {
            requestReview(activity, ReviewTrigger.DailyEngagement(sessionMinutes))
        }
    }
}

/**
 * Compose에서 영상 시청 처리 (연속 카운트 자동 관리)
 *
 * @param watchPercentage 시청 비율 (0.0 ~ 1.0)
 * @param activity 현재 Activity
 * @param scope Coroutine Scope
 * @param currentCount 현재 연속 카운트
 * @param threshold 리뷰 요청 임계값 (기본 10개)
 * @param delayMillis 리뷰 요청 전 딜레이 (기본 800ms)
 * @return 업데이트된 연속 카운트
 */
fun ReviewManager.onVideoWatched(
    watchPercentage: Float,
    activity: Activity,
    scope: CoroutineScope,
    currentCount: Int = 0,
    threshold: Int = 10,
    delayMillis: Long = 800L,
): Int =
    when {
        // 70% 이상 시청
        watchPercentage >= 0.7f -> {
            trackEmotionAction(EmotionAction.WATCH_COMPLETE)
            val newCount = currentCount + 1

            // 임계값 도달 시 리뷰 요청
            if (newCount == threshold) {
                scope.launch {
                    delay(delayMillis)
                    requestReview(activity, ReviewTrigger.ContinuousWatching(newCount))
                }
            }
            newCount
        }

        // 30% 이상 시청
        watchPercentage >= 0.3f -> {
            trackEmotionAction(EmotionAction.WATCH_70_PERCENT)
            currentCount + 1
        }

        // 스킵 (연속 카운트 리셋)
        else -> {
            trackEmotionAction(EmotionAction.SKIP_QUICK)
            0
        }
    }

/**
 * Compose에서 좋아요 처리 (세션 카운트 자동 관리)
 *
 * @param activity 현재 Activity
 * @param scope Coroutine Scope
 * @param currentCount 현재 세션 좋아요 카운트
 * @param threshold 리뷰 요청 임계값 (기본 3개)
 * @param delayMillis 리뷰 요청 전 딜레이 (기본 800ms)
 * @return 업데이트된 세션 좋아요 카운트
 */
fun ReviewManager.onLikeClicked(
    activity: Activity,
    scope: CoroutineScope,
    currentCount: Int = 0,
    threshold: Int = 3,
    delayMillis: Long = 800L,
): Int {
    trackEmotionAction(EmotionAction.LIKE)
    val newCount = currentCount + 1
    Log.d("ReviewKit", "newCount : $newCount , threshold : $threshold")

    if (newCount == threshold) {
        scope.launch {
            delay(delayMillis)
            requestReview(activity, ReviewTrigger.LikeSpree(newCount))
        }
    }

    return newCount
}

/**
 * Compose에서 저장 처리
 */
fun ReviewManager.onSaveClicked(
    activity: Activity,
    scope: CoroutineScope,
    delayMillis: Long = 800L,
) {
    trackEmotionAction(EmotionAction.SAVE)

    scope.launch {
        delay(delayMillis)
        requestReview(activity, ReviewTrigger.SaveAction)
    }
}

/**
 * Compose에서 공유 처리 (점수만 누적)
 */

fun ReviewManager.onShareClicked(
    activity: Activity,
    scope: CoroutineScope,
    delayMillis: Long = 800L,
) {
    trackEmotionAction(EmotionAction.SHARE)
    // 리뷰는 다음 액션에서 자동 요청
    scope.launch {
        delay(delayMillis)
        requestReview(activity, ReviewTrigger.SaveAction)
    }
}

/**
 * 마일스톤 메시지 생성
 */
fun ReviewTrigger.toMilestoneMessage(): String =
    when (this) {
        is ReviewTrigger.ContinuousWatching -> {
            "벌써 ${count}개 영상을 연속으로 보셨네요! 🎉"
        }

        is ReviewTrigger.LikeSpree -> {
            "마음에 드는 영상을 ${count}개나 저장하셨네요! ❤️"
        }

        is ReviewTrigger.SaveAction -> {
            "마음에 드는 영상을 저장하셨군요! 😊"
        }

        is ReviewTrigger.ShareAction -> {
            "영상을 공유해주셔서 감사합니다! 🙏"
        }

        is ReviewTrigger.CreatorFollow -> {
            "좋아하는 크리에이터를 ${count}명이나 팔로우하셨네요! ✨"
        }

        is ReviewTrigger.DailyEngagement -> {
            "${minutes}분째 함께해주셔서 감사합니다! ⏰"
        }

        else -> {
            "앱을 사용해주셔서 감사합니다! 🎉"
        }
    }
