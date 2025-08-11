package com.example.test.model.datasource

import com.example.test.base.Result
import com.example.test.model.local.HoldingsDao
import com.example.test.model.local.HoldingsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class HoldingsLocalDataSourceTest {

    @Mock
    private lateinit var mockHoldingsDao: HoldingsDao

    private lateinit var holdingsLocalDataSource: HoldingsLocalDataSourceImpl

    @Before
    fun setUp() {
        holdingsLocalDataSource = HoldingsLocalDataSourceImpl(mockHoldingsDao)
    }

    @Test
    fun `getAllHoldings should return success when holdings exist`() = runTest {
        // Given
        val mockHoldings = createMockHoldingsEntities()
        whenever(mockHoldingsDao.getAllHoldings()).thenReturn(mockHoldings)

        // When
        val result = holdingsLocalDataSource.getAllHoldings()

        // Then
        assertTrue(result is Result.Success)
        val successResult = result as Result.Success
        assertEquals(mockHoldings, successResult.data)
        verify(mockHoldingsDao, times(1)).getAllHoldings()
    }

    @Test
    fun `getAllHoldings should return error when no holdings found`() = runTest {
        // Given
        whenever(mockHoldingsDao.getAllHoldings()).thenReturn(emptyList())

        // When
        val result = holdingsLocalDataSource.getAllHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("No cached holdings found", errorResult.exception.message)
        verify(mockHoldingsDao, times(1)).getAllHoldings()
    }

    @Test
    fun `getAllHoldings should return error when holdings is null`() = runTest {
        // Given
        whenever(mockHoldingsDao.getAllHoldings()).thenReturn(null)

        // When
        val result = holdingsLocalDataSource.getAllHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("No cached holdings found", errorResult.exception.message)
        verify(mockHoldingsDao, times(1)).getAllHoldings()
    }

    @Test
    fun `getAllHoldings should return error when DAO throws exception`() = runTest {
        // Given
        val databaseException = Exception("Database connection failed")
        whenever(mockHoldingsDao.getAllHoldings()).thenAnswer { throw databaseException }

        // When
        val result = holdingsLocalDataSource.getAllHoldings()

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals(databaseException, errorResult.exception)
        verify(mockHoldingsDao, times(1)).getAllHoldings()
    }

    @Test
    fun `saveHoldings should return success when insertion succeeds`() = runTest {
        // Given
        val mockHoldings = createMockHoldingsEntities()
        whenever(mockHoldingsDao.insertHoldings(any())).thenReturn(Unit)

        // When
        val result = holdingsLocalDataSource.saveHoldings(mockHoldings)

        // Then
        assertTrue(result is Result.Success)
        val successResult = result as Result.Success
        assertEquals(Unit, successResult.data)
        verify(mockHoldingsDao, times(1)).insertHoldings(mockHoldings)
    }

    @Test
    fun `saveHoldings should return error when insertion fails`() = runTest {
        // Given
        val mockHoldings = createMockHoldingsEntities()
        val databaseException = Exception("Insert failed")
        whenever(mockHoldingsDao.insertHoldings(any())).thenAnswer { throw databaseException }

        // When
        val result = holdingsLocalDataSource.saveHoldings(mockHoldings)

        // Then
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals(databaseException, errorResult.exception)
        verify(mockHoldingsDao, times(1)).insertHoldings(mockHoldings)
    }

    @Test
    fun `clearHoldings should return number of rows deleted when clearing succeeds`() = runTest {
        // Given
        val rowsDeleted = 5
        whenever(mockHoldingsDao.clearAllHoldings()).thenReturn(rowsDeleted)

        // When
        val result = holdingsLocalDataSource.clearHoldings()

        // Then
        assertEquals(rowsDeleted, result)
        verify(mockHoldingsDao, times(1)).clearAllHoldings()
    }

    @Test
    fun `clearHoldings should return zero when no rows were deleted`() = runTest {
        // Given
        whenever(mockHoldingsDao.clearAllHoldings()).thenReturn(0)

        // When
        val result = holdingsLocalDataSource.clearHoldings()

        // Then
        assertEquals(0, result)
        verify(mockHoldingsDao, times(1)).clearAllHoldings()
    }

    @Test
    fun `clearHoldings should return -1 when clearing fails`() = runTest {
        // Given
        val databaseException = Exception("Clear failed")
        whenever(mockHoldingsDao.clearAllHoldings()).thenAnswer { throw databaseException }

        // When
        val result = holdingsLocalDataSource.clearHoldings()

        // Then
        assertEquals(-1, result)
        verify(mockHoldingsDao, times(1)).clearAllHoldings()
    }

    @Test
    fun `hasHoldings should return true when holdings exist`() = runTest {
        // Given
        whenever(mockHoldingsDao.getHoldingsCount()).thenReturn(5)

        // When
        val result = holdingsLocalDataSource.hasHoldings()

        // Then
        assertTrue(result)
        verify(mockHoldingsDao, times(1)).getHoldingsCount()
    }

    @Test
    fun `hasHoldings should return false when no holdings exist`() = runTest {
        // Given
        whenever(mockHoldingsDao.getHoldingsCount()).thenReturn(0)

        // When
        val result = holdingsLocalDataSource.hasHoldings()

        // Then
        assertFalse(result)
        verify(mockHoldingsDao, times(1)).getHoldingsCount()
    }

    @Test
    fun `hasHoldings should return false when DAO throws exception`() = runTest {
        // Given
        val databaseException = Exception("Count query failed")
        whenever(mockHoldingsDao.getHoldingsCount()).thenAnswer { throw databaseException }

        // When
        val result = holdingsLocalDataSource.hasHoldings()

        // Then
        assertFalse(result)
        verify(mockHoldingsDao, times(1)).getHoldingsCount()
    }

    // Helper method to create mock holdings entities
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
} 