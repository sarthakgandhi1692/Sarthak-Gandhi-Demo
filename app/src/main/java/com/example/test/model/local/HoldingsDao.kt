package com.example.test.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for holdings-related database operations.
 * Provides methods to interact with the holdings table in the database.
 */
@Dao
interface HoldingsDao {

    /**
     * Retrieves all holdings from the database, ordered by symbol in ascending order.
     * 
     * @return List of holdings entities, or null if no holdings exist
     */
    @Query("SELECT * FROM holdings ORDER BY symbol ASC")
    suspend fun getAllHoldings(): List<HoldingsEntity>

    /**
     * Inserts or updates a list of holdings in the database.
     * If a holding with the same primary key exists, it will be replaced.
     * 
     * @param holdings List of holdings entities to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldings(holdings: List<HoldingsEntity>)

    /**
     * Removes all holdings from the database.
     */
    @Query("DELETE FROM holdings")
    suspend fun clearAllHoldings(): Int

    /**
     * Gets the total number of holdings records in the database.
     * 
     * @return The count of holdings records
     */
    @Query("SELECT COUNT(*) FROM holdings")
    suspend fun getHoldingsCount(): Int
} 