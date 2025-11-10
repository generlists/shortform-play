package com.sean.ratel.android

import android.app.Application
import com.sean.player.utils.log.RLog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShortFormPlayApplication : Application() {
    companion object {
        lateinit var ratelApp: ShortFormPlayApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        RLog.init(
            this,
            enableAllLogger = if (BuildConfig.DEBUG) true else false,
            enableShowLogWithLinkToSource = false,
            enableUdpLogger = false,
        )
    }
}
