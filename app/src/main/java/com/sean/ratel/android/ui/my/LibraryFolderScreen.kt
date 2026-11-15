package com.sean.ratel.android.ui.my

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sean.ratel.android.MainViewModel
import com.sean.ratel.android.R
import com.sean.ratel.android.data.library.FolderKind
import com.sean.ratel.android.data.library.LibraryItem
import com.sean.ratel.android.data.library.toMainShortsModel
import com.sean.ratel.android.data.log.GAAccountAnalytics
import com.sean.ratel.android.ui.common.image.NetworkImage
import com.sean.ratel.android.ui.theme.APP_BACKGROUND
import com.sean.ratel.android.ui.theme.APP_SUBTITLE_TEXT_COLOR
import com.sean.ratel.android.ui.theme.BarBg

/**
 * 폴더 안의 영상 목록. 영상을 누르면 그 영상부터 폴더의 목록을 이어서 재생하고, 사용자 폴더는 이름 바꾸기·삭제를 할 수 있다.
 * 폴더가 지워졌으면(다른 기기에서 지운 경우 등) 이전 화면으로 돌아간다.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LibraryFolderScreen(
    folderId: String,
    libraryViewModel: LibraryViewModel,
    mainViewModel: MainViewModel,
) {
    val folder by libraryViewModel.folder(folderId).collectAsStateWithLifecycle(initialValue = null)
    val items by libraryViewModel.items(folderId).collectAsStateWithLifecycle(initialValue = emptyList())
    var menuOpen by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    LibraryMessageEffect(libraryViewModel)
    LaunchedEffect(folderId) { libraryViewModel.track(GAAccountAnalytics.Event.LIBRARY_FOLDER_OPEN, libraryViewModel.folderType(folderId)) }
    BackHandler(enabled = true) { mainViewModel.runNavigationBack() }

    val current = folder
    Column(
        Modifier
            .fillMaxSize()
            .background(APP_BACKGROUND)
            .padding(WindowInsets.statusBars.asPaddingValues()),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { mainViewModel.runNavigationBack() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.library_back), tint = Color.White)
            }
            Text(
                current?.let { folderDisplayName(it) }.orEmpty(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (current != null && current.kind == FolderKind.CUSTOM) {
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.library_menu), tint = Color.White)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = Color(0xFF1C1C1E)) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.library_menu_rename), color = Color.White) },
                            onClick = {
                                menuOpen = false
                                renaming = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.library_menu_delete), color = Color(0xFFFF6B6B)) },
                            onClick = {
                                menuOpen = false
                                deleting = true
                            },
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Text(
                stringResource(R.string.library_empty_folder),
                color = Color(0xFFC9CBD0),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            )
        } else {
            val videos = remember(items) { items.map { it.toMainShortsModel() } }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding =
                    PaddingValues(
                        start = 20.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(items, key = { _, item -> item.key }) { index, item ->
                    SavedVideoRow(
                        item = item,
                        onPlay = { mainViewModel.playLibrary(videos, index) },
                        onRemove = { libraryViewModel.remove(item.folderId, item.videoId) },
                    )
                }
            }
        }
    }

    if (renaming && current != null) {
        FolderNameDialog(
            title = stringResource(R.string.library_name_rename_title),
            initial = current.name,
            onConfirm = {
                libraryViewModel.rename(current.id, it)
                renaming = false
            },
            onDismiss = { renaming = false },
        )
    }
    if (deleting && current != null) {
        ConfirmDeleteFolderDialog(
            onConfirm = {
                deleting = false
                libraryViewModel.deleteFolder(current.id)
                mainViewModel.runNavigationBack()
            },
            onDismiss = { deleting = false },
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SavedVideoRow(
    item: LibraryItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.width(48.dp).height(72.dp).clip(RoundedCornerShape(8.dp)).background(BarBg)) {
            if (item.thumbnail.isNotBlank()) {
                NetworkImage(url = item.thumbnail, contentDescription = null, modifier = Modifier.size(width = 48.dp, height = 72.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, color = Color.White, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                listOf(item.channelTitle, item.duration).filter { it.isNotBlank() }.joinToString(" · "),
                color = APP_SUBTITLE_TEXT_COLOR,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.library_remove), tint = Color(0xFF8E9096))
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ConfirmDeleteFolderDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFDDDDDD),
        title = { Text(stringResource(R.string.library_delete_title), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.library_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.library_menu_delete), color = Color(0xFFFF6B6B)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.my_cancel), color = Color.White) }
        },
    )
}
