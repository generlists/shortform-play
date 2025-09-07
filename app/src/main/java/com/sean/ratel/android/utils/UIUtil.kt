package com.sean.ratel.android.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdSize
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.data.common.RemoteConfig
import com.sean.ratel.android.data.common.RemoteConfig.MAIN_AD_KEY
import com.sean.ratel.android.data.common.RemoteConfig.MAIN_SHORTFORM_KEY
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.dto.MainShortsResponse
import com.sean.ratel.android.ui.end.YouTubeContentEndViewModel
import com.sean.ratel.android.ui.end.YouTubeEndFragment
import com.sean.ratel.android.ui.navigation.Destination
import java.text.DecimalFormat
import java.util.Locale

object UIUtil {
    fun adSize(context: Context): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics =
                    context.getSystemService(WindowManager::class.java)?.currentWindowMetrics
                windowMetrics?.bounds?.width() ?: 0
            } else {
                displayMetrics.widthPixels
            }
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    fun adInLineAdaptiveBannerSize(
        context: Context,
        maxHeight: Int,
    ): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics =
                    context.getSystemService(WindowManager::class.java)?.currentWindowMetrics
                windowMetrics?.bounds?.width() ?: 0
            } else {
                displayMetrics.widthPixels
            }
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getInlineAdaptiveBannerAdSize(adWidth, maxHeight)
    }

    fun validationIndex(
        route: String,
        itemSize: Int,
    ): List<Int> {
        if (route == Destination.Home.Main.route) {
            return listOf(RemoteConfig.getRemoteConfigIntValue(MAIN_AD_KEY)).filter { itemSize > it }
        } else {
            return listOf(RemoteConfig.getRemoteConfigIntValue(MAIN_SHORTFORM_KEY)).filter { itemSize > it }
        }
    }

    fun findFragment(
        context: Context,
        position: Int,
    ): YouTubeEndFragment? {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        val fragmentTag = "f$position"
        val fragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (fragment is YouTubeEndFragment) return fragment
        return null
    }

    fun findCurrentFragment(
        context: Context,
        position: Int,
    ) {
        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        val fragmentTag = "f$position"
        val fragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (fragment is YouTubeEndFragment) fragment.updateSelectPosition()
    }

    fun findCurrentFragment(
        context: Context,
        position: Int,
        pageScrollState: YouTubeContentEndViewModel.PageScrollState,
    ) {
        RLog.d("", "$context ,  $position , $pageScrollState")
    }

    fun formatNumberByLocale(
        number: Long,
        locale: Locale = Locale.getDefault(),
    ): String =
        if (locale.language == "ko") {
            // 한국어일 때: 천, 백만, 십억 단위로 표시
            when {
                number < 1000 -> number.toString() // 1,000 미만
                number in 1000..999999 ->
                    String.format(
                        locale,
                        "%.1f천",
                        number / 1000.0,
                    ) // 1,000 이상 1,000,000 미만 (천 단위)
                number in 1000000..999999999 ->
                    String.format(
                        locale,
                        "%.1f백만",
                        number / 1000000.0,
                    ) // 1,000,000 이상 1,000,000,000 미만 (백만 단위)
                number >= 1000000000 ->
                    String.format(
                        locale,
                        "%.1f십억",
                        number / 1000000000.0,
                    ) // 1,000,000,000 이상 (십억 단위)
                else -> number.toString()
            }
        } else {
            // 한국어가 아닐 때: K, M, B 단위로 표시
            when {
                number < 1000 -> number.toString() // 1,000 미만
                number in 1000..999999 ->
                    String.format(
                        locale,
                        "%.1fK",
                        number / 1000.0,
                    ) // 1,000 이상 1,000,000 미만 (K 단위)
                number in 1000000..999999999 ->
                    String.format(
                        locale,
                        "%.1fM",
                        number / 1000000.0,
                    ) // 1,000,000 이상 1,000,000,000 미만 (M 단위)
                number >= 1000000000 ->
                    String.format(
                        locale,
                        "%.1fB",
                        number / 1000000000.0,
                    ) // 1,000,000,000 이상 (B 단위)
                else -> number.toString()
            }
        }

    // MainShortsModel을 JSON 형식으로 변환하는 함수
    fun serializeToJson(mainShortsModel: MainShortsModel): String {
        // Gson 객체 생성
        val gson = GsonBuilder().create()

        // 객체를 JSON 문자열로 변환
        return gson.toJson(mainShortsModel)
    }

    // JSON 문자열을 다시 MainShortsModel로 변환하는 함수
    fun deserializeFromJson(json: String): MainShortsModel {
        val gson = GsonBuilder().create()

        // JSON 문자열을 객체로 변환
        return gson.fromJson(json, MainShortsModel::class.java)
    }

    fun mainShortsListToJson(mainShortsListResponse: MainShortsResponse): String {
        val gson = GsonBuilder().create()
        return gson.toJson(mainShortsListResponse)
    }

    // JSON 문자열을 List<MainShortsModel>로 변환
    fun jsonToObject(jsonString: String?): MainShortsResponse? =
        try {
            val gson = Gson()
            gson.fromJson(jsonString, MainShortsResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    fun pixelToDp(
        context: Context,
        px: Float,
    ): Float =
        px / (
            context.resources.displayMetrics.densityDpi
                .toFloat() / DisplayMetrics.DENSITY_DEFAULT
        )

    // 확장 함수: 픽셀 값을 dp로 변환
    fun Int.toDp(): Dp = (this / Resources.getSystem().displayMetrics.density).dp

    fun getScreenWidthDp(context: Context): Float {
        val metrics = context.resources.displayMetrics
        return metrics.widthPixels / metrics.density
    }

    fun formatNumberWithCommas(number: Long): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(number)
    }

    @JvmStatic
    internal fun Context.hasPipPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                packageName,
            ) == AppOpsManager.MODE_ALLOWED
        }
    }

    fun getCountryCode(countryCode: String? = null): String =
        countryCode
            ?: if (Locale
                    .getDefault()
                    .country
                    .toString()
                    .isNotEmpty()
            ) {
                Locale.getDefault().country
            } else {
                "KR"
            }
}
