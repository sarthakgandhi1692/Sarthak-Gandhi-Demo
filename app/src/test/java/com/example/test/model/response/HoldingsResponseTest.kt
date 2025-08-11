package com.example.test.model.response

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HoldingsResponseTest {
    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder().build()
    }

    @Test
    fun `test successful parsing of holdings response`() {
        val json = """
            {
                "data": {
                    "userHolding": [
                        {
                            "avgPrice": 100.50,
                            "close": 102.75,
                            "ltp": 103.25,
                            "quantity": 10,
                            "symbol": "AAPL"
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(HoldingsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(1, response?.data?.userHolding?.size)
        
        val holding = response?.data?.userHolding?.first()
        assertEquals(100.50, holding?.avgPrice)
        assertEquals(102.75, holding?.close)
        assertEquals(103.25, holding?.ltp)
        assertEquals(10, holding?.quantity)
        assertEquals("AAPL", holding?.symbol)
    }

    @Test
    fun `test parsing of empty holdings list`() {
        val json = """
            {
                "data": {
                    "userHolding": []
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(HoldingsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertTrue(response?.data?.userHolding?.isEmpty() == true)
    }

    @Test
    fun `test parsing of multiple holdings`() {
        val json = """
            {
                "data": {
                    "userHolding": [
                        {
                            "avgPrice": 100.50,
                            "close": 102.75,
                            "ltp": 103.25,
                            "quantity": 10,
                            "symbol": "AAPL"
                        },
                        {
                            "avgPrice": 50.25,
                            "close": 51.00,
                            "ltp": 51.50,
                            "quantity": 20,
                            "symbol": "MSFT"
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(HoldingsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(2, response?.data?.userHolding?.size)
        
        val firstHolding = response?.data?.userHolding?.get(0)
        assertEquals(100.50, firstHolding?.avgPrice)
        assertEquals("AAPL", firstHolding?.symbol)
        
        val secondHolding = response?.data?.userHolding?.get(1)
        assertEquals(50.25, secondHolding?.avgPrice)
        assertEquals("MSFT", secondHolding?.symbol)
    }

    @Test(expected = JsonDataException::class)
    fun `test parsing of invalid json throws exception`() {
        val json = """
            {
                "data": {
                    "userHolding": [
                        {
                            "avgPrice": "invalid",
                            "close": 102.75,
                            "ltp": 103.25,
                            "quantity": 10,
                            "symbol": "AAPL"
                        }
                    ]
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(HoldingsResponse::class.java)
        adapter.fromJson(json)
    }
} 