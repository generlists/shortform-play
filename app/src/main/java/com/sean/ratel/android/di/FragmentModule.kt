package com.sean.ratel.android.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class) // 혹은 SingletonComponent 등 적절한 컴포넌트
object FragmentModule
