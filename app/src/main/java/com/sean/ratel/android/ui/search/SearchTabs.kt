package com.sean.ratel.android.ui.search

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.YouTubeCategory
import com.sean.ratel.android.ui.navigation.Destination

@Keep
enum class SearchTabs(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String,
    val destRoute: String,
) {
    SHORTS(
        R.string.search_type_keyword,
        R.drawable.ic_video_search,
        Destination.Home.ShortForm.route,
        Destination.Home.ShortForm.route,
    ),
    ARCHIVE(
        R.string.search_type_daily,
        R.drawable.ic_achive_search,
        Destination.Search.route,
        Destination.Search.route,
    ),
}

enum class SearchType {
    VideoSearch,
    ArchiveSearch,
}

data class SearchFilterActions(
    val changeCategory: (YouTubeCategory) -> Unit,
    val resetCategory: (Boolean) -> Unit,
    val onDismiss: () -> Unit,
    val onApply: (Long?, YouTubeCategory) -> Unit,
)
