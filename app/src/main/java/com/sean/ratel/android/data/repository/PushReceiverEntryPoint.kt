package com.sean.ratel.android.data.repository

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PushReceiverEntryPoint {
    fun pushRepository(): PushBackgroundManager
}
