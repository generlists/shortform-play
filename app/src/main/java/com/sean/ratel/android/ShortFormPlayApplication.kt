package com.sean.ratel.android

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShortFormPlayApplication : Application() {
    companion object {
        lateinit var ratelApp: ShortFormPlayApplication
            private set

        fun getContext(): Context = ratelApp.applicationContext
    }
}
