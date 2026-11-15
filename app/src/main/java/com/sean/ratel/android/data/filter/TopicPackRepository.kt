package com.sean.ratel.android.data.filter

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 주제 묶음을 앱에 들어 있는 assets/topics.json 에서 읽는다(한 번 읽고 캐시).
 * 나중에 Firebase Storage 의 최신본으로 갱신하는 단계를 여기에 붙인다.
 */
@Singleton
class TopicPackRepository internal constructor(
    private val loadJson: suspend () -> String?,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this({ readAsset(context) })

    @Volatile
    private var cache: TopicPackSet? = null

    suspend fun get(): TopicPackSet =
        cache ?: withContext(Dispatchers.IO) {
            TopicPackParser.parse(loadJson()).also { cache = it }
        }

    companion object {
        const val ASSET_NAME = "topics.json"

        private fun readAsset(context: Context): String? =
            try {
                context.assets
                    .open(ASSET_NAME)
                    .bufferedReader()
                    .use { it.readText() }
            } catch (e: Exception) {
                null
            }
    }
}
