package com.sean.ratel.android

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.common.RemoteConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShortFormPlayApplication : Application() {
    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate() {
        super.onCreate()
        RLog.init(
            this,
            enableAllLogger = if (BuildConfig.DEBUG) true else false,
            enableShowLogWithLinkToSource = false,
            enableUdpLogger = false,
        )

        firebaseRemoteConfig(remoteConfig, onComplete = {
            RemoteConfig.loadComplete(true)
        })
    }

    fun firebaseRemoteConfig(
        remoteConfig: FirebaseRemoteConfig,
        onComplete: () -> Unit,
    ) {
        remoteConfig
            .setDefaultsAsync(R.xml.remote_config_defaults)
            .addOnSuccessListener {
                // 1. 일단 기본값 적용
                RemoteConfig.setRemoteConfig(remoteConfig.all)
                remoteConfig
                    .fetchAndActivate()
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            remoteConfig.all.forEach { entry ->

                                RLog.d(
                                    "Application",
                                    "Config real Log : ${entry.key} = ${entry.value.asString()}",
                                )
                            }

                            // 2. 서버값 적용

                            RemoteConfig.setRemoteConfig(remoteConfig.all)
                        } else {
                            RLog.e(
                                "hbungshin",
                                "Fetch failed: ${task.exception}",
                            )
                        }

                        // 3. 여기서 앱 진행
                        onComplete()
                    }
            }.addOnFailureListener {
                RLog.e("Application", "Default load failed: $it")

                onComplete()
            }
    }
}
