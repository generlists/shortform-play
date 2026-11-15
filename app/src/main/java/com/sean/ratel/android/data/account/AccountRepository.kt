package com.sean.ratel.android.data.account

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sean.ratel.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import so.smartlab.common.utils.log.RLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Firebase Auth + Credential Manager 로 Google 로그인을 처리한다. */
@Singleton
class AccountRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AccountSession {
        private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
        private val credentialManager by lazy { CredentialManager.create(context) }

        override val user: Flow<AccountUser?> =
            callbackFlow {
                val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAccountUser()) }
                auth.addAuthStateListener(listener)
                awaitClose { auth.removeAuthStateListener(listener) }
            }.distinctUntilChanged()

        override val currentUser: AccountUser? get() = auth.currentUser?.toAccountUser()

        override suspend fun idToken(forceRefresh: Boolean): String? {
            val current = auth.currentUser ?: return null
            return try {
                current.getIdToken(forceRefresh).awaitResult().token
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RLog.e(TAG, "id token 실패: ${e.javaClass.simpleName}")
                null
            }
        }

        override suspend fun signInWithGoogle(activity: Activity): SignInResult =
            try {
                val option =
                    GetGoogleIdOption
                        .Builder()
                        // google-services 플러그인이 만들어 주는 웹 클라이언트 ID(Firebase 프로젝트 것)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val credential = credentialManager.getCredential(activity, request).credential
                RLog.d(TAG, "google credential 받음: ${credential.type}")
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null)).awaitResult()
                    // 개인정보(이메일)는 남기지 않고 값이 있는지만 남긴다
                    RLog.d(TAG, "firebase 로그인 성공: email=${result.user?.email != null} providers=${result.user?.providerData?.size}")
                    result.user?.let { SignInResult.Success(it.toAccountUser()) } ?: SignInResult.Failure("no user")
                } else {
                    RLog.e(TAG, "지원하지 않는 credential: ${credential.type}")
                    SignInResult.Failure("unsupported credential")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {
                RLog.d(TAG, "로그인 취소")
                SignInResult.Cancelled
            } catch (e: NoCredentialException) {
                RLog.e(TAG, "사용할 Google 계정 없음: ${e.message}")
                SignInResult.NoAccount
            } catch (e: GetCredentialException) {
                RLog.e(TAG, "credential 실패: ${e.javaClass.simpleName} ${e.type} ${e.message}")
                SignInResult.Failure(e.javaClass.simpleName)
            } catch (e: GoogleIdTokenParsingException) {
                RLog.e(TAG, "Google 토큰 해석 실패: ${e.message}")
                SignInResult.Failure(e.javaClass.simpleName)
            } catch (e: Exception) {
                // Firebase 로그인 실패(FirebaseAuthException 등) — 원인을 알 수 있게 클래스와 메시지를 남긴다
                RLog.e(TAG, "로그인 실패: ${e.javaClass.name} ${e.message?.take(MAX_LOG_MESSAGE)}")
                SignInResult.Failure(e.javaClass.simpleName)
            }

        override suspend fun signOut() {
            auth.signOut()
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                RLog.e(TAG, "clearCredentialState 실패: ${e.javaClass.simpleName}")
            }
        }

        /**
         * Firebase 가 사용자 객체의 email 을 비워 줄 때가 있어서(로그인 방식별 정보에는 있다) provider 정보에서도 찾는다.
         */
        private fun FirebaseUser.toAccountUser(): AccountUser {
            val providers = providerData.filter { it.providerId != FIREBASE_PROVIDER_ID }
            return AccountUser(
                uid = uid,
                email = email?.takeIf { it.isNotBlank() } ?: providers.firstNotNullOfOrNull { it.email?.takeIf { e -> e.isNotBlank() } },
                displayName =
                    displayName?.takeIf { it.isNotBlank() }
                        ?: providers.firstNotNullOfOrNull { it.displayName?.takeIf { n -> n.isNotBlank() } },
                photoUrl = (photoUrl ?: providers.firstNotNullOfOrNull { it.photoUrl })?.toString(),
                provider = providers.firstOrNull()?.providerId,
            )
        }

        private suspend fun <T> Task<T>.awaitResult(): T =
            suspendCancellableCoroutine { continuation ->
                addOnSuccessListener { continuation.resume(it) }
                addOnFailureListener { continuation.resumeWithException(it) }
                addOnCanceledListener { continuation.cancel() }
            }

        private companion object {
            const val TAG = "AccountRepository"
            const val FIREBASE_PROVIDER_ID = "firebase"
            const val MAX_LOG_MESSAGE = 300
        }
    }
