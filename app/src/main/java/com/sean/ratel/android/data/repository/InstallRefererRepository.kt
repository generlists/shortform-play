package com.sean.ratel.android.data.repository

import com.sean.ratel.android.data.local.pref.InstallReferePreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
InstallRefererRepository
    @Inject
    constructor(
        private val installReferPreference: InstallReferePreference,
    ) {
        suspend fun setInstallReferer(path: String) {
            installReferPreference.setInstallReferer(path)
        }

        suspend fun getInstallReferer(): String? = installReferPreference.getInstallReferer()
    }
