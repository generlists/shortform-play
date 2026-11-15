package com.sean.ratel.android.data.filter

import com.google.gson.Gson
import com.sean.ratel.android.data.account.AccountSession
import com.sean.ratel.android.data.api.youtube.ShortFormPlayV2Api
import com.sean.ratel.android.data.dto.FiltersConflictDto
import com.sean.ratel.android.data.dto.FiltersDto
import com.sean.ratel.android.data.dto.FiltersSaveRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncResult {
    data object Success : SyncResult

    /** 로그인하지 않았다 — 필터는 로그인한 계정에만 저장한다. */
    data object NotSignedIn : SyncResult

    /** 네트워크·서버 오류. 변경은 기기에 남아 있고(dirty) 다음 동기화 때 다시 올린다. */
    data class Failure(
        val reason: String,
    ) : SyncResult
}

/**
 * 내 필터와 서버(/shortformplay/v2)를 맞춘다.
 *
 * - 규칙: **로그인하지 않으면 필터가 없다.** 로그아웃하거나 로그인 상태가 아니면 기기의 필터를 지운다.
 * - 로그인(또는 앱 시작)하면 서버에서 받아 기기에 반영한다. 올리지 못한 변경(dirty)이 있으면 서버와 합쳐서(union) 올린다.
 * - 저장하면 기기에 먼저 저장하고 서버에 올린다. 다른 기기가 먼저 저장해서 409 가 오면 최신 version 으로 한 번 더
 *   시도한다(마지막으로 저장한 기기가 이긴다). 실패하면 dirty 로 남겨 다음에 다시 올린다.
 */
