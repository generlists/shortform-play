package com.sean.ratel.android.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdsRemoveProductId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfferId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SubscriptionProductId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ConsumeProductId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoveAdsOriginalPriceMicros
