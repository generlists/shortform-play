package com.sean.ratel.android.data.api

sealed class UiState<out T> {
    object Idle : UiState<Nothing>() // 초기 상태

    object Loading : UiState<Nothing>() // 로딩 중

    data class Success<out T>(
        val data: T,
    ) : UiState<T>()

    data class Error(
        val message: String,
    ) : UiState<Nothing>()
}
