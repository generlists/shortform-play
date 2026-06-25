package com.sean.ratel.android.data.repository

import android.content.Context
import android.icu.util.TimeZone
import com.sean.ratel.android.R
import com.sean.ratel.android.data.api.ApiResult
import com.sean.ratel.android.data.api.ServerErrorException
import com.sean.ratel.android.data.api.youtube.YouTubeSearchApi
import com.sean.ratel.android.data.dto.PromotionResponse
import com.sean.ratel.android.data.dto.VerifyIAPRequest
import com.sean.ratel.android.data.dto.VerifyIAPResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        val youTubeSearchApi: YouTubeSearchApi,
    ) {
        fun requestPremiumPromotion(
            appId: String,
            locale: String,
        ): Flow<ApiResult<PromotionResponse>> =

            flow {
                emit(ApiResult.Loading)
                try {
                    val response = youTubeSearchApi.requestPremiumPromotion(appId, locale, TimeZone.getDefault().id)

                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string() // 서버가 보낸 JSON 본문을 문자열로 가져옴!!

                    val message =
                        when {
                            // 400번대 에러일 때 서버가 보낸 찰진 메시지 추출!!
                            (e.code() in 400..499) -> {
                                try {
                                    val jsonObject = JSONObject(errorBody)
                                    jsonObject.optString(
                                        "message",
                                        e.message(),
                                    )
                                } catch (jsonException: Exception) {
                                    jsonException.message
                                        ?: context.resources.getString(R.string.api_unknown_error)
                                }
                            }

                            (e.code() in 500..599) -> {
                                context.resources.getString(R.string.api_server_error)
                            }

                            else -> {
                                context.resources.getString(R.string.api_unknown_error)
                            }
                        }

                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }

        fun verifyIap(verifyIAPRequest: VerifyIAPRequest): Flow<ApiResult<VerifyIAPResponse>> =

            flow {
                emit(ApiResult.Loading)
                try {
                    val response = youTubeSearchApi.verifyIap(verifyIAPRequest)

                    emit(ApiResult.Success(response))
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string() // 서버가 보낸 JSON 본문을 문자열로 가져옴!!

                    val message =
                        when {
                            // 400번대 에러일 때 서버가 보낸 찰진 메시지 추출!!
                            (e.code() in 400..499) -> {
                                try {
                                    // 서버 응답이 {"success":false, "message":"로그인해라 시발!!", ...} 이니까
                                    val jsonObject = JSONObject(errorBody)
                                    jsonObject.optString(
                                        "message",
                                        e.message(),
                                    )
                                } catch (jsonException: Exception) {
                                    e.message()
                                        ?: context.resources.getString(R.string.api_unknown_error)
                                }
                            }

                            (e.code() in 500..599) -> {
                                context.resources.getString(R.string.api_server_error)
                            }

                            else -> {
                                context.resources.getString(R.string.api_unknown_error)
                            }
                        }

                    emit(ApiResult.Exception(ServerErrorException(message)))
                } catch (e: Exception) {
                    emit(ApiResult.Exception(e))
                }
            }
    }
