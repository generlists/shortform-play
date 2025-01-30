package com.sean.ratel.android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS
import com.sean.ratel.android.data.common.STRINGS.MY_EMAIL_ACCOUNT
import com.sean.ratel.core.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.ceil

object PhoneUtil {
    private const val DIV = 1024.0

    fun bytesToMegabytes(bytes: Long): Double = roundUpTwoDecimalPlaces(bytes / (DIV * DIV))

    // 소숫점 두자리 올림
    private fun roundUpTwoDecimalPlaces(value: Double): Double = ceil(value * 100) / 100

    fun readJsonFromRaw(
        context: Context,
        resourceId: Int,
    ): String {
        val inputStream = context.resources.openRawResource(resourceId)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }

    fun shareAppLinkButton(context: Context) {
        val messageToShare = "${context.getString(R.string.short_form_app_share)} ${STRINGS.URLUPDATE_GOOGLE_PLAY_WEB(context.packageName)}"
        val sendIntent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, messageToShare)
                type = "text/plain"
            }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    // 버전 이름 가져오기 (versionName)
    fun getAppVersionName(context: Context): String =
        try {
            val packageInfo = context.packageManager.getPackageInfo(STRINGS.URL_MY_PACKAGE_NAME, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }

    fun getEnvironment(): String = "Model : ${Build.MODEL}\nOS Version : ${Build.VERSION.RELEASE}\nAPI Level : ${Build.VERSION.SDK_INT}"

    fun openAppSettings(context: Context) {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        context.startActivity(intent)
    }

    fun sendEmail(
        context: Context,
        subject: String,
        body: String,
    ) {
        val intent =
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$MY_EMAIL_ACCOUNT") // 이메일만 처리하는 인텐트
                putExtra(Intent.EXTRA_SUBJECT, subject) // 이메일 제목
                putExtra(Intent.EXTRA_TEXT, body) // 이메일 본문
            }

        // 이메일 클라이언트가 있는지 확인
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.setting_app_email_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun goAppSettingsOpenSourceLicense(context: Context) {
        if (BuildConfig.DEBUG) {
            Toast
                .makeText(
                    context,
                    R.string.setting_app_opensource_not_produce,
                    Toast.LENGTH_SHORT,
                ).show()
        } else {
            val intent = Intent(context, OssLicensesMenuActivity::class.java)
            OssLicensesMenuActivity.setActivityTitle(context.getString(R.string.setting_app_openSource))
            (context as Activity).startActivity(intent)
        }
    }

    fun openBrowsere(
        context: Context,
        url: String,
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        // Chrome 패키지로 설정
        intent.setPackage("com.android.chrome")

        // Chrome이 설치되어 있는지 확인
        if (intent.resolveActivity(context.packageManager) != null) {
            // Chrome이 설치된 경우 Chrome으로 URL 열기
            context.startActivity(intent)
        } else {
            // Chrome이 설치되어 있지 않은 경우, Chooser로 다른 브라우저 선택
            val chooserIntent =
                Intent.createChooser(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                    context.getString(R.string.device_browser_select),
                )
            context.startActivity(chooserIntent)
        }
    }

    fun getStatusBarHeight(context: Context): Int {
        // 시스템에서 상태바의 높이를 나타내는 리소스 ID를 가져옵니다
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            // 리소스 ID를 통해 상태바 높이를 픽셀 단위로 가져옵니다
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    fun goYoutubeApp(
        context: Context,
        param: String,
    ) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(param))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
