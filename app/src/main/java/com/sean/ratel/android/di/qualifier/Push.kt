package com.sean.ratel.android.di.qualifier

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppId

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Region

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceModel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiUrl
