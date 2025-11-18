package com.sean.ratel.android.data.repository

import android.content.Context
import com.auth0.jwt.JWT
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.sean.ratel.android.data.api.AuthApi
import com.sean.ratel.android.data.dto.IntegrityExchangeReq
import com.sean.ratel.android.data.dto.IntegrityExchangeRes
import com.sean.ratel.android.di.qualifier.GoogleCloudProjectNumber
import com.sean.ratel.android.utils.TimeUtil.expTimePrint
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private val authApi: AuthApi,
        @GoogleCloudProjectNumber private val cloudProjectNumber: String,
    ) {
        // 최신 버전에서는 이 방식으로 매니저 생성해야 함
        private val integrityManager by lazy {
            IntegrityManagerFactory.createStandard(appContext)
        }

        /** 1-3. 서버에서 nonce 받기 */
        suspend fun getRequestHash(): String {
            try {
                return authApi.getRequestHash().request_hash
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        suspend fun exchange(body: IntegrityExchangeReq): Response<IntegrityExchangeRes> = authApi.exchange(body)

        fun isExpired(token: String?): Boolean {
            return try {
                token?.let {
                    val claims = JWT.decode(token)

                    val expTime = claims.expiresAt?.time ?: return true
                    expTimePrint(expTime, System.currentTimeMillis())

                    expTime < System.currentTimeMillis()
                } ?: true
            } catch (e: Exception) {
                true
            }
        }
    }
