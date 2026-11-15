package com.sean.ratel.android.data.library

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import so.smartlab.common.utils.log.RLog
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** 보관함을 기기에 저장한다. */
interface LibraryStore {
    suspend fun load(): LibraryData

    suspend fun save(data: LibraryData)

    suspend fun clear()
}

/**
 * JSON 파일 하나에 저장한다(영상이 많아도 DataStore 처럼 매번 통째로 다시 쓰지 않고, 바뀔 때만 쓴다).
 * 쓰다가 끊겨도 깨지지 않게 임시 파일에 쓴 뒤 바꿔치기한다. 읽을 수 없으면 빈 보관함으로 시작한다.
 */
class FileLibraryStore(
    private val file: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LibraryStore {
    private val gson = Gson()

    override suspend fun load(): LibraryData =
        withContext(dispatcher) {
            if (!file.exists()) return@withContext LibraryData()
            try {
                gson.fromJson(file.readText(), LibraryData::class.java) ?: LibraryData()
            } catch (e: Exception) {
                RLog.e(TAG, "보관함 파일을 읽지 못해 비어 있는 것으로 시작한다: ${e.message}")
                LibraryData()
            }
        }

    override suspend fun save(data: LibraryData) {
        withContext(dispatcher) {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(gson.toJson(data))
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }
    }

    override suspend fun clear() {
        withContext(dispatcher) { file.delete() }
    }

    private companion object {
        const val TAG = "LibraryStore"
    }
}
