package com.sean.ratel.android.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MinLaunchCount

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MinDaysSinceInstall

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MinDaysBetweenInAppAttempts

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MinDaysBetweenCustomPrompts

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MaxCustomPromptsPerYear

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InAppSuccessRateThreshold

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MinAttemptsBeforeCustom

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EmotionScoreThreshold

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AllowReviewOnResume

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugMode
