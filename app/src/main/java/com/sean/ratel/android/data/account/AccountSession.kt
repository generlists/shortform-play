package com.sean.ratel.android.data.account

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/** 로그인한 사용자 (Firebase Auth). */
data class AccountUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    /** google.com, apple.com 등 로그인 방식. */
    val provider: String?,
)

sealed interface SignInResult {
    data class Success(
        val user: AccountUser,
    ) : SignInResult

    /** 사용자가 계정 선택 창을 닫았다. */
    data object Cancelled : SignInResult

    /** 기기에 Google 계정이 없다. */
    data object NoAccount : SignInResult

    data class Failure(
        val message: String?,
    ) : SignInResult
}

/**
 * 로그인 세션. 실제 구현은 [AccountRepository](Firebase Auth)이고, 필터 동기화와 인터셉터는 이 인터페이스에만 의존해서
 * 기기 없이 JVM 에서 테스트할 수 있다.
 */
interface AccountSession {
    /** 로그인 상태가 바뀔 때마다 내보낸다(앱 시작 때 현재 상태도 한 번). 로그아웃 상태는 null. */
    val user: Flow<AccountUser?>

    val currentUser: AccountUser?

    /** 서버에 보낼 Firebase ID 토큰. 로그인하지 않았거나 받지 못하면 null. */
    suspend fun idToken(forceRefresh: Boolean = false): String?

    suspend fun signInWithGoogle(activity: Activity): SignInResult

    /** 로그아웃(기기에서만). 서버 데이터는 건드리지 않는다. */
    suspend fun signOut()
}