@Singleton
class FilterSyncRepository
    @Inject
    constructor(
        private val api: ShortFormPlayV2Api,
        private val account: AccountSession,
        private val filters: FilterRepository,
    ) {
        private val mutex = Mutex()
        private val gson = Gson()

        /**
         * 실패했을 때 다시 시도하기까지의 간격(ms). 앱을 막 켰을 때는 Integrity 토큰이 아직 준비되지 않아 서버가 411 로 거절하므로
         * 잠시 뒤 다시 해 본다. 테스트에서 줄여 쓴다.
         */
        internal var retryDelaysMs: List<Long> = listOf(3_000L, 8_000L, 20_000L, 45_000L)

        /** 로그인 상태를 지켜보며 맞춘다. 앱이 시작될 때 한 번 호출한다. */
        @OptIn(ExperimentalCoroutinesApi::class)
        fun start(scope: CoroutineScope): Job =
            scope.launch {
                // 로그인 상태가 바뀌면 진행 중이던 재시도는 취소하고 새 상태로 다시 한다
                account.user.collectLatest { user ->
                    if (user == null) {
                        mutex.withLock { clearStaleFilter() }
                    } else {
                        reconcileWithRetry()
                    }
                }
            }

        private suspend fun reconcileWithRetry() {
            var result = reconcile()
            for (delayMs in retryDelaysMs) {
                if (result !is SyncResult.Failure) return
                delay(delayMs)
                result = reconcile()
            }
        }

        /** 서버의 필터를 받아 기기와 맞춘다(로그인 직후, 앱 시작 때). */
        suspend fun reconcile(): SyncResult =
            mutex.withLock {
                if (account.currentUser == null) return@withLock SyncResult.NotSignedIn
                RLog.d(TAG, "서버와 동기화 시작(login)")
                val server =
                    try {
                        api.login().filters.toFilter()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        return@withLock failure("login", e)
                    }
                RLog.d(TAG, "서버 필터 받음: version=${server.version}")
                val local = filters.state.first()
                if (local.dirty) {
                    // 올리지 못한 변경이 있다. 서버가 그사이 바뀌었으면 합쳐서 올린다
                    val content = if (server.version == local.filter.version) local.filter else FilterMerge.union(server, local.filter)
                    put(content, server.version, local.options)
                } else {
                    if (local.filter != server) filters.save(local.copy(filter = server, dirty = false))
                    SyncResult.Success
                }
            }

        /** 사용자가 필터를 저장했다. 로그인하지 않았으면 저장하지 않는다. */
        suspend fun saveFromUser(
            filter: ExcludeFilter,
            options: FilterOptions,
        ): SyncResult =
            mutex.withLock {
                if (account.currentUser == null) return@withLock SyncResult.NotSignedIn
                val current = filters.state.first()
                val content = FilterLimits.sanitize(filter).copy(version = current.filter.version)
                filters.save(FilterState(content, options, dirty = true))
                put(content, content.version, options)
            }

        /** 로그아웃 — 기기의 필터도 지운다(피드가 원래대로 돌아온다). 서버의 필터는 그대로 둔다. */
        suspend fun signOut() =
            mutex.withLock {
                account.signOut()
                filters.clear()
            }

        /** 탈퇴 — 서버가 내 필터와 계정을 지우고, 성공하면 기기에서도 로그아웃한다. */
        suspend fun deleteAccount(): SyncResult =
            mutex.withLock {
                if (account.currentUser == null) return@withLock SyncResult.NotSignedIn
                val response =
                    try {
                        api.deleteMe()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        return@withLock failure("deleteMe", e)
                    }
                if (!response.isSuccessful) return@withLock SyncResult.Failure("http ${response.code()}")
                account.signOut()
                filters.clear()
                SyncResult.Success
            }

        /** [baseVersion] 은 앱이 마지막으로 받은 서버 version. 409 면 최신 version 으로 한 번 더 시도한다. */
        private suspend fun put(
            content: ExcludeFilter,
            baseVersion: Int,
            options: FilterOptions,
            retry: Boolean = true,
        ): SyncResult {
            val body = FilterLimits.sanitize(content).toRequest(baseVersion)
            val response: Response<FiltersDto> =
                try {
                    api.putFilters(body)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    return failure("putFilters", e)
                }
            return when {
                response.isSuccessful -> {
                    val saved = response.body().toFilter()
                    filters.save(FilterState(saved, options, dirty = false))
                    SyncResult.Success
                }

                response.code() == HTTP_CONFLICT && retry -> {
                    val current = parseConflict(response)
                    if (current == null) SyncResult.Failure("conflict") else put(content, current.version, options, retry = false)
                }

                else -> SyncResult.Failure("http ${response.code()}")
            }
        }

        private suspend fun clearStaleFilter() {
            if (filters.state.first() != FilterState()) filters.clear()
        }

        private fun parseConflict(response: Response<*>): ExcludeFilter? =
            try {
                gson.fromJson(response.errorBody()?.string(), FiltersConflictDto::class.java)?.current?.toFilter()
            } catch (e: Exception) {
                null
            }

        private fun failure(
            step: String,
            e: Exception,
        ): SyncResult.Failure {
            RLog.e(TAG, "$step 실패: ${e.javaClass.name} ${e.message?.take(MAX_LOG_MESSAGE)}")
            return SyncResult.Failure("$step ${e.javaClass.simpleName}")
        }

        private fun ExcludeFilter.toRequest(baseVersion: Int) =
            FiltersSaveRequest(
                version = baseVersion,
                topics = topics,
                keywords = keywords,
                blockedChannels = blockedChannels,
                exceptions = exceptions,
            )

        private fun FiltersDto?.toFilter(): ExcludeFilter =
            ExcludeFilter(
                topics = this?.topics.orEmpty(),
                keywords = this?.keywords.orEmpty(),
                blockedChannels = this?.blockedChannels.orEmpty(),
                exceptions = this?.exceptions.orEmpty(),
                version = this?.version ?: 0,
            )

        private companion object {
            const val TAG = "FilterSyncRepository"
            const val HTTP_CONFLICT = 409
            const val MAX_LOG_MESSAGE = 300
        }
    }
