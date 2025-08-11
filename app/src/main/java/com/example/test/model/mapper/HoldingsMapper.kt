package com.example.test.model.mapper

import com.example.test.model.local.HoldingsEntity
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding

object HoldingsMapper {
    
    fun mapToEntities(holdingsResponse: HoldingsResponse): List<HoldingsEntity> {
        return holdingsResponse.data.userHolding.map { userHolding ->
            HoldingsEntity(
                avgPrice = userHolding.avgPrice,
                close = userHolding.close,
                ltp = userHolding.ltp,
                quantity = userHolding.quantity,
                symbol = userHolding.symbol
            )
        }
    }
    
    fun mapToUserHoldings(entities: List<HoldingsEntity>): List<UserHolding> {
        return entities.map { entity ->
            UserHolding(
                avgPrice = entity.avgPrice,
                close = entity.close,
                ltp = entity.ltp,
                quantity = entity.quantity,
                symbol = entity.symbol
            )
        }
    }
    
    fun mapToHoldingsResponse(entities: List<HoldingsEntity>): HoldingsResponse {
        val userHoldings = mapToUserHoldings(entities)
        val data = Data(userHolding = userHoldings)
        return HoldingsResponse(data = data)
    }
} 