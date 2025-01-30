package com.sean.ratel.android.data.api.youtube

import com.sean.ratel.android.data.dto.MainShortsResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface FireBaseApi {
    @GET
    suspend fun requestYouTubeVideos(
        @Url url: String,
    ): MainShortsResponse
}
