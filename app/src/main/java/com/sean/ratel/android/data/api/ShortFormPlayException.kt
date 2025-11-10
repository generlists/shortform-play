package com.sean.ratel.android.data.api

import java.io.IOException

// / 네트워크 연결 안 됐을 때
class NoConnectivityException(
    message: String,
) : IOException(message)

// 서버 오류 (5xx)
class ServerErrorException(
    message: String,
) : IOException(message)

// 요청 오류 (4xx)
class BadRequestException(
    message: String,
) : IOException(message)

// 응답 없음
class EmptyResponseException(
    message: String,
) : IOException(message)

// 기타 알 수 없는 오류
class UnknownNetworkException(
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)

// class StandardIntegrityException(errorCode: StandardIntegrityErrorCode,message:String)
