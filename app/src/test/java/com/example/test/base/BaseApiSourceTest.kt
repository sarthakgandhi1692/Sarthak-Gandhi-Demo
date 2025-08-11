package com.example.test.base

import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BaseApiSourceTest {

    private val testBaseApiSource = TestBaseApiSource()


    @Test
    fun `getResult should return success when API response is successful with valid body`() =
        runTest {
            // Given
            val mockHoldingsResponse = createMockHoldingsResponse()
            val mockResponse = Response.success(mockHoldingsResponse)

            // When
            val result = testBaseApiSource.testGetResult { mockResponse }

            // Then
            assertTrue(result is Result.Success)
            val successResult = result as Result.Success
            assertEquals(mockHoldingsResponse, successResult.data)
        }

    @Test
    fun `getResult should return error when API response is unsuccessful`() = runTest {
        // Given
        val errorResponse = Response.error<HoldingsResponse>(404, createErrorResponseBody())

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { errorResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(404, apiException.responseCode)
    }

    @Test
    fun `getResult should return error when API response body is null`() = runTest {
        // Given
        val nullBodyResponse = Response.success<HoldingsResponse>(null)

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { nullBodyResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(200, apiException.responseCode)
    }

    @Test
    fun `getResult should return error when API call throws exception`() = runTest {
        // Given
        val networkException = Exception("Network error")

        // When
        val result: Result<HoldingsResponse> =
            testBaseApiSource.testGetResult { throw networkException }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals(networkException, errorResult.exception)
    }

    @Test
    fun `getResult should handle 500 error with default error response`() = runTest {
        // Given
        val errorResponse = Response.error<HoldingsResponse>(500, createErrorResponseBody())

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { errorResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(500, apiException.responseCode)
        assertEquals("ShowToastError", apiException.errorResponse?.type)
        assertEquals("Error", apiException.errorResponse?.title)
    }

    @Test
    fun `getResult should handle 401 unauthorized error`() = runTest {
        // Given
        val errorResponse = Response.error<HoldingsResponse>(401, createErrorResponseBody())

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { errorResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(401, apiException.responseCode)
    }

    @Test
    fun `getResult should handle 403 forbidden error`() = runTest {
        // Given
        val errorResponse = Response.error<HoldingsResponse>(403, createErrorResponseBody())

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { errorResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(403, apiException.responseCode)
    }

    @Test
    fun `getResult should handle 429 rate limit error`() = runTest {
        // Given
        val errorResponse = Response.error<HoldingsResponse>(429, createErrorResponseBody())

        // When
        val result: Result<HoldingsResponse> = testBaseApiSource.testGetResult { errorResponse }

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(429, apiException.responseCode)
    }

    // Helper methods
    private fun createMockHoldingsResponse(): HoldingsResponse {
        val userHoldings = listOf(
            UserHolding(
                avgPrice = 100.0,
                close = 105.0,
                ltp = 110.0,
                quantity = 10,
                symbol = "AAPL"
            )
        )
        val data = Data(userHolding = userHoldings)
        return HoldingsResponse(data = data)
    }

    private fun createErrorResponseBody(): ResponseBody {
        return ResponseBody.create(
            "application/json".toMediaTypeOrNull(),
            """{"type":"ShowToastError","title":"Error","subtitle":"Something went wrong","imageUrl":""}"""
        )
    }

    // Test implementation of BaseApiSource to access protected methods
    private class TestBaseApiSource : BaseApiSource() {
        suspend fun <T> testGetResult(call: suspend () -> Response<T>): Result<T> {
            return getResult(call)
        }
    }
} 