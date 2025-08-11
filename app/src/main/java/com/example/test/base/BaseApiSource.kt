package com.example.test.base

import com.squareup.moshi.Moshi
import retrofit2.Response


abstract class BaseApiSource {

    companion object {
        private const val TAG = "BaseApiSource"
    }

    /**
     * Makes an API call and handles the response, converting it to a [Result] object.
     * 
     * @param call Suspend function that makes the actual API call returning a [Response]
     * @return [Result] containing either the successful response body or an error
     */
    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Result<T> {
        val moshiAdapter = Moshi
            .Builder()
            .build()
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Result.Success(body)
            }

            val errorBody = response.errorBody()
                ?: return getDefaultError(response.code())

            return try {
                val errorBytes = String(errorBody.bytes())
                val moshi = moshiAdapter.adapter(ErrorResponse::class.java)
                val value = moshi.fromJson(errorBytes)
                if (value != null) {
                    error(ApiException(response.code(), value))
                } else {
                    getDefaultError(response.code())
                }
            } catch (e: Exception) {
                getDefaultError(response.code())
            }
        } catch (e: Exception) {
            return error(e)
        }
    }

    /**
     * Creates a default error Result with a standard error response for a given HTTP response code.
     * 
     * @param responseCode The HTTP response code from the failed request
     * @return [Result.Error] with default error information
     */
    private fun <T> getDefaultError(responseCode: Int): Result<T> {
        return error(ApiException(responseCode, getDefaultErrorResponse()))
    }

    /**
     * Wraps an exception in a [Result.Error].
     * 
     * @param error The exception to wrap
     * @return [Result.Error] containing the provided exception
     */
    private fun <T> error(error: Exception): Result<T> {
        return Result.Error(error)
    }

    /**
     * Creates a default error response with standard error messages.
     * 
     * @return [ErrorResponse] with default error information
     */
    private fun getDefaultErrorResponse(): ErrorResponse {
        return ErrorResponse(
            type = "ShowToastError",
            title = "Something went wrong",
            subtitle = "Something went wrong",
            imageUrl = ""
        )
    }
}

/**
 * Custom exception class for API errors that includes the HTTP response code
 * and the error response body.
 *
 * @property responseCode The HTTP response code from the failed request
 * @property errorResponse The parsed error response from the API, if available
 */
class ApiException(
    val responseCode: Int,
    val errorResponse: ErrorResponse?
) : Exception()
