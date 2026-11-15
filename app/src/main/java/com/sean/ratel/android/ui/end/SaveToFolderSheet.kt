package com.sean.ratel.android.ui.end

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.R
import com.sean.ratel.android.data.dto.MainShortsModel
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.my.FolderNameDialog
import com.sean.ratel.android.ui.my.LibraryMessageEffect
import com.sean.ratel.android.ui.my.LibraryViewModel
import com.sean.ratel.android.ui.my.folderDisplayName
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.APP_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg

/**
 * 재생 화면의 "저장" — 이 영상을 담을 폴더를 고른다. 줄을 누르면 바로 담거나 뺀다(여러 폴더에 담을 수 있다).
 * "새 폴더"를 만들면 그 폴더에 바로 담는다. 로그인하지 않아도 쓸 수 있고, 로그인하면 서버와 맞춰진다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun SaveToFolderSheet(
    video: MainShortsModel?,
    libraryViewModel: LibraryViewModel,
    onDismiss: () -> Unit,
) {
    val folders by libraryViewModel.folders.collectAsStateWithLifecycle()
    val videoId = video?.shortsVideoModel?.videoId.orEmpty()
    val savedIds by libraryViewModel.savedFolderIds(videoId).collectAsStateWithLifecycle(initialValue = emptySet())
    var creating by remember { mutableStateOf(false) }

    LibraryMessageEffect(libraryViewModel)
    LaunchedEffect(Unit) { libraryViewModel.track(GAAccountAnalytics.Event.LIBRARY_SAVE_SHEET_OPEN, "all") }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = APP_BACKGROUND,
        contentColor = Color.White,
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.library_save_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { creating = true }) {
                    Text(
                        stringResource(R.string.library_new_folder),
                        color = APP_TEXT_COLOR,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(folders, key = { it.folder.id }) { summary ->
                    val saved = summary.folder.id in savedIds
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable { libraryViewModel.toggle(summary.folder.id, video) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.size(width = 36.dp, height = 44.dp).clip(RoundedCornerShape(8.dp)).background(BarBg)) {
                            summary.covers.firstOrNull()?.let {
                                NetworkImage(url = it, contentDescription = null, modifier = Modifier.size(width = 36.dp, height = 44.dp))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(folderDisplayName(summary.folder), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.library_count, summary.count), color = APP_SUBTITLE_TEXT_COLOR, fontSize = 12.sp)
                        }
                        Icon(
                            painter = painterResource(if (saved) R.drawable.ic_save_selected else R.drawable.ic_save_normal),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }

    if (creating) {
        FolderNameDialog(
            title = stringResource(R.string.library_name_new_title),
            initial = "",
            onConfirm = { name ->
                creating = false
                // 새 폴더를 만들면 이 영상을 바로 담는다
                libraryViewModel.createFolder(name) { folder -> libraryViewModel.toggle(folder.id, video) }
            },
            onDismiss = { creating = false },
        )
    }
}
