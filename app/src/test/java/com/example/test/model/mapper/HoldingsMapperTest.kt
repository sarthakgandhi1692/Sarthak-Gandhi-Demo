package com.example.test.model.mapper

import com.example.test.model.local.HoldingsEntity
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import org.junit.Assert.*
import org.junit.Test

class HoldingsMapperTest {

    private val testUserHolding = UserHolding(
        avgPrice = 150.50,
        close = 155.00,
        ltp = 156.75,
        quantity = 10,
        symbol = "AAPL"
    )

    private val testEntity = HoldingsEntity(
        avgPrice = 150.50,
        close = 155.00,
        ltp = 156.75,
        quantity = 10,
        symbol = "AAPL"
    )

    @Test
    fun `test mapToEntities converts HoldingsResponse to list of HoldingsEntity`() {
        // Given
        val holdingsResponse = HoldingsResponse(
            data = Data(
                userHolding = listOf(testUserHolding)
            )
        )

        // When
        val result = HoldingsMapper.mapToEntities(holdingsResponse)

        // Then
        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(testUserHolding.symbol, symbol)
            assertEquals(testUserHolding.avgPrice, avgPrice, 0.001)
            assertEquals(testUserHolding.close, close, 0.001)
            assertEquals(testUserHolding.ltp, ltp, 0.001)
            assertEquals(testUserHolding.quantity, quantity)
        }
    }

    @Test
    fun `test mapToEntities with empty holdings list`() {
        // Given
        val emptyHoldingsResponse = HoldingsResponse(
            data = Data(
                userHolding = emptyList()
            )
        )

        // When
        val result = HoldingsMapper.mapToEntities(emptyHoldingsResponse)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test mapToUserHoldings converts list of HoldingsEntity to list of UserHolding`() {
        // Given
        val entities = listOf(testEntity)

        // When
        val result = HoldingsMapper.mapToUserHoldings(entities)

        // Then
        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(testEntity.symbol, symbol)
            assertEquals(testEntity.avgPrice, avgPrice, 0.001)
            assertEquals(testEntity.close, close, 0.001)
            assertEquals(testEntity.ltp, ltp, 0.001)
            assertEquals(testEntity.quantity, quantity)
        }
    }

    @Test
    fun `test mapToUserHoldings with empty entities list`() {
        // Given
        val emptyEntities = emptyList<HoldingsEntity>()

        // When
        val result = HoldingsMapper.mapToUserHoldings(emptyEntities)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test mapToHoldingsResponse converts list of HoldingsEntity to HoldingsResponse`() {
        // Given
        val entities = listOf(testEntity)

        // When
        val result = HoldingsMapper.mapToHoldingsResponse(entities)

        // Then
        assertNotNull(result.data)
        assertEquals(1, result.data.userHolding.size)
        
        with(result.data.userHolding.first()) {
            assertEquals(testEntity.symbol, symbol)
            assertEquals(testEntity.avgPrice, avgPrice, 0.001)
            assertEquals(testEntity.close, close, 0.001)
            assertEquals(testEntity.ltp, ltp, 0.001)
            assertEquals(testEntity.quantity, quantity)
        }
    }

    @Test
    fun `test mapToHoldingsResponse with empty entities list`() {
        // Given
        val emptyEntities = emptyList<HoldingsEntity>()

        // When
        val result = HoldingsMapper.mapToHoldingsResponse(emptyEntities)

        // Then
        assertNotNull(result.data)
        assertTrue(result.data.userHolding.isEmpty())
    }

    @Test
    fun `test multiple holdings are mapped correctly`() {
        // Given
        val holdings = listOf(
            UserHolding(avgPrice = 150.50, close = 155.00, ltp = 156.75, quantity = 10, symbol = "AAPL"),
            UserHolding(avgPrice = 2500.75, close = 2600.00, ltp = 2590.25, quantity = 5, symbol = "GOOGL")
        )
        val holdingsResponse = HoldingsResponse(Data(holdings))

        // When
        val entities = HoldingsMapper.mapToEntities(holdingsResponse)
        val mappedBack = HoldingsMapper.mapToHoldingsResponse(entities)

        // Then
        assertEquals(2, entities.size)
        assertEquals(2, mappedBack.data.userHolding.size)
        assertEquals(holdings, mappedBack.data.userHolding)
    }
} 