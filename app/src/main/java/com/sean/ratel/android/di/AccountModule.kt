package com.sean.ratel.android.di

import com.sean.ratel.android.data.account.AccountRepository
import com.sean.ratel.android.data.account.AccountSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {
    @Binds
    @Singleton
    abstract fun bindAccountSession(impl: AccountRepository): AccountSession
}
