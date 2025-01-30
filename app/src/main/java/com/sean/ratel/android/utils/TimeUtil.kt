package com.sean.ratel.android.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

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

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp * 1000) // Convert to milliseconds
        val dateFormat = SimpleDateFormat("yy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
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

    fun parseYouTubeDuration(duration: String?): Duration? {
        duration?.let {
            val pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            val matcher = pattern.matcher(duration)

            var hours = 0L
            var minutes = 0L
            var seconds = 0L

            if (matcher.matches()) {
                if (matcher.group(1) != null) {
                    hours = matcher.group(1)?.toLong() ?: 0L
                }
                if (matcher.group(2) != null) {
                    minutes = matcher.group(2)?.toLong() ?: 0L
                }
                if (matcher.group(3) != null) {
                    seconds = matcher.group(3)?.toLong() ?: 0L
                }
            }

            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds)
        } ?: run {
            return null
        }
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
}
