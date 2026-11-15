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
    NotInterested(
        R.drawable.ic_not_interested,
        R.drawable.ic_not_interested,
        R.string.end_not_interested,
    ),

    // 보관함에 저장(폴더 고르기). 어느 사용자 폴더에든 담겨 있으면 채워진 아이콘
    Save(
        R.drawable.ic_save_selected,
        R.drawable.ic_save_normal,
        R.string.end_save,
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
    SPEED(
        R.drawable.ic_speed,
        R.drawable.ic_speed,
        R.string.end_speed,
    ),
    CAPTION(
        R.drawable.ic_caption_enabled,
        R.drawable.ic_caption_disabled,
        R.string.setting_play_caption,
    ),
}
