package com.sean.ratel.android.ui.end

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.watch.LimitMode
import com.sean.ratel.android.ui.cast.YouTubePlayersManager
import com.sean.ratel.android.ui.my.WatchLimitViewModel
import com.sean.ratel.android.ui.my.formatWatchTime
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.player.core.data.domain.model.youtube.YouTubeStreamPlaybackState

/**
 * 재생 화면에서 하루 시청 한도에 닿았을 때 "계속 볼까요?"를 묻는다. 뜨는 순간 재생을 멈춘다.
 * - 계속 보기: 이어서 재생하고, 다음 질문은 10분(영상 한도는 5개) 뒤에 한다.
 * - 그만 보기: 재생 화면에서 나간다. 다시 들어오면 한도를 넘은 상태라 또 묻는다.
 * 한도를 켜지 않았으면(기본) 아무것도 그리지 않는다.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WatchLimitPrompt(
    mainViewModel: MainViewModel,
    youTubePlayersManager: YouTubePlayersManager,
) {
    val viewModel: WatchLimitViewModel = hiltViewModel()
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    val current = prompt

    // 대화상자가 떠 있는 동안은 재생이 시작될 때마다 다시 멈춘다. 한도를 넘긴 채 재생 화면에 들어오면 대화상자가 영상보다
    // 먼저 뜨는데, 한 번만 멈추면 그 뒤에 시작되는 자동 재생을 막지 못한다(멈춘 동안은 시청 시간도 쌓이지 않는다).
    val playState by youTubePlayersManager.localPlayState.collectAsStateWithLifecycle()
    LaunchedEffect(current != null, playState) {
        if (current != null && playState == YouTubeStreamPlaybackState.Playing) youTubePlayersManager.pauseCurrent()
    }
    // "계속 보기"는 대화상자가 완전히 사라진 뒤에 재생을 재개한다. 바로 재생하면 아직 남아 있는 대화상자 때문에 위에서 다시 멈춘다.
    var resumeRequested by remember { mutableStateOf(false) }
    LaunchedEffect(current == null, resumeRequested) {
        if (current == null && resumeRequested) {
            youTubePlayersManager.resumeCurrent()
            resumeRequested = false
        }
    }
    // 한도 안내가 뜰 때마다 한 번 GA 로 보낸다
    LaunchedEffect(current != null) { if (current != null) viewModel.trackPromptShown() }
    if (current == null) return

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFDDDDDD),
        title = {
            Text(
                text =
                    if (current.mode == LimitMode.TIME) {
                        stringResource(R.string.limit_prompt_time_title, formatWatchTime(current.seconds))
                    } else {
                        stringResource(R.string.limit_prompt_videos_title, current.videos)
                    },
                fontWeight = FontWeight.Bold,
            )
        },
        text = { Text(stringResource(R.string.limit_prompt_body)) },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.continueWatching()
                    resumeRequested = true
                },
            ) { Text(stringResource(R.string.limit_prompt_continue), color = APP_TEXT_COLOR, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.trackStop()
                    mainViewModel.runNavigationBack()
                },
            ) {
                Text(stringResource(R.string.limit_prompt_stop), color = Color.White)
            }
        },
    )
}
