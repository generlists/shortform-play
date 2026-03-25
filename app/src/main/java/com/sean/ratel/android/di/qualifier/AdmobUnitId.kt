package com.sean.ratel.android.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdmobUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdOpenUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BannerUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NativeAdUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdaptiveBannerUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InterstitialUnitId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TestHashId
