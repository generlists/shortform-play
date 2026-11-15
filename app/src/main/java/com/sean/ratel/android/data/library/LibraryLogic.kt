package com.sean.ratel.android.data.library

/**
 * 보관함 규칙 — 순수 계산이라 기기 없이 테스트한다. 모든 함수는 새 [LibraryData] 를 돌려준다.
 *
 * - 고정 폴더(좋아요·나중에 볼 영상)는 항상 있고 서버에 올리지 않는다. 그 안의 영상은 올린다.
 * - 바뀐 행은 dirty 로 표시하고, 서버와 맞추면([merge]) 올린 것만 지운다.
 * - 행마다 updatedAt 이 더 새로운 쪽이 이긴다(서버와 같다). 지운 것은 deleted 로 남긴다.
 */
object LibraryLogic {
    private val SYSTEM_IDS = setOf(LIKES_FOLDER_ID, WATCH_LATER_FOLDER_ID)

    private fun systemFolders(): List<LibraryFolder> =
        listOf(
            LibraryFolder(LIKES_FOLDER_ID, "Likes", FolderKind.LIKES, position = 0),
            LibraryFolder(WATCH_LATER_FOLDER_ID, "Watch later", FolderKind.WATCH_LATER, position = 1),
        )

    fun isSystem(folderId: String): Boolean = folderId in SYSTEM_IDS

    /** 고정 폴더가 항상 앞에 있게 한다(없으면 만든다). 서버에서 같은 id 가 와도 무시한다. */
    fun withSystemFolders(data: LibraryData): LibraryData {
        val custom = data.folders.filter { it.id !in SYSTEM_IDS }
        val folders = systemFolders() + custom
        return if (folders == data.folders) data else data.copy(folders = folders)
    }

    fun normalizeName(raw: String): String? {
        val name = raw.trim().replace(Regex("\\s+"), " ")
        if (name.isEmpty() || name.length > LibraryLimits.NAME_MAX) return null
        if (name.any { it.isISOControl() }) return null
        return name
    }

    // ---- 보기

    /** 지우지 않은 폴더: 고정 폴더 → 사용자 폴더(순서대로). */
    fun visibleFolders(data: LibraryData): List<LibraryFolder> {
        val live = data.folders.filter { !it.deleted }
        return live.filter { it.id in SYSTEM_IDS }.sortedBy { it.position } +
            live.filter { it.id !in SYSTEM_IDS }.sortedWith(compareBy({ it.position }, { it.name }))
    }

    /** 폴더 안의 영상(지운 것 제외), 최근에 담은 순서. */
    fun itemsOf(
        data: LibraryData,
        folderId: String,
    ): List<LibraryItem> = data.items.filter { it.folderId == folderId && !it.deleted }.sortedByDescending { it.savedAt }

    /** 이 영상이 담겨 있는 폴더 id 들. */
    fun savedFolderIds(
        data: LibraryData,
        videoId: String,
    ): Set<String> {
        val live = data.folders.filter { !it.deleted }.map { it.id }.toSet()
        return data.items.filter { it.videoId == videoId && !it.deleted && it.folderId in live }.map { it.folderId }.toSet()
    }

    fun summaries(data: LibraryData): List<FolderSummary> =
        visibleFolders(data).map { folder ->
            val items = itemsOf(data, folder.id)
            FolderSummary(folder, items.size, items.take(COVERS).map { it.thumbnail }.filter { it.isNotBlank() })
        }

    private const val COVERS = 3

    private fun liveItemCount(data: LibraryData) = data.items.count { !it.deleted }

    private fun customFolders(data: LibraryData) = data.folders.filter { it.id !in SYSTEM_IDS && !it.deleted }

    // ---- 영상

