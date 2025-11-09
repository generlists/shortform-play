package com.sean.ratel.android.data.api

import com.sean.ratel.android.data.dto.IntegrityExchangeReq
import com.sean.ratel.android.data.dto.IntegrityExchangeRes
import com.sean.ratel.android.data.dto.RequestHashResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @GET(EndPoint.AUTH_HASH)
    suspend fun getRequestHash(): RequestHashResponse

    @Headers("Content-Type: application/json")
    @POST(EndPoint.AUTH_INTEGRITY)
    suspend fun exchange(
        @Body body: IntegrityExchangeReq,
    ): Response<IntegrityExchangeRes>
}
