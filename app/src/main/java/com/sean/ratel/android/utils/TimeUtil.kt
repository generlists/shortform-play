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
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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
        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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

    enum class RelativeLang {
        KO,
        EN,
        TH,
        JA,
        ZH_TW,
        FR,
        ID,
    }

    fun String.toRelativeLangByCountry(): RelativeLang =
        when (this.trim().uppercase()) {
            "KR", "KOR" -> RelativeLang.KO
            "US", "EN" -> RelativeLang.EN
            "TH", "THA" -> RelativeLang.TH
            "JP", "JPN" -> RelativeLang.JA
            "TW", "TWN" -> RelativeLang.ZH_TW
            "FR", "FRA" -> RelativeLang.FR
            "ID", "IDN" -> RelativeLang.ID
            else -> RelativeLang.EN
        }

    fun formatRelativeDate(
        createdAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        lang: RelativeLang = RelativeLang.KO,
    ): String {
        val diffMillis = (nowMillis - createdAtMillis).coerceAtLeast(0L)

        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour
        val week = 7 * day
        val month = 30 * day
        val year = 365 * day

        val value: Long
        val unit: RelativeUnit

        when {
            diffMillis < minute -> {
                return when (lang) {
                    RelativeLang.KO -> "지금"
                    RelativeLang.EN -> "Just now"
                    RelativeLang.TH -> "เมื่อสักครู่"
                    RelativeLang.JA -> "たった今"
                    RelativeLang.ZH_TW -> "剛剛"
                    RelativeLang.FR -> "À l’instant"
                    RelativeLang.ID -> "Baru saja"
                }
            }

            diffMillis < hour -> {
                value = diffMillis / minute
                unit = RelativeUnit.MINUTE
            }

            diffMillis < day -> {
                value = diffMillis / hour
                unit = RelativeUnit.HOUR
            }

            diffMillis < 2 * day -> {
                return when (lang) {
                    RelativeLang.KO -> "어제"
                    RelativeLang.EN -> "Yesterday"
                    RelativeLang.TH -> "เมื่อวาน"
                    RelativeLang.JA -> "昨日"
                    RelativeLang.ZH_TW -> "昨天"
                    RelativeLang.FR -> "Hier"
                    RelativeLang.ID -> "Kemarin"
                }
            }

            diffMillis < week -> {
                value = diffMillis / day
                unit = RelativeUnit.DAY
            }

            diffMillis < month -> {
                value = diffMillis / week
                unit = RelativeUnit.WEEK
            }

            diffMillis < year -> {
                value = diffMillis / month
                unit = RelativeUnit.MONTH
            }

            else -> {
                value = diffMillis / year
                unit = RelativeUnit.YEAR
            }
        }

        return formatRelativeValue(value, unit, lang)
    }

    private enum class RelativeUnit {
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR,
    }

    private fun formatRelativeValue(
        value: Long,
        unit: RelativeUnit,
        lang: RelativeLang,
    ): String =
        when (lang) {
            RelativeLang.KO -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> "분"
                        RelativeUnit.HOUR -> "시간"
                        RelativeUnit.DAY -> "일"
                        RelativeUnit.WEEK -> "주"
                        RelativeUnit.MONTH -> "개월"
                        RelativeUnit.YEAR -> "년"
                    }
                "${value}$text 전"
            }

            RelativeLang.EN -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> if (value == 1L) "minute" else "minutes"
                        RelativeUnit.HOUR -> if (value == 1L) "hour" else "hours"
                        RelativeUnit.DAY -> if (value == 1L) "day" else "days"
                        RelativeUnit.WEEK -> if (value == 1L) "week" else "weeks"
                        RelativeUnit.MONTH -> if (value == 1L) "month" else "months"
                        RelativeUnit.YEAR -> if (value == 1L) "year" else "years"
                    }
                "$value $text ago"
            }

            RelativeLang.TH -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> "นาที"
                        RelativeUnit.HOUR -> "ชั่วโมง"
                        RelativeUnit.DAY -> "วัน"
                        RelativeUnit.WEEK -> "สัปดาห์"
                        RelativeUnit.MONTH -> "เดือน"
                        RelativeUnit.YEAR -> "ปี"
                    }
                "$value ${text}ที่แล้ว"
            }

            RelativeLang.JA -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> "分"
                        RelativeUnit.HOUR -> "時間"
                        RelativeUnit.DAY -> "日"
                        RelativeUnit.WEEK -> "週間"
                        RelativeUnit.MONTH -> "か月"
                        RelativeUnit.YEAR -> "年"
                    }
                "${value}${text}前"
            }

            RelativeLang.ZH_TW -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> "分鐘"
                        RelativeUnit.HOUR -> "小時"
                        RelativeUnit.DAY -> "天"
                        RelativeUnit.WEEK -> "週"
                        RelativeUnit.MONTH -> "個月"
                        RelativeUnit.YEAR -> "年"
                    }
                "$value ${text}前"
            }

            RelativeLang.FR -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> if (value == 1L) "minute" else "minutes"
                        RelativeUnit.HOUR -> if (value == 1L) "heure" else "heures"
                        RelativeUnit.DAY -> if (value == 1L) "jour" else "jours"
                        RelativeUnit.WEEK -> if (value == 1L) "semaine" else "semaines"
                        RelativeUnit.MONTH -> "mois"
                        RelativeUnit.YEAR -> if (value == 1L) "an" else "ans"
                    }
                "Il y a $value $text"
            }

            RelativeLang.ID -> {
                val text =
                    when (unit) {
                        RelativeUnit.MINUTE -> "menit"
                        RelativeUnit.HOUR -> "jam"
                        RelativeUnit.DAY -> "hari"
                        RelativeUnit.WEEK -> "minggu"
                        RelativeUnit.MONTH -> "bulan"
                        RelativeUnit.YEAR -> "tahun"
                    }
                "$value $text yang lalu"
            }
        }
}
