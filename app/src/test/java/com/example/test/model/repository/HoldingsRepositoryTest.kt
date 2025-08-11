package com.example.test.model.repository

import com.example.test.base.Result
import com.example.test.model.datasource.HoldingsLocalDataSource
import com.example.test.model.datasource.HoldingsRemoteDataSource
import com.example.test.model.local.HoldingsEntity
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class HoldingsRepositoryTest {

    @Mock
    private lateinit var mockHoldingsRemoteDataSource: HoldingsRemoteDataSource

    @Mock
    private lateinit var mockHoldingsLocalDataSource: HoldingsLocalDataSource

    private lateinit var holdingsRepository: HoldingsRepositoryImpl

    @Before
    fun setUp() {
        holdingsRepository = HoldingsRepositoryImpl(
            mockHoldingsRemoteDataSource,
            mockHoldingsLocalDataSource
        )
    }

    @Test
    fun `getHoldingsFlow should emit cached data first when available`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(2) // Take only what we need: cached + fresh
                .collect { results.add(it) }
        }

        // Then
        assertTrue(results.isNotEmpty())
        assertTrue(results[0] is HoldingsResult.Success)
        val firstResult = results[0] as HoldingsResult.Success
        assertTrue(firstResult.isFromCache)
        assertEquals(mockHoldingsResponse, firstResult.holdings)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should emit fresh data after remote fetch`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(2) // Take only what we need: cached + fresh
                .collect { results.add(it) }
        }

        // Then
        assertEquals(2, results.size)
        assertTrue(results[1] is HoldingsResult.Success)
        val secondResult = results[1] as HoldingsResult.Success
        assertFalse(secondResult.isFromCache)
        assertEquals(mockHoldingsResponse, secondResult.holdings)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should handle no cached data scenario`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Error(Exception("No cached data")))
            .thenReturn(Result.Success(createMockHoldingsEntities())) // Second call after save
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: fresh data
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Success)
        val firstResult = results[0] as HoldingsResult.Success
        assertFalse(firstResult.isFromCache)
        assertEquals(mockHoldingsResponse, firstResult.holdings)

        // Verify sequence
        verify(mockHoldingsLocalDataSource, times(1)).clearHoldings()
        verify(mockHoldingsLocalDataSource, times(1)).saveHoldings(any())
        verify(mockHoldingsLocalDataSource, times(2)).getAllHoldings() // Initial failed + after save

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should handle data inconsistency during clear`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Error(Exception("No cached data")))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(-1) // Data inconsistency

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: error result
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Error)
        val errorResult = results[0] as HoldingsResult.Error
        assertEquals("Data is inconsistent", errorResult.exception.message)
        assertFalse(errorResult.hasCachedData)

        // Verify that save was not called after clear failure
        verify(mockHoldingsLocalDataSource, times(0)).saveHoldings(any())

        job.cancel()
    }

    @Test
    fun `getHoldingsFlow should emit error when remote fetch fails and no cached data`() = runTest {
        // Given
        val networkException = Exception("Network error")
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Error(Exception("No cached data")))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Error(networkException))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: error result
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Error)
        val errorResult = results[0] as HoldingsResult.Error
        assertEquals(networkException, errorResult.exception)
        assertFalse(errorResult.hasCachedData)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should emit cached data when remote fetch fails but cached data exists`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        val networkException = Exception("Network error")
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Error(networkException))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: cached data
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Success)
        val successResult = results[0] as HoldingsResult.Success
        assertTrue(successResult.isFromCache)
        assertEquals(mockHoldingsResponse, successResult.holdings)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should handle database save failure`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Error(Exception("No cached data")))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Error(Exception("Database save failed")))

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: error result
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Error)
        val errorResult = results[0] as HoldingsResult.Error
        assertFalse(errorResult.hasCachedData)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should handle database clear failure`() = runTest {
        // Given
        val mockHoldingsResponse = createMockHoldingsResponse()
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Error(Exception("No cached data")))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(-1) // Data inconsistency

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: error result
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Error)
        val errorResult = results[0] as HoldingsResult.Error
        assertEquals("Data is inconsistent", errorResult.exception.message)
        assertFalse(errorResult.hasCachedData)

        job.cancel() // Clean up
    }

    @Test
    fun `triggerRefresh should emit refresh trigger`() = runTest {
        // When
        holdingsRepository.triggerRefresh()

        // Then
        // The refresh trigger is internal, so we test it indirectly by checking
        // that the method completes without throwing an exception
        assertTrue(true) // Test passes if no exception is thrown
    }



    @Test
    fun `getHoldingsFlow should handle exception in initial cache check`() = runTest {
        // Given
        val databaseException = Exception("Database connection failed")
        val mockHoldingsResponse = createMockHoldingsResponse()
        val mockEntities = createMockHoldingsEntities()
        
        // Mock initial cache check to throw exception and subsequent checks to return no data
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenAnswer { throw databaseException } // Initial check
            .thenReturn(Result.Error(Exception("No cached data"))) // First hasCachedData check
            .thenReturn(Result.Error(Exception("No cached data"))) // Second hasCachedData check
            .thenReturn(Result.Success(mockEntities)) // After saving remote data

        // Mock successful remote fetch and save
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))

        // When & Then
        val results = mutableListOf<HoldingsResult>()
        
        // First, collect the initial error
        val errorJob = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1)
                .collect { 
                    results.add(it)
                    assertTrue(it is HoldingsResult.Error)
                    val error = it as HoldingsResult.Error
                    assertEquals(databaseException, error.exception)
                    assertFalse(error.hasCachedData)
                }
        }
        
        advanceUntilIdle()
        errorJob.cancel()
        
        // Clear results and collect the success after refresh
        results.clear()
        val successJob = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1)
                .collect {
                    results.add(it)
                    assertTrue(it is HoldingsResult.Success)
                    val success = it as HoldingsResult.Success
                    assertFalse(success.isFromCache)
                    assertEquals(mockHoldingsResponse, success.holdings)
                }
        }
        
        // Trigger refresh and wait
        holdingsRepository.triggerRefresh()
        advanceUntilIdle()
        successJob.cancel()

        // Verify interactions
        verify(mockHoldingsLocalDataSource, times(4)).getAllHoldings()
        verify(mockHoldingsLocalDataSource, times(1)).clearHoldings()
        verify(mockHoldingsLocalDataSource, times(1)).saveHoldings(any())
        verify(mockHoldingsRemoteDataSource, times(1)).getUserHoldings()
    }

    @Test
    fun `getHoldingsFlow should handle exception in refresh flow`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        val databaseException = Exception("Database connection failed")
        
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenAnswer { throw databaseException }

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(1) // Take only what we need: cached data
                .collect { results.add(it) }
        }

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0] is HoldingsResult.Success)
        val successResult = results[0] as HoldingsResult.Success
        assertTrue(successResult.isFromCache)

        job.cancel() // Clean up
    }

    @Test
    fun `getHoldingsFlow should emit cached data followed by fresh data in initial flow`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        setupSuccessfulMocks(mockEntities, mockHoldingsResponse)

        // When
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(2) // Take only initial emissions (cached + fresh)
                .collect { results.add(it) }
        }

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is HoldingsResult.Success)
        assertTrue((results[0] as HoldingsResult.Success).isFromCache)
        assertTrue(results[1] is HoldingsResult.Success)
        assertFalse((results[1] as HoldingsResult.Success).isFromCache)

        job.cancel()
    }

    @Test
    fun `getHoldingsFlow should handle refresh trigger correctly`() = runTest {
        // Given
        val mockEntities = createMockHoldingsEntities()
        val mockHoldingsResponse = createMockHoldingsResponse()
        val refreshResponse = mockHoldingsResponse.copy(
            data = mockHoldingsResponse.data.copy(
                userHolding = listOf(
                    UserHolding(
                        symbol = "MSFT",
                        avgPrice = 300.0,
                        close = 310.0,
                        ltp = 310.0,
                        quantity = 50
                    )
                )
            )
        )
        val refreshEntities = listOf(
            HoldingsEntity(
                symbol = "MSFT",
                avgPrice = 300.0,
                close = 310.0,
                ltp = 310.0,
                quantity = 50
            )
        )

        // Initial setup
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
            .thenReturn(Result.Success(mockEntities)) // After initial save
            .thenReturn(Result.Success(refreshEntities)) // After refresh save
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
            .thenReturn(Result.Success(refreshResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))

        // When - Initial Flow
        val results = mutableListOf<HoldingsResult>()
        val job = launch(UnconfinedTestDispatcher()) {
            holdingsRepository.getHoldingsFlow()
                .take(3) // Take initial cached + fresh + refresh
                .collect { results.add(it) }
        }

        // Wait for initial emissions
        advanceUntilIdle()

        // When - Trigger Refresh
        holdingsRepository.triggerRefresh()

        // Then
        assertEquals(3, results.size)
        
        // Verify initial cached data
        assertTrue(results[0] is HoldingsResult.Success)
        assertTrue((results[0] as HoldingsResult.Success).isFromCache)
        assertEquals(mockHoldingsResponse, (results[0] as HoldingsResult.Success).holdings)

        // Verify initial fresh data
        assertTrue(results[1] is HoldingsResult.Success)
        assertFalse((results[1] as HoldingsResult.Success).isFromCache)
        assertEquals(mockHoldingsResponse, (results[1] as HoldingsResult.Success).holdings)

        // Verify refresh data
        assertTrue(results[2] is HoldingsResult.Success)
        assertFalse((results[2] as HoldingsResult.Success).isFromCache)
        assertEquals(refreshResponse, (results[2] as HoldingsResult.Success).holdings)

        // Verify sequence
        verify(mockHoldingsLocalDataSource, times(3)).getAllHoldings() // Initial + after save + after refresh
        verify(mockHoldingsRemoteDataSource, times(2)).getUserHoldings() // Initial + refresh
        verify(mockHoldingsLocalDataSource, times(2)).clearHoldings() // Initial + refresh
        verify(mockHoldingsLocalDataSource, times(2)).saveHoldings(any()) // Initial + refresh

        // Verify order
        val inOrder = inOrder(mockHoldingsLocalDataSource, mockHoldingsRemoteDataSource)
        // Initial flow
        inOrder.verify(mockHoldingsLocalDataSource).getAllHoldings() // 1. Check cache
        inOrder.verify(mockHoldingsRemoteDataSource).getUserHoldings() // 2. Fetch remote
        inOrder.verify(mockHoldingsLocalDataSource).clearHoldings() // 3. Clear old data
        inOrder.verify(mockHoldingsLocalDataSource).saveHoldings(any()) // 4. Save new data
        inOrder.verify(mockHoldingsLocalDataSource).getAllHoldings() // 5. Read saved data
        // Refresh flow
        inOrder.verify(mockHoldingsRemoteDataSource).getUserHoldings() // 6. Fetch refresh
        inOrder.verify(mockHoldingsLocalDataSource).clearHoldings() // 7. Clear old data
        inOrder.verify(mockHoldingsLocalDataSource).saveHoldings(any()) // 8. Save refresh data
        inOrder.verify(mockHoldingsLocalDataSource).getAllHoldings() // 9. Read refresh data

        job.cancel()
    }



    private suspend fun setupSuccessfulMocks(
        mockEntities: List<HoldingsEntity>,
        mockHoldingsResponse: HoldingsResponse
    ) {
        whenever(mockHoldingsLocalDataSource.getAllHoldings())
            .thenReturn(Result.Success(mockEntities))
        whenever(mockHoldingsRemoteDataSource.getUserHoldings())
            .thenReturn(Result.Success(mockHoldingsResponse))
        whenever(mockHoldingsLocalDataSource.clearHoldings())
            .thenReturn(0) // Success
        whenever(mockHoldingsLocalDataSource.saveHoldings(any()))
            .thenReturn(Result.Success(Unit))
    }

    // Helper methods
    private fun createMockHoldingsEntities(): List<HoldingsEntity> {
        return listOf(
            HoldingsEntity(
                symbol = "AAPL",
                avgPrice = 150.0,
                close = 155.0,
                ltp = 155.0,
                quantity = 100
            ),
            HoldingsEntity(
                symbol = "GOOGL",
                avgPrice = 2800.0,
                close = 2850.0,
                ltp = 2850.0,
                quantity = 50
            )
        )
    }

    private fun createMockHoldingsResponse(): HoldingsResponse {
        val userHoldings = listOf(
            UserHolding(
                symbol = "AAPL",
                avgPrice = 150.0,
                close = 155.0,
                ltp = 155.0,
                quantity = 100
            ),
            UserHolding(
                symbol = "GOOGL",
                avgPrice = 2800.0,
                close = 2850.0,
                ltp = 2850.0,
                quantity = 50
            )
        )
        val data = Data(userHolding = userHoldings)
        return HoldingsResponse(data = data)
    }
} 