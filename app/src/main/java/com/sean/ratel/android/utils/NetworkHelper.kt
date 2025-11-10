package com.sean.ratel.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.sean.player.utils.log.RLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun isNetworkConnected(): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            RLog.d(TAG, "networkCapabilities : $networkCapabilities , actNw : $actNw")
            return (
                actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            )
        }

        companion object {
            private const val TAG = "NetworkHelper"
        }
    }
