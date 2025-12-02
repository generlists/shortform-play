package com.sean.ratel.android.utils

import android.annotation.SuppressLint
import com.sean.player.utils.log.RLog
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtil {
    fun formatMillisToDate(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date =
            Instant
                .ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        return date.format(formatter)
    }

    fun formatTimestamp(
        timestamp: Long,
        format: String,
    ): String {
        val date = Date(timestamp * 1000) // Convert to milliseconds
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    /** 날짜 포맷 */
    fun millisToDateString(millis: Long): String {
        val date =
            Instant
                .ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun formatYouTubeDuration(duration: String): String {
        // Regular expression to parse ISO 8601 duration format
        val regex = """PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""".toRegex()
        val matchResult = regex.matchEntire(duration)

        if (matchResult != null) {
            val (hours, minutes, seconds) = matchResult.destructured

            val totalHours = hours.ifEmpty { "0" }.toInt()
            val totalMinutes = minutes.ifEmpty { "0" }.toInt()
            val totalSeconds = seconds.ifEmpty { "0" }.toInt()

            return if (totalHours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds)
            } else {
                String.format(Locale.US, "%02d:%02d", totalMinutes, totalSeconds)
            }
        }

        return "00:00" // Default value if parsing fails
    }

    fun formatTimeFromFloat(floatTime: Float): String {
        // 소숫점을 올림 처리
        val totalSeconds = kotlin.math.ceil(floatTime).toInt()

        // 분과 초로 변환
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        // 00:00 형식으로 변환
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentDate.format(formatter)
    }

    @SuppressLint("SimpleDateFormat")
    fun getPreviousDate(): String {
        val myDate = SimpleDateFormat("yyyyMMdd")
        val calendar = Calendar.getInstance()
        val today = Date()
        calendar.time = today
        calendar.add(Calendar.DATE, -1) // 현재 하루 앞으로 이동했기 때문에 2일 전으로 이동하면 어제
        return myDate.format(calendar.time)
    }

    fun formatLocalizedDate(
        dateString: String,
        locale: Locale,
    ): String {
        // ISO 날짜 문자열을 LocalDateTime으로 변환
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateTime = LocalDateTime.parse(dateString, inputFormatter)

        // 로컬화된 포맷을 위한 출력 Formatter
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", locale)

        // 날짜를 포맷하고 반환
        return dateTime.format(outputFormatter)
    }

    fun timeToFloat(time: String): Float {
        val parts = time.split(":")
        val minutes = parts[0].toInt()
        val seconds = parts[1].toInt()
        return (minutes * 60 + seconds).toFloat()
    }

    fun expTimePrint(
        expTime: Long,
        currentTime: Long,
    ) {
        // Instant 변환
        val expInstant = Instant.ofEpochMilli(expTime)
        val curInstant = Instant.ofEpochMilli(currentTime)

        // 포맷터 (2025-11-05 09:15:14 형태)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // UTC 기준
        val expUtc = formatter.withZone(ZoneId.of("UTC")).format(expInstant)
        val curUtc = formatter.withZone(ZoneId.of("UTC")).format(curInstant)

        // KST 기준 (Asia/Seoul)
        val expKst = formatter.withZone(ZoneId.of("Asia/Seoul")).format(expInstant)
        val curKst = formatter.withZone(ZoneId.of("Asia/Seoul")).format(curInstant)

        RLog.d("SKTTTTTTTTT", "🕒 토큰 만료(exp) 시간 (UTC): $expUtc")
        RLog.d("SKTTTTTTTTT", "🕘 토큰 만료(exp) 시간 (KST): $expKst")
        RLog.d("SKTTTTTTTTT", "⏰ 현재 시간 (UTC): $curUtc")
        RLog.d("SKTTTTTTTTT", "⏰ 현재 시간 (KST): $curKst")
    }
}
