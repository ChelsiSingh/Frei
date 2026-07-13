package com.frei.app.data.remote.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    data object NetworkError : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}


suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(response.code(), "Empty response body")
            }
        } else {
            ApiResult.Error(
                response.code(),
                response.errorBody()?.string() ?: "Unknown error (${response.code()})"
            )
        }
    } catch (e: java.io.IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Error(-1, e.localizedMessage ?: "Unexpected error")
    }
}