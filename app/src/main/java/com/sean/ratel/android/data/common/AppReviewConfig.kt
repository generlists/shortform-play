package com.sean.ratel.android.data.common

import so.smartlab.common.review.ReviewConfig
import javax.inject.Singleton

@Singleton
class AppReviewConfig(
    override val minLaunchCount: Int = 5,
    override val minDaysSinceInstall: Int = 3,
    override val minDaysBetweenInAppAttempts: Int = 7,
    override val minDaysBetweenCustomPrompts: Int = 60,
    override val maxCustomPromptsPerYear: Int = 2,
    override val inAppSuccessRateThreshold: Float = 0.2f,
    override val minAttemptsBeforeCustom: Int = 5,
    override val emotionScoreThreshold: Int = 100,
    override val allowReviewOnResume: Boolean = true,
    override val debugMode: Boolean = false,
) : ReviewConfig