    fun add(
        data: LibraryData,
        folderId: String,
        video: VideoSnapshot,
        now: Long,
    ): Pair<LibraryData, AddResult> {
        if (video.videoId.isBlank()) return data to AddResult.NoFolder
        val folder = data.folders.firstOrNull { it.id == folderId && !it.deleted } ?: return data to AddResult.NoFolder
        val key = LibraryItem.keyOf(folder.id, video.videoId)
        val existing = data.items.firstOrNull { it.key == key }
        if (existing != null && !existing.deleted) return data to AddResult.AlreadySaved
        if (liveItemCount(data) >= LibraryLimits.ITEMS_MAX) return data to AddResult.LimitReached

        val item =
            LibraryItem(
                folderId = folder.id,
                videoId = video.videoId,
                title = video.title,
                thumbnail = video.thumbnail,
                channelId = video.channelId,
                channelTitle = video.channelTitle,
                duration = video.duration,
                savedAt = now,
                deleted = false,
                updatedAt = now,
            )
        val items = data.items.filter { it.key != key } + item
        return data.copy(items = items, dirtyItems = data.dirtyItems + key) to AddResult.Added
    }

    fun remove(
        data: LibraryData,
        folderId: String,
        videoId: String,
        now: Long,
    ): LibraryData {
        val key = LibraryItem.keyOf(folderId, videoId)
        val existing = data.items.firstOrNull { it.key == key && !it.deleted } ?: return data
        val items = data.items.map { if (it.key == key) existing.copy(deleted = true, updatedAt = now) else it }
        return data.copy(items = items, dirtyItems = data.dirtyItems + key)
    }

    // ---- 폴더

    fun createFolder(
        data: LibraryData,
        rawName: String,
        id: String,
        now: Long,
    ): Pair<LibraryData, FolderResult> {
        val name = normalizeName(rawName) ?: return data to FolderResult.NameInvalid
        if (nameTaken(data, name, exceptId = null)) return data to FolderResult.NameTaken
        val custom = customFolders(data)
        if (custom.size >= LibraryLimits.FOLDERS_MAX) return data to FolderResult.LimitReached
        val folder = LibraryFolder(id, name, FolderKind.CUSTOM, position = (custom.maxOfOrNull { it.position } ?: 0) + 1, updatedAt = now)
        return data.copy(folders = data.folders + folder, dirtyFolders = data.dirtyFolders + id) to FolderResult.Done(folder)
    }

    fun rename(
        data: LibraryData,
        id: String,
        rawName: String,
        now: Long,
    ): Pair<LibraryData, FolderResult> {
        val folder = customFolders(data).firstOrNull { it.id == id } ?: return data to FolderResult.NoFolder
        val name = normalizeName(rawName) ?: return data to FolderResult.NameInvalid
        if (nameTaken(data, name, exceptId = id)) return data to FolderResult.NameTaken
        if (name == folder.name) return data to FolderResult.Done(folder)
        val renamed = folder.copy(name = name, updatedAt = now)
        return data.copy(folders = data.folders.map { if (it.id == id) renamed else it }, dirtyFolders = data.dirtyFolders + id) to
            FolderResult.Done(renamed)
    }

    /** 사용자 폴더를 지우고(지운 표시), 그 안의 영상도 같이 지운 표시를 한다. */
    fun deleteFolder(
        data: LibraryData,
        id: String,
        now: Long,
    ): LibraryData {
        val folder = customFolders(data).firstOrNull { it.id == id } ?: return data
        val gone = folder.copy(deleted = true, updatedAt = now)
        val dirty = data.dirtyItems.toMutableSet()
        val items =
            data.items.map {
                if (it.folderId == id && !it.deleted) {
                    dirty += it.key
                    it.copy(deleted = true, updatedAt = now)
                } else {
                    it
                }
            }
        return data.copy(
            folders = data.folders.map { if (it.id == id) gone else it },
            items = items,
            dirtyFolders = data.dirtyFolders + id,
            dirtyItems = dirty,
        )
    }

