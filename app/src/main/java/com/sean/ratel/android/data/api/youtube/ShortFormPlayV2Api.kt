package com.sean.ratel.android.data.api.youtube

import com.sean.ratel.android.data.api.EndPoint
import com.sean.ratel.android.data.dto.FiltersDto
import com.sean.ratel.android.data.dto.FiltersSaveRequest
import com.sean.ratel.android.data.dto.LibrarySyncDto
import com.sean.ratel.android.data.dto.LibrarySyncRequest
import com.sean.ratel.android.data.dto.LoginDto
import com.sean.ratel.android.data.dto.MeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/** 로그인·내 필터 API. 로그인 토큰(X-Firebase-Token)은 FirebaseTokenInterceptor 가 붙인다. */
interface ShortFormPlayV2Api {
    /** 로그인 직후 한 번 — 사용자를 만들거나 갱신하고, 저장된 필터를 같이 받는다. */
    @POST(EndPoint.V2_LOGIN)
    suspend fun login(): LoginDto

    @GET(EndPoint.V2_ME)
    suspend fun me(): MeDto

    /** 탈퇴 — 서버가 내 필터와 Firebase 계정을 지운다. 성공하면 204. */
    @DELETE(EndPoint.V2_ME)
    suspend fun deleteMe(): Response<Unit>

    @GET(EndPoint.V2_FILTERS)
    suspend fun getFilters(): FiltersDto

    /** 저장. 본문 version 이 서버와 다르면 409(본문에 최신 필터)라서 Response 로 받는다. */
    @PUT(EndPoint.V2_FILTERS)
    suspend fun putFilters(
        @Body body: FiltersSaveRequest,
    ): Response<FiltersDto>

    /**
     * 보관함 동기화 — 앱이 바꾼 폴더·영상을 올리고, [LibrarySyncRequest.since] 이후 서버에서 바뀐 것을 받는다.
     * 한도 초과·형식 오류는 422(아무것도 적용하지 않음)라서 Response 로 받는다.
     */
    @POST(EndPoint.V2_LIBRARY_SYNC)
    suspend fun syncLibrary(
        @Body body: LibrarySyncRequest,
    ): Response<LibrarySyncDto>
}
