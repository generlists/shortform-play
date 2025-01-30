@file:Suppress("DEPRECATION")

package com.sean.ratel.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT
import android.telephony.TelephonyManager.NETWORK_TYPE_CDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_EDGE
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B
import android.telephony.TelephonyManager.NETWORK_TYPE_GPRS
import android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP
import android.telephony.TelephonyManager.NETWORK_TYPE_IDEN
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_NR
import android.telephony.TelephonyManager.NETWORK_TYPE_UMTS

object NetworkUtil {
    // wifi, cellular, wired
    enum class NetworkInfos(
        val networkType: String?,
        val cellularType: String?,
        var isVpn: Boolean,
    ) {
        NETWORK_TYPE_WIFI("wifi", null, false),
        NETWORK_TYPE_CELLULAR_2G("cellular", "2G", false),
        NETWORK_TYPE_CELLULAR_3G("cellular", "3G", false),
        NETWORK_TYPE_CELLULAR_4G("cellular", "4G", false),
        NETWORK_TYPE_CELLULAR_5G("cellular", "5G", false),
        NETWORK_TYPE_WIRED("wired", null, false),
        NETWORK_TYPE_UNKNOWN(null, null, false),
    }

    private fun getNetworkCellular(context: Context): NetworkInfos {
        for (i in NetworkInfos.values().indices) {
            if (NetworkInfos.values()[i].cellularType == getNetworkSubType(context)) {
                return NetworkInfos.values()[i]
            }
        }
        return NetworkInfos.NETWORK_TYPE_UNKNOWN
    }

    private fun getWhenNotRetrievedNetworkInfo(context: Context): NetworkInfos =
        when {
            isWifiConnected(context) -> {
                NetworkInfos.NETWORK_TYPE_WIFI
            }
            isMobileNetwork(context) -> {
                getNetworkCellular(context)
            }
            else -> {
                NetworkInfos.NETWORK_TYPE_UNKNOWN
            }
        }

    @Suppress("deprecation")
    private fun isWifiConnected(context: Context): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiConnect = wifiManager?.connectionInfo

        val detailWifiInfo = WifiInfo.getDetailedStateOf(wifiConnect?.supplicantState)

        if ((wifiConnect?.ipAddress != 0) &&
            (
                detailWifiInfo == android.net.NetworkInfo.DetailedState.CONNECTED ||
                    detailWifiInfo == android.net.NetworkInfo.DetailedState.OBTAINING_IPADDR
            )
        ) {
            return true
        }
        return false
    }

    @Suppress("deprecation")
    private fun isMobileNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val mobile = connectivityManager?.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return mobile?.isAvailable == true &&
            mobile.detailedState == android.net.NetworkInfo.DetailedState.CONNECTED
    }

    fun getOperationVersion(): String = Build.VERSION.RELEASE

    fun getDeviceModel(): String = Build.MODEL

    fun getMcc(context: Context): String? {
        val simOperator =
            (context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager)?.simOperator

        return if (simOperator == null || simOperator.isEmpty()) {
            null
        } else if (simOperator.length >= 3) {
            simOperator.substring(0, 3)
        } else {
            null
        }
    }

    fun getMnc(context: Context): String? {
        val simOperator =
            (context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager)?.simOperator
        return if (simOperator == null || simOperator.isEmpty()) {
            null
        } else if (simOperator.length >= 3) {
            simOperator.substring(3)
        } else {
            null
        }
    }

    fun getNetworkInfo(context: Context): NetworkInfos {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val activeNetwork =
            connectivityManager?.activeNetwork ?: return NetworkInfos.NETWORK_TYPE_UNKNOWN

        val networkCap =
            connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return NetworkInfos.NETWORK_TYPE_UNKNOWN

        val networkType =
            when {
                networkCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    NetworkInfos.NETWORK_TYPE_WIFI
                }
                networkCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    getNetworkCellular(context)
                }
                networkCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    NetworkInfos.NETWORK_TYPE_WIRED
                }
                // In some phone(not 3g), an exception is handled when transport is normally imported in case of vpn.
                // android 11 normal case
                // https://stackoverflow.com/questions/61480674/establishing-vpn-connection-sets-networkcapabilities-transport-wifi-to-false
                else -> {
                    getWhenNotRetrievedNetworkInfo(context)
                }
            }

        networkType.apply { isVpn = networkCap.hasTransport(NetworkCapabilities.TRANSPORT_VPN) }
        return networkType
    }

    @SuppressLint("MissingPermission")
    private fun getNetworkSubType(context: Context): String {
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        try {
            when (telephonyManager.dataNetworkType) {
                NETWORK_TYPE_EDGE,
                NETWORK_TYPE_GPRS,
                NETWORK_TYPE_CDMA,
                NETWORK_TYPE_IDEN,
                NETWORK_TYPE_1xRTT,
                ->
                    return "2G"
                NETWORK_TYPE_UMTS,
                NETWORK_TYPE_HSDPA,
                NETWORK_TYPE_HSPA,
                NETWORK_TYPE_HSPAP,
                NETWORK_TYPE_EVDO_0,
                NETWORK_TYPE_EVDO_A,
                NETWORK_TYPE_EVDO_B,
                ->
                    return "3G"
                NETWORK_TYPE_LTE ->
                    return "4G"
                NETWORK_TYPE_NR ->
                    return "5G"

                else -> return "unknown"
            }
        } catch (e: SecurityException) {
            // not used
        }
        return "unknown"
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
