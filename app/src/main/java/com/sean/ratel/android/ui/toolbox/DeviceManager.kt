package com.sean.ratel.android.ui.toolbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.home.setting.SettingViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun PhoneAppList(
    mainViewModel: MainViewModel,
    viewModel: SettingViewModel?,
) {
    Column(
        Modifier
            .wrapContentSize()
            .background(APP_BACKGROUND),
    ) {
        PhoneManagerTitle()
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            // 테두리도 살짝 투명도를 주어 번지는 배경과 연결
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            ToolBox.entries.forEach { toolBox ->
                PhoneManagerItem(mainViewModel, viewModel, toolBox)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PhoneManagerTitle() {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(APP_BACKGROUND)
            .alpha(0.9f)
            .padding(20.dp),
    ) {
        Text(
            stringResource(R.string.device_manager_title),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.White,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PhoneManagerItem(
    mainViewModel: MainViewModel,
    viewModel: SettingViewModel?,
    item: ToolBox,
) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
    ) {
        Row(
            Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .wrapContentSize()
                .clickable(onClick = { runDetailPage(item, mainViewModel, viewModel) }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                // 이미지 리소스
                painter = painterResource(id = item.icon),
                contentDescription = "App List",
                modifier =
                    Modifier
                        .height(32.dp)
                        .width(32.dp),
            )

            Column(Modifier.padding(start = 10.dp)) {
                Text(
                    stringResource(item.mainTitle),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier =
                    Modifier,
                )
                Text(
                    stringResource(item.description),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = APP_SUBTITLE_TEXT_COLOR,
                    modifier =
                        Modifier
                            .padding(top = 2.dp),
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 18.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.End,
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surfaceDim,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

fun runDetailPage(
    toolBox: ToolBox,
    mainViewModel: MainViewModel,
    viewModel: SettingViewModel?,
) {
    when (toolBox.icon) {
        R.drawable.ic_app_list -> {
            mainViewModel.setInterstitialAdStart(Destination.AppManager.route, true)
            viewModel?.runAppManagerDetail()
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.AppManager.route,
            )
        }

        else -> {
            Unit
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun PhoneAppPreView() {
    RatelappTheme {
        PhoneAppList(hiltViewModel(), hiltViewModel())
    }
}
