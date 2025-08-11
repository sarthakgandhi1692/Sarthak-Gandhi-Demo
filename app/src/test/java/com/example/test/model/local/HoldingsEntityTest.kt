package com.example.test.model.local

import org.junit.Test
import org.junit.Assert.*

class HoldingsEntityTest {

    @Test
    fun `test HoldingsEntity creation with valid data`() {
        val holdingsEntity = HoldingsEntity(
            symbol = "AAPL",
            avgPrice = 150.50,
            close = 155.00,
            ltp = 156.75,
            quantity = 10
        )

        assertEquals("AAPL", holdingsEntity.symbol)
        assertEquals(150.50, holdingsEntity.avgPrice, 0.001)
        assertEquals(155.00, holdingsEntity.close, 0.001)
        assertEquals(156.75, holdingsEntity.ltp, 0.001)
        assertEquals(10, holdingsEntity.quantity)
    }

    @Test
    fun `test HoldingsEntity data class equality`() {
        val holdings1 = HoldingsEntity("AAPL", 150.50, 155.00, 156.75, 10)
        val holdings2 = HoldingsEntity("AAPL", 150.50, 155.00, 156.75, 10)
        val holdings3 = HoldingsEntity("GOOGL", 150.50, 155.00, 156.75, 10)

        assertEquals(holdings1, holdings2)
        assertNotEquals(holdings1, holdings3)
    }

    @Test
    fun `test HoldingsEntity copy function`() {
        val original = HoldingsEntity("AAPL", 150.50, 155.00, 156.75, 10)
        val copied = original.copy(quantity = 20)

        assertEquals(original.symbol, copied.symbol)
        assertEquals(original.avgPrice, copied.avgPrice, 0.001)
        assertEquals(original.close, copied.close, 0.001)
        assertEquals(original.ltp, copied.ltp, 0.001)
        assertEquals(20, copied.quantity)
    }

    @Test
    fun `test HoldingsEntity with zero values`() {
        val holdings = HoldingsEntity("AAPL", 0.0, 0.0, 0.0, 0)

        assertEquals("AAPL", holdings.symbol)
        assertEquals(0.0, holdings.avgPrice, 0.001)
        assertEquals(0.0, holdings.close, 0.001)
        assertEquals(0.0, holdings.ltp, 0.001)
        assertEquals(0, holdings.quantity)
    }

    @Test
    fun `test HoldingsEntity toString representation`() {
        val holdings = HoldingsEntity("AAPL", 150.50, 155.00, 156.75, 10)
        val toString = holdings.toString()

        assertTrue(toString.contains("AAPL"))
        assertTrue(toString.contains("150.5"))
        assertTrue(toString.contains("155.0"))
        assertTrue(toString.contains("156.75"))
        assertTrue(toString.contains("10"))
    }
} 