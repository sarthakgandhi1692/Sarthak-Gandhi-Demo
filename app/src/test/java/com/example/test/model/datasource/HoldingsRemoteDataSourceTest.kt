package com.example.test.model.datasource

import com.example.test.api.DemoApi
import com.example.test.base.ApiException
import com.example.test.base.Result
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class HoldingsRemoteDataSourceTest {

    @Mock
    private lateinit var mockDemoApi: DemoApi

    private lateinit var holdingsRemoteDataSource: HoldingsRemoteDataSourceImpl

    @Before
    fun setUp() {
        holdingsRemoteDataSource = HoldingsRemoteDataSourceImpl(mockDemoApi)
    }

    @Test
    fun `getUserHoldings should return success when API call succeeds`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        val mockResponse = Response.success(mockHoldingsResponse)
        `when`(mockDemoApi.getHoldings()).thenReturn(mockResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Success)
        val successResult = result as Result.Success
        assertEquals(mockHoldingsResponse, successResult.data)
    }

    @Test
    fun `getUserHoldings should return error when API call fails with network error`() = runTest {
        // Given
        val networkException = Exception("Network error")
        `when`(mockDemoApi.getHoldings()).thenAnswer { throw networkException }

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals(networkException, errorResult.exception)
    }

    @Test
    fun `getUserHoldings should return error when API response is unsuccessful`() = runTest {
        // Given
        val errorResponse =
            Response.error<HoldingsResponse>(500, okhttp3.ResponseBody.create(null, ""))
        `when`(mockDemoApi.getHoldings()).thenReturn(errorResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(500, apiException.responseCode)
    }

    @Test
    fun `getUserHoldings should return error when API response body is null`() = runTest {
        // Given
        val nullBodyResponse = Response.success<HoldingsResponse>(null)
        `when`(mockDemoApi.getHoldings()).thenReturn(nullBodyResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(200, apiException.responseCode)
    }

    @Test
    fun `getUserHoldings should handle empty holdings list successfully`() = runTest {
        // Given
        val emptyHoldingsResponse = createEmptyHoldingsResponse()
        val mockResponse = Response.success(emptyHoldingsResponse)
        `when`(mockDemoApi.getHoldings()).thenReturn(mockResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Success)
        val successResult = result as Result.Success
        assertEquals(emptyHoldingsResponse, successResult.data)
        assertTrue(successResult.data.data.userHolding.isEmpty())
    }

    @Test
    fun `getUserHoldings should handle large holdings list successfully`() = runTest {
        // Given
        val largeHoldingsResponse = createLargeHoldingsResponse(100)
        val mockResponse = Response.success(largeHoldingsResponse)
        `when`(mockDemoApi.getHoldings()).thenReturn(mockResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Success)
        val successResult = result as Result.Success
        assertEquals(largeHoldingsResponse, successResult.data)
        assertEquals(100, successResult.data.data.userHolding.size)
    }

    @Test
    fun `getUserHoldings should handle API timeout gracefully`() = runTest {
        // Given
        val timeoutException = Exception("Timeout")
        `when`(mockDemoApi.getHoldings()).thenAnswer { throw timeoutException }

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals(timeoutException, errorResult.exception)
    }

    @Test
    fun `getUserHoldings should handle malformed response gracefully`() = runTest {
        // Given
        val malformedResponse = Response.success<HoldingsResponse>(null)
        `when`(mockDemoApi.getHoldings()).thenReturn(malformedResponse)

        // When
        val result = holdingsRemoteDataSource.getUserHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertTrue(errorResult.exception is ApiException)
        val apiException = errorResult.exception as ApiException
        assertEquals(200, apiException.responseCode)
    }

    // Helper methods to create test data
    private fun createMockHoldingsResponse(): HoldingsResponse {
        val userHoldings = listOf(
            UserHolding(
                avgPrice = 100.0,
                close = 105.0,
                ltp = 110.0,
                quantity = 10,
                symbol = "AAPL"
            ),
            UserHolding(
                avgPrice = 200.0,
                close = 210.0,
                ltp = 220.0,
                quantity = 5,
                symbol = "GOOGL"
            )
        )
        val data = Data(userHolding = userHoldings)
        return HoldingsResponse(data = data)
    }

    private fun createEmptyHoldingsResponse(): HoldingsResponse {
        val data = Data(userHolding = emptyList())
        return HoldingsResponse(data = data)
    }

    private fun createLargeHoldingsResponse(count: Int): HoldingsResponse {
        val userHoldings = (1..count).map { index ->
            UserHolding(
                avgPrice = 100.0 + index,
                close = 105.0 + index,
                ltp = 110.0 + index,
                quantity = index,
                symbol = "STOCK$index"
            )
        }
        val data = Data(userHolding = userHoldings)
        return HoldingsResponse(data = data)
    }
} 