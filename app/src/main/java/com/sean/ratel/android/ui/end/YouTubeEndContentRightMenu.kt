package com.sean.ratel.android.ui.end

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.sean.ratel.android.R

@Keep
enum class YouTubeEndContentRightMenu(
    @DrawableRes val selectedResourceId: Int,
    @DrawableRes val unSelectedResourceId: Int,
    @StringRes val title: Int? = null,
) {
    Like(
        R.drawable.ic_like_selected,
        R.drawable.ic_like_normal,
        R.string.end_lkie,
    ),
    DisLike(
        R.drawable.ic_dislike_selected,
        R.drawable.ic_dislike_normal,
        R.string.end_dislike,
    ),
    Comment(
        R.drawable.ic_comment,
        R.drawable.ic_comment,
        R.string.end_comment,
    ),
    Share(
        R.drawable.ic_share_main,
        R.drawable.ic_share,
        R.string.end_share,
    ),
    Sound(
        R.drawable.ic_volume_on,
        R.drawable.ic_volume_off,
        R.string.end_sound_on,
    ),
}
