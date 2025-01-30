package com.sean.player.utils.log

import android.os.Looper
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Class to send log via udp
 */
internal class RUdpLogger(val ip: String, val deviceId: String) {

    private val executor = Executors.newSingleThreadExecutor()
    private val socket = DatagramSocket()
    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS",
        Locale.US
    )

    fun send(msg: String) {
        val currentTime = System.currentTimeMillis()
        val tid = if (Looper.getMainLooper().isCurrentThread) {
            "MAIN"
        } else {
            Thread.currentThread().id.toString()
        }
        executor.submit {
            val tag = "|$deviceId|"
            val message = String.format(
                MESSAGE_FORMAT,
                convertToDate(currentTime),
                tid,
                msg
            )

            runCatching {
                val msgByteArray = (tag + message).toByteArray()
                val request =
                    DatagramPacket(
                        msgByteArray,
                        msgByteArray.size,
                        InetAddress.getByName(ip),
                        PORT
                    )
                socket.send(request)
            }
        }
    }

    private fun convertToDate(milliseconds: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = milliseconds
        return dateFormat.format(calendar.time)
    }

    fun isClosed() = socket.isClosed

    fun close() {
        socket.close()
        executor.shutdown()
    }

    companion object {
        private const val MESSAGE_FORMAT = "%s [%s] %s"
        private const val PORT = 12500
    }
}
