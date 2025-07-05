package com.sean.ratel.android.ui.toolbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.End
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
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.sean.ratel.android.R
import com.sean.ratel.android.ui.home.setting.SettingViewModel
import com.sean.ratel.android.ui.navigation.Destination
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.RatelappTheme

@Suppress("ktlint:standard:function-naming")
@Composable
fun PhoneAppList(viewModel: SettingViewModel?) {
    Column(
        Modifier
            .wrapContentSize()
            .background(APP_BACKGROUND),
    ) {
        PhoneManagerTitle()
        ToolBox.entries.forEach { toolBox ->
            PhoneManagerItem(viewModel, toolBox)
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
            .padding(start = 5.dp),
    ) {
        Text(
            stringResource(R.string.device_manager_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            color = Color.White,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PhoneManagerItem(
    viewModel: SettingViewModel?,
    item: ToolBox,
) {
    Column(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(APP_BACKGROUND),
    ) {
        Spacer(Modifier.height(5.dp))

        Row(
            Modifier
                .wrapContentSize()
                .background(APP_BACKGROUND)
                .clickable(onClick = { runDetailPage(item, viewModel) }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                // 이미지 리소스
                painter = painterResource(id = item.icon),
                contentDescription = "App List",
                modifier =
                    Modifier
                        .padding(start = 5.dp)
                        .height(32.dp)
                        .width(32.dp),
            )

            Column(Modifier.padding(start = 5.dp, top = 5.dp)) {
                Text(
                    stringResource(item.mainTitle),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color.White,
                    modifier =
                        Modifier
                            .padding(top = 2.dp),
                )
                Text(
                    stringResource(item.description),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = APP_SUBTITLE_TEXT_COLOR,
                    modifier =
                        Modifier
                            .padding(top = 2.dp, bottom = 10.dp),
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(end = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    // 이미지 리소스
                    painter = painterResource(id = R.drawable.ic_link_index),
                    contentDescription = "Link",
                    modifier =
                        Modifier
                            .height(32.dp)
                            .width(32.dp)
                            .align(End),
                )
            }
        }
        Spacer(Modifier.height(5.dp))
    }
}

fun runDetailPage(
    toolBox: ToolBox,
    viewModel: SettingViewModel?,
) {
    when (toolBox.icon) {
        R.drawable.ic_app_list -> {
            viewModel?.runAppManagerDetail()
            viewModel?.sendGALog(
                Event.SCREEN_VIEW,
                Destination.AppManager.route,
            )
        }

//        R.drawable.ic_network_manager -> {
//            Toast.makeText(context, R.string.device_manager_comming_soon, Toast.LENGTH_LONG).show()
//            viewModel?.sendGALog(
//                Event.SCREEN_VIEW,
//                Destination.SettingDeviceNetwork.route,
//            )
//        }

//        R.drawable.ic_phone_care -> {
//            Toast.makeText(context, R.string.device_manager_comming_soon, Toast.LENGTH_LONG).show()
//            viewModel?.sendGALog(
//                Event.SCREEN_VIEW,
//                Destination.SettingPhoneCare.route,
//            )
//        }

        else -> Unit
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun PhoneAppPreView() {
    RatelappTheme {
        PhoneAppList(null)
    }
}
