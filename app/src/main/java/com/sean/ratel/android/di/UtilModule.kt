package com.sean.ratel.android.di

import androidx.annotation.OptIn
import com.sean.ratel.android.data.android.toolbox.PhoneAppManager
import com.sean.ratel.android.data.android.toolbox.PhoneAppProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides
    @Singleton
    @OptIn(UnstableApi::class)
    fun providePhoneAppManagerr(): PhoneAppProvider = PhoneAppManager()
}
