package com.example.test.model.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HoldingsResponse(
    @param:Json(name = "data")
    val data: Data
)

@JsonClass(generateAdapter = true)
data class Data(
    @param:Json(name = "userHolding")
    val userHolding: List<UserHolding>
)

@JsonClass(generateAdapter = true)
data class UserHolding(
    @param:Json(name = "avgPrice")
    val avgPrice: Double,
    @param:Json(name = "close")
    val close: Double,
    @param:Json(name = "ltp")
    val ltp: Double,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "symbol")
    val symbol: String
)