    private fun nameTaken(
        data: LibraryData,
        name: String,
        exceptId: String?,
    ): Boolean = customFolders(data).any { it.id != exceptId && it.name.equals(name, ignoreCase = true) }

    // ---- 서버와 맞추기

    /** 올릴 변경(사용자 폴더 먼저, 그다음 영상)을 [max] 개까지. */
    fun dirtyRows(
        data: LibraryData,
        max: Int = LibraryLimits.BATCH_MAX,
    ): Pair<List<LibraryFolder>, List<LibraryItem>> {
        val folders = data.folders.filter { it.id in data.dirtyFolders && it.id !in SYSTEM_IDS }.take(max)
        val items = data.items.filter { it.key in data.dirtyItems }.take(max - folders.size)
        return folders to items
    }

    fun hasDirty(data: LibraryData): Boolean = data.dirtyFolders.isNotEmpty() || data.dirtyItems.isNotEmpty()

    /** 서버에 없는 것으로 보일 때(서버가 되돌려졌을 때) 가진 것을 전부 다시 올리게 한다. */
    fun markAllDirty(data: LibraryData): LibraryData =
        data.copy(
            rev = 0L,
            dirtyFolders = data.folders.filter { it.id !in SYSTEM_IDS }.map { it.id }.toSet(),
            dirtyItems = data.items.map { it.key }.toSet(),
        )

    /** 올릴 수 없는 행(형식이 올바르지 않은 영상 등)을 dirty 에서 뺀다 — 계속 걸려서 다른 행을 막지 않게. */
    fun dropDirty(
        data: LibraryData,
        folderIds: Set<String>,
        itemKeys: Set<String>,
    ): LibraryData = data.copy(dirtyFolders = data.dirtyFolders - folderIds, dirtyItems = data.dirtyItems - itemKeys)

    /**
     * 서버 응답을 반영한다. 서버 시각이 같거나 새로우면 서버 값을 쓰고, 기기 값이 더 새로우면(동기화 중에 고쳤거나 서버가 무시한 것) 기기 값을 둔다.
     * [pushedFolders]/[pushedItems]: 이번에 올린 행의 key → 올릴 때의 updatedAt. 올린 뒤로 바뀌지 않았으면 dirty 를 지운다.
     */
    fun merge(
        data: LibraryData,
        serverFolders: List<LibraryFolder>,
        serverItems: List<LibraryItem>,
        serverRev: Long,
        pushedFolders: Map<String, Long>,
        pushedItems: Map<String, Long>,
    ): LibraryData {
        val folders = data.folders.associateBy { it.id }.toMutableMap()
        val takenFolders = mutableSetOf<String>()
        for (s in serverFolders) {
            if (s.id in SYSTEM_IDS) continue
            val local = folders[s.id]
            if (local == null || s.updatedAt >= local.updatedAt) {
                folders[s.id] = s
                takenFolders += s.id
            }
        }
        val items = data.items.associateBy { it.key }.toMutableMap()
        val takenItems = mutableSetOf<String>()
        for (s in serverItems) {
            val local = items[s.key]
            if (local == null || s.updatedAt >= local.updatedAt) {
                items[s.key] = s
                takenItems += s.key
            }
        }

        val dirtyFolders =
            data.dirtyFolders.filterTo(mutableSetOf()) { id ->
                val pushedAt = pushedFolders[id]
                id !in takenFolders && (pushedAt == null || (folders[id]?.updatedAt ?: 0L) > pushedAt)
            }
        val dirtyItems =
            data.dirtyItems.filterTo(mutableSetOf()) { key ->
                val pushedAt = pushedItems[key]
                key !in takenItems && (pushedAt == null || (items[key]?.updatedAt ?: 0L) > pushedAt)
            }
        return withSystemFolders(
            data.copy(
                folders = folders.values.toList(),
                items = items.values.toList(),
                rev = serverRev,
                dirtyFolders = dirtyFolders,
                dirtyItems = dirtyItems,
            ),
        )
    }
}
