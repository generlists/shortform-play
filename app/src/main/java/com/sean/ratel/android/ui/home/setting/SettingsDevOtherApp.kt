package com.sean.ratel.android.ui.home.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sean.ratel.android.R
import com.sean.ratel.android.data.common.STRINGS.URL_GOOGLE_PLAY_APP
import com.sean.ratel.android.data.common.STRINGS.URL_MY_OTHER_PACKAGE_NAME
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.Background_op_10
import com.sean.ratel.android.ui.theme.RatelappTheme
import com.sean.ratel.android.utils.PhoneUtil

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingsDevOtherApp(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        val context = LocalContext.current
        Title()
        DevOtherAppSection()
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun DevOtherAppSection() {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        SettingGridRow()
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun SettingGridRow(
    gridList: List<SettingGridItem> =
        listOf(
            SettingGridItem(
                R.drawable.ic_scrap_pro,
                stringResource(R.string.setting_dev_other_app_name),
            ),
        ),
) {
    val context = LocalContext.current
    // val  statisticArgs = LocalStatisticsArgs.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outlineVariant)
                .heightIn(max = 1000.dp),
    ) {
        items(gridList.size) { index ->
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable {
                        PhoneUtil.runAppStore(
                            context,
                            URL_GOOGLE_PLAY_APP(
                                URL_MY_OTHER_PACKAGE_NAME,
                            ),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(5.dp),
                ) {
                    Box(modifier = Modifier.wrapContentSize()) {
                        Box(
                            modifier =
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painterResource(gridList[index].icon),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .height(48.dp)
                                        .width(48.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = gridList[index].appName,
                        color = Color.White,
                        modifier = Modifier.wrapContentSize(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

data class SettingGridItem(
    val icon: Int,
    val appName: String,
)

@Suppress("ktlint:standard:function-naming")
@Composable
private fun Title() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Background_op_10)
            .alpha(0.9f)
            .padding(20.dp),
    ) {
        Text(
            stringResource(R.string.setting_dev_other_app),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.White,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SettingViewPreView() {
    RatelappTheme {
        // SettingsApp()
    }
}
