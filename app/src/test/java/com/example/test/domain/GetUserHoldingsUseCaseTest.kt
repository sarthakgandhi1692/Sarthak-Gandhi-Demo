package com.example.test.domain

import com.example.test.model.repository.HoldingsRepository
import com.example.test.model.repository.HoldingsResult
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class GetUserHoldingsUseCaseTest {

    @Mock
    private lateinit var mockHoldingsRepository: HoldingsRepository

    private lateinit var getUserHoldingsUseCase: GetUserHoldingsUseCase

    @Before
    fun setUp() {
        getUserHoldingsUseCase = GetUserHoldingsUseCase(mockHoldingsRepository)
    }

    @Test
    fun `invoke should return flow from repository`() = runTest {
        // Given
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Success(
                holdings = createMockHoldingsResponse(),
                isFromCache = true
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        val result = getUserHoldingsUseCase.invoke()

        // Then
        assertEquals(mockFlow, result)
        verify(mockHoldingsRepository, times(1)).getHoldingsFlow()
    }

    @Test
    fun `invoke should return success flow with cached data`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Success(
                holdings = mockHoldingsResponse,
                isFromCache = true
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        val result = getUserHoldingsUseCase.invoke()
        val firstResult = result.first()

        // Then
        assertTrue(firstResult is HoldingsResult.Success)
        val successResult = firstResult as HoldingsResult.Success
        assertEquals(mockHoldingsResponse, successResult.holdings)
        assertTrue(successResult.isFromCache)
        verify(mockHoldingsRepository, times(1)).getHoldingsFlow()
    }

    @Test
    fun `invoke should return success flow with fresh data`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Success(
                holdings = mockHoldingsResponse,
                isFromCache = false
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        val result = getUserHoldingsUseCase.invoke()
        val firstResult = result.first()

        // Then
        assertTrue(firstResult is HoldingsResult.Success)
        val successResult = firstResult as HoldingsResult.Success
        assertEquals(mockHoldingsResponse, successResult.holdings)
        assertFalse(successResult.isFromCache)
        verify(mockHoldingsRepository, times(1)).getHoldingsFlow()
    }

    @Test
    fun `invoke should return error flow`() = runTest {
        // Given
        val mockException = Exception("Network error")
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Error(
                exception = mockException,
                hasCachedData = false
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        val result = getUserHoldingsUseCase.invoke()
        val firstResult = result.first()

        // Then
        assertTrue(firstResult is HoldingsResult.Error)
        val errorResult = firstResult as HoldingsResult.Error
        assertEquals(mockException, errorResult.exception)
        assertFalse(errorResult.hasCachedData)
        verify(mockHoldingsRepository, times(1)).getHoldingsFlow()
    }

    @Test
    fun `invoke should return error flow with cached data`() = runTest {
        // Given
        val mockException = Exception("Network error")
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Error(
                exception = mockException,
                hasCachedData = true
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        val result = getUserHoldingsUseCase.invoke()
        val firstResult = result.first()

        // Then
        assertTrue(firstResult is HoldingsResult.Error)
        val errorResult = firstResult as HoldingsResult.Error
        assertEquals(mockException, errorResult.exception)
        assertTrue(errorResult.hasCachedData)
        verify(mockHoldingsRepository, times(1)).getHoldingsFlow()
    }

    @Test
    fun `invoke should handle multiple calls correctly`() = runTest {
        // Given
        val mockFlow: Flow<HoldingsResult> = flowOf(
            HoldingsResult.Success(
                holdings = createMockHoldingsResponse(),
                isFromCache = true
            )
        )
        whenever(mockHoldingsRepository.getHoldingsFlow()).thenReturn(mockFlow)

        // When
        getUserHoldingsUseCase.invoke()
        getUserHoldingsUseCase.invoke()
        getUserHoldingsUseCase.invoke()

        // Then
        verify(mockHoldingsRepository, times(3)).getHoldingsFlow()
    }

    // Helper method to create mock holdings response
    private fun createMockHoldingsResponse(): HoldingsResponse {
        val userHolding = UserHolding(
            symbol = "AAPL",
            avgPrice = 150.0,
            close = 155.0,
            ltp = 155.0,
            quantity = 100
        )
        val data = Data(userHolding = listOf(userHolding))
        return HoldingsResponse(data = data)
    }
} 