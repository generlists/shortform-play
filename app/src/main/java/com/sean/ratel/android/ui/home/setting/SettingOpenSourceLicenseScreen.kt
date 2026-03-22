package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.LibraryDefaults.libraryPadding
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.common.TopNavigationBar
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingOpenSourceLicensesScreen(
    modifier: Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val colors =
        libraryColors(
            backgroundColor = APP_BACKGROUND,
            contentColor = Color.White,
            badgeBackgroundColor = APP_TEXT_COLOR,
            badgeContentColor = Color.Black,
            dialogConfirmButtonColor = Color.White,
        )

    val padding =
        libraryPadding(
            namePadding = PaddingValues(top = 10.dp),
            versionPadding = PaddingValues(start = 10.dp),
            badgePadding = PaddingValues(top = 10.dp, end = 10.dp),
            badgeContentPadding = PaddingValues(10.dp),
        )

    Box(
        modifier
            .fillMaxSize(),
    ) {
        TopNavigationBar(
            titleResourceId = R.string.open_license,
            historyBack = { mainViewModel.runNavigationBack() },
            isShareButton = false,
            runSetting = {},
            filterButton = false,
            onFilterChange = {
            },
            items =
                listOf(),
        )
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Transparent),
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(APP_BACKGROUND),
            ) {
                LibrariesContainer(
                    colors = colors,
                    padding = padding,
                )
            }
        }
    }
}
