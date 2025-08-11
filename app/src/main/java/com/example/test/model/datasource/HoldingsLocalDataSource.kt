package com.example.test.model.datasource

import com.example.test.base.Result
import com.example.test.model.local.HoldingsDao
import com.example.test.model.local.HoldingsEntity
import javax.inject.Inject

/**
 * Interface defining operations for managing holdings data in local storage.
 */
interface HoldingsLocalDataSource {
    /**
     * Retrieves the most recent holdings data from local storage.
     *
     * @return [Result] containing a list of holdings if successful, or an error if the operation fails
     */
    suspend fun getAllHoldings(): Result<List<HoldingsEntity>>

    /**
     * Saves a list of holdings to local storage.
     *
     * @param holdings The list of holdings to save
     * @return [Result] indicating success or failure of the operation
     */
    suspend fun saveHoldings(holdings: List<HoldingsEntity>): Result<Unit>

    /**
     * Removes all holdings data from local storage.
     *
     * @return [Result] indicating success or failure of the operation
     */
    suspend fun clearHoldings(): Int

    /**
     * Checks if there are any holdings stored in local storage.
     *
     * @return true if holdings exist, false otherwise
     */
    suspend fun hasHoldings(): Boolean
}

/**
 * Implementation of [HoldingsLocalDataSource] that uses Room DAO for local storage operations.
 *
 * @property holdingsDao The Data Access Object for holdings-related database operations
 */
class HoldingsLocalDataSourceImpl @Inject constructor(
    private val holdingsDao: HoldingsDao
) : HoldingsLocalDataSource {

    override suspend fun getAllHoldings(): Result<List<HoldingsEntity>> {
        return try {
            val holdings = holdingsDao.getAllHoldings()
            if (holdings.isNotEmpty()) {
                Result.Success(holdings)
            } else {
                Result.Error(Exception("No cached holdings found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveHoldings(holdings: List<HoldingsEntity>): Result<Unit> {
        return try {
            holdingsDao.insertHoldings(holdings = holdings)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun clearHoldings(): Int {
        return try {
            holdingsDao.clearAllHoldings()
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun hasHoldings(): Boolean {
        return try {
            holdingsDao.getHoldingsCount() > 0
        } catch (e: Exception) {
            false
        }
    }
} 