package com.sean.ratel.android.data.common

import android.content.Context
import android.widget.Toast
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.sean.player.utils.log.RLog
import com.sean.ratel.android.BuildConfig
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.ApiResult.Loading.safeApiCall
import com.sean.ratel.android.data.api.AuthApi
import com.sean.ratel.android.data.dto.IntegrityExchangeReq
import com.sean.ratel.android.data.local.pref.AuthTokenPreference
import com.sean.ratel.android.di.qualifier.GoogleCloudProjectNumber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class IntegrityManager
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        @GoogleCloudProjectNumber private val cloudProjectNumber: String,
        private val authTokenPreference: AuthTokenPreference,
    ) {
        private val integrityManager by lazy {
            IntegrityManagerFactory.createStandard(appContext)
        }

        suspend fun requestIntegrityToken(requestHash: String): IntegrityResult =
            suspendCancellableCoroutine { cont ->
                try {
                    val prepareReq =
                        StandardIntegrityManager.PrepareIntegrityTokenRequest
                            .builder()
                            .setCloudProjectNumber(cloudProjectNumber.toLong())
                            .build()

                    integrityManager
                        .prepareIntegrityToken(prepareReq)
                        .addOnSuccessListener { provider ->
                            val req =
                                StandardIntegrityTokenRequest
                                    .builder()
                                    .setRequestHash(requestHash)
                                    .build()

                            provider
                                .request(req)
                                .addOnSuccessListener { resp ->
                                    cont.resume(IntegrityResult.Success(resp.token()))
                                }.addOnFailureListener { e ->
                                    if (!cont.isCancelled) {
                                        val code = (e as? StandardIntegrityException)?.errorCode ?: 999
                                        cont.resume(IntegrityResult.Failure(code, e.message))
                                    }
                                }
                        }.addOnFailureListener { e ->
                            if (!cont.isCancelled) {
                                cont.resume(IntegrityResult.Failure(1000, e.message))
                            }
                        }
                } catch (e: Exception) {
                    if (!cont.isCancelled) {
                        val code = (e as? StandardIntegrityException)?.errorCode ?: 999
                        cont.resume(IntegrityResult.Failure(code, e.message))
                    }
                }
            }

        suspend fun headerRefreshToken(): Pair<String, Long>? {
            try {
                val hashResponse = authApi.getRequestHash()
                val hash = hashResponse.request_hash

                when (val result = requestIntegrityToken(hash)) {
                    is IntegrityResult.Success -> {
                        val callResult =
                            safeApiCall {
                                authApi.exchange(
                                    IntegrityExchangeReq(
                                        appContext.packageName,
                                        result.token,
                                        hash,
                                    ),
                                )
                            }
                        when (callResult) {
                            is ApiResult.Success -> {
                                RLog.d(TAG, "서버 응답 성공: ${callResult.data}")
                                val data = callResult.data
                                val accessToken = data.access_token
                                val expiresIn = data.expires_in ?: (24 * 3600L)
                                accessToken?.let {
                                    authTokenPreference.saveAccessToken(accessToken, expiresIn)
                                    authTokenPreference.updateTokenCache()
                                    return it to expiresIn
                                }
                                RLog.d(TAG, "Access Token 갱신 성공: expires in $expiresIn 초")
                            }

                            is ApiResult.Error -> {
                                RLog.e(TAG, "서버 응답 오류(${callResult.code}): ${callResult.message}")
                            }

                            is ApiResult.Exception -> {
                                RLog.e(TAG, "기타 네트워크 예외: ${callResult.e.localizedMessage}")
                                Toast
                                    .makeText(
                                        appContext,
                                        "기타 네트워크 예외: ${callResult.e.localizedMessage}",
                                        Toast.LENGTH_LONG,
                                    ).show()
                            }

                            else -> {
                                Unit
                            }
                        }
                    }

                    else -> {
                        Unit
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(appContext, "예외: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            return null
        }

        // hilt 순환참조로 따로 추가
        private val retrofit by lazy {
            Retrofit
                .Builder()
                .baseUrl(BuildConfig.SHORTFORM_PLAY_BASE_URL)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private val authApi = retrofit.create(AuthApi::class.java)

        sealed class IntegrityResult {
            data class Success(
                val token: String,
            ) : IntegrityResult()

            data class Failure(
                val errorCode: Int,
                val message: String?,
            ) : IntegrityResult()
        }

        companion object {
            private val TAG = "IntegrityManager"
        }
    }
