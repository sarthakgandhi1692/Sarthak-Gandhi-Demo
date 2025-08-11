package com.example.test.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingsEntity(
    @PrimaryKey
    val symbol: String,
    val avgPrice: Double,
    val close: Double,
    val ltp: Double,
    val quantity: Int,
)