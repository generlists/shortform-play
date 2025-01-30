package com.sean.ratel.android.ui.home.shortform

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.navigation.Destination

@Keep
enum class ShortFormTab(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val destRoute: String,
) {
    SHORTS(
        R.string.shorts,
        R.drawable.ic_youtube_shorts_logo_white,
        Destination.Home.ShortForm.route,
        Destination.Home.ShortForm.route,
    ),
//    RILS(
//        R.string.video,
//        R.drawable.ic_facebook,
//        Destination.Home.Video.route,
//        Destination.Home.Video.route
//    ),
//    X(
//        R.string.X,
//        R.drawable.ic_twiter,
//        Destination.My.route,
//        Destination.My.route
//    ),
//    NAVER_CLIP(
//        R.string.naver_clip,
//        R.drawable.ic_play_icon,
//        Destination.My.route,
//        Destination.My.route
//    ),
//    DAUM_SHORTS(
//        R.string.daum_shorts,
//        R.drawable.ic_play_icon,
//        Destination.My.route,
//        Destination.My.route
//    )
}
