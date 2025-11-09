package com.sean.ratel.android.data.api

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : ApiResult<T>()

    data class Error(
        val code: Int,
        val message: String,
    ) : ApiResult<Nothing>()

    data class Exception(
        val e: Throwable,
    ) : ApiResult<Nothing>()

    object Loading : ApiResult<Nothing>()

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> =
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { Success(it) }
                    ?: Error(response.code(), "Empty body")
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = errorString ?: "Unknown server error"
                Error(response.code(), errorMessage)
            }
        } catch (e: HttpException) {
            Error(e.code(), e.message ?: "HTTP exception")
        } catch (e: IOException) {
            Exception(e)
        } catch (e: java.lang.Exception) {
            Exception(e)
        }
}
