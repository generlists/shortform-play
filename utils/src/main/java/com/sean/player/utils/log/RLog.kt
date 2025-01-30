package com.sean.player.utils.log

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.sean.player.utils.BuildConfig
import java.security.MessageDigest

/**
 * Class to print log for TVING Player
 */
object RLog {
    private const val TAG = "RLog"
    private var enableLog = if(BuildConfig.DEBUG) true else false
    private var showLogWithLinkToSource = false

    private var udpLogger: RUdpLogger? = null

    fun init(
        context: Context,
        enableAllLogger: Boolean,
        enableShowLogWithLinkToSource: Boolean,
        enableUdpLogger: Boolean,
        ipAddress: String? = null
    ) {
        enableLog = enableAllLogger
        showLogWithLinkToSource = enableShowLogWithLinkToSource
        if (!enableLog) {
            return
        }

        // get ip from settings
        val ip = if (enableUdpLogger) {
            ipAddress
        } else {
            null
        }

        // release when ip is not equal
        if (udpLogger?.ip != ip) {
            release()
        }

        // create only udpLogger == null && ip != null
        val logger = udpLogger ?: ip?.let {
            RUdpLogger(
                ip,
                "TVING_%s_%s - %s".format(
                    BuildConfig.DEBUG,
                    Build.MODEL,
                    getHashedAndroidId(context)
                )
            )
        }
        udpLogger = logger
    }

    fun d(tag: String, msg: String) = print(LogLevel.D, tag, msg)

    fun d(tag: String, msg: String, throwable: Throwable?) =
        throwable?.let { d(tag, "$msg\n${formatThrowable(throwable)}") } ?: d(tag, msg)

    fun w(tag: String, msg: String) = print(LogLevel.W, tag, msg)

    fun w(tag: String, msg: String, throwable: Throwable?) =
        throwable?.let { w(tag, "$msg\n${formatThrowable(throwable)}") } ?: w(tag, msg)

    fun e(tag: String, msg: String) = print(LogLevel.E, tag, msg)

    fun e(tag: String, msg: String, throwable: Throwable?) =
        throwable?.let { e(tag, "$msg\n${formatThrowable(throwable)}") } ?: e(tag, msg)

    fun i(tag: String, msg: String) = print(LogLevel.I, tag, msg)

    fun i(tag: String, msg: String, throwable: Throwable?) =
        throwable?.let { i(tag, "$msg\n${formatThrowable(throwable)}") } ?: i(tag, msg)

    fun v(tag: String, msg: String) = print(LogLevel.V, tag, msg)

    fun v(tag: String, msg: String, throwable: Throwable?) =
        throwable?.let { v(tag, "$msg\n${formatThrowable(throwable)}") } ?: v(tag, msg)

    private fun print(level: LogLevel, tag: String, msg: String) {
        if (!enableLog) return

        val msgWithTag = "[$tag] $msg"
        val str = makeLinkMessage(msgWithTag)
        when (level) {
            LogLevel.D -> Log.d(TAG, str)
            LogLevel.I -> Log.i(TAG, str)
            LogLevel.W -> Log.w(TAG, str)
            LogLevel.E -> Log.e(TAG, str)
            LogLevel.V -> Log.v(TAG, str)
        }
        udpLogger?.send("[$level][$TAG] $msgWithTag")
    }

    @SuppressLint("HardwareIds")
    private fun getHashedAndroidId(context: Context): String {
        runCatching {
            val androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val hashedId =
                MessageDigest.getInstance("MD5").digest(androidId.toByteArray())
            val hexString = toHexString(hashedId)
            return if (hexString.length > 4) {
                hexString.substring(0, 4).uppercase()
            } else {
                hexString.uppercase()
            }
        }
        return ""
    }

    private fun toHexString(src: ByteArray): String {
        val sb = StringBuffer(src.size * 2)
        for (byte in src) {
            sb.append(String.format("%02X", byte))
        }
        return sb.toString()
    }

    private fun formatThrowable(throwable: Throwable): String {
        val builder = StringBuilder()
        builder.append("${throwable::class.java.simpleName}, msg = ${throwable.message}\n")
        throwable.stackTrace.forEach {
            builder.append(
                "  at " +
                        "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})\n"
            )
        }
        return builder.toString()
    }

    private fun release() {
        udpLogger?.close()
        udpLogger = null
    }

    fun String.withCallStack(depth: Int = 3): String = runCatching {
        val callStack = Throwable().stackTrace
        var maxOutputLines = depth
        return StringBuilder(this).apply {
            for (i in 2..callStack.size) {
                if (i < 2) { // 0 is current, 1 is just caller function
                    continue
                }

                val item = callStack[i]
                append("\n  ")
                append("${item.className}.${item.methodName}(${item.fileName}:${item.lineNumber})")

                if (--maxOutputLines <= 0) {
                    append("\n  ...(${callStack.size - i} lines)")
                    break
                }
            }
        }.toString()
    }.getOrDefault("")

    private fun makeLinkMessage(message: String): String {
        if (!showLogWithLinkToSource) return message
        val stackTrace = Thread.currentThread().stackTrace[5]
        val sb = java.lang.StringBuilder()
        sb.append(stackTrace.methodName)
        sb.append("(")
        sb.append(stackTrace.fileName)
        sb.append(":")
        sb.append(stackTrace.lineNumber)
        sb.append("): ")
        sb.append(message)
        return sb.toString()
    }

    /**
     * Level for log
     */
    private enum class LogLevel {
        D, I, W, E, V
    }
}