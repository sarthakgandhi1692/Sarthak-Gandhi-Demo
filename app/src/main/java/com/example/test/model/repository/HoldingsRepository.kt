package com.example.test.model.repository

import com.example.test.base.Result
import com.example.test.model.datasource.HoldingsLocalDataSource
import com.example.test.model.datasource.HoldingsRemoteDataSource
import com.example.test.model.mapper.HoldingsMapper
import com.example.test.model.response.HoldingsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

/**
 * Repository interface for managing user holdings data.
 * Provides methods to fetch and refresh holdings data while handling caching.
 */
interface HoldingsRepository {
    /**
     * Returns a Flow of holdings data that emits updates when data changes.
     *
     * @return Flow of [HoldingsResult] containing either success with holdings data or error state
     */
    fun getHoldingsFlow(): Flow<HoldingsResult>

    /**
     * Triggers a refresh of holdings data from the remote source.
     */
    suspend fun triggerRefresh()
}

/**
 * Sealed class representing the result of holdings data operations.
 */
sealed class HoldingsResult {
    /**
     * Successful result containing holdings data.
     *
     * @property holdings The holdings response data
     * @property isFromCache Whether the data was retrieved from cache
     */
    data class Success(
        val holdings: HoldingsResponse,
        val isFromCache: Boolean
    ) : HoldingsResult()

    /**
     * Error result when holdings data operation fails.
     *
     * @property exception The exception that caused the error
     * @property hasCachedData Whether there is cached data available despite the error
     */
    data class Error(
        val exception: Exception,
        val hasCachedData: Boolean = false
    ) : HoldingsResult()
}

/**
 * Implementation of [HoldingsRepository] that manages holdings data using local and remote data sources.
 * Implements a cache-first strategy with background refresh.
 *
 * @property holdingsRemoteDataSource Data source for remote holdings data
 * @property holdingsLocalDataSource Data source for local (cached) holdings data
 */
class HoldingsRepositoryImpl @Inject constructor(
    private val holdingsRemoteDataSource: HoldingsRemoteDataSource,
    private val holdingsLocalDataSource: HoldingsLocalDataSource
) : HoldingsRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 0)

    override fun getHoldingsFlow(): Flow<HoldingsResult> = merge(
        // Initial load flow
        flow {
            try {
                // Try to get from cache first
                val cachedResult = holdingsLocalDataSource.getAllHoldings()
                val hasCachedData = cachedResult is Result.Success && cachedResult.data.isNotEmpty()

                if (hasCachedData) {
                    val holdingsResponse = HoldingsMapper.mapToHoldingsResponse(
                        entities = cachedResult.data
                    )
                    emit(
                        HoldingsResult.Success(
                            holdings = holdingsResponse,
                            isFromCache = true
                        )
                    )
                }

                // Then fetch from remote and save to database
                fetchAndEmitRemoteData(flowScope = this)
            } catch (e: Exception) {
                handleException(
                    flowScope = this,
                    exception = e
                )
            }
        },

        // Refresh flow
        flow {
            refreshTrigger.collect {
                try {
                    fetchAndEmitRemoteData(flowScope = this)
                } catch (e: Exception) {
                    handleException(
                        flowScope = this,
                        exception = e
                    )
                }
            }
        }
    )

    override suspend fun triggerRefresh() {
        refreshTrigger.emit(Unit)
    }

    /**
     * Fetches holdings data from remote source and updates local cache.
     *
     * @param flowScope The flow collector to emit results to
     */
    private suspend fun fetchAndEmitRemoteData(
        flowScope: FlowCollector<HoldingsResult>
    ) {
        val remoteResult = holdingsRemoteDataSource.getUserHoldings()

        when (remoteResult) {
            is Result.Success -> {

                // Clear existing data first
                val clearDataSuccess = holdingsLocalDataSource.clearHoldings()
                if (clearDataSuccess == -1) {
                    flowScope.emit(
                        HoldingsResult.Error(
                            exception = Exception("Data is inconsistent"),
                            hasCachedData = false
                        )
                    )
                    return
                }

                // Always save remote data to database
                val entities = HoldingsMapper.mapToEntities(
                    holdingsResponse = remoteResult.data
                )
                holdingsLocalDataSource.saveHoldings(
                    holdings = entities
                )

                // Always read from database to emit
                val cachedResult = holdingsLocalDataSource.getAllHoldings()
                if (cachedResult is Result.Success && cachedResult.data.isNotEmpty()) {
                    val holdingsResponse =
                        HoldingsMapper.mapToHoldingsResponse(
                            entities = cachedResult.data
                        )
                    flowScope.emit(
                        HoldingsResult.Success(
                            holdings = holdingsResponse,
                            isFromCache = false
                        )
                    )
                } else {
                    flowScope.emit(
                        HoldingsResult.Error(
                            exception = Exception("Failed to read from database"),
                            hasCachedData = false
                        )
                    )
                }
            }

            is Result.Error -> {
                handleRemoteFailure(
                    flowScope = flowScope,
                    exception = remoteResult.exception
                )
            }
        }
    }

    /**
     * Handles failures when fetching from remote source.
     * Falls back to cached data if available.
     *
     * @param flowScope The flow collector to emit results to
     * @param exception The exception that caused the remote failure
     */
    private suspend fun handleRemoteFailure(
        flowScope: FlowCollector<HoldingsResult>,
        exception: Exception
    ) {
        val hasCachedData = hasCachedData()

        if (hasCachedData) {
            emitCachedData(flowScope = flowScope)
        } else {
            flowScope.emit(
                HoldingsResult.Error(
                    exception = exception,
                    hasCachedData = false
                )
            )
        }
    }

    /**
     * Handles general exceptions during data operations.
     * Emits error only if no cached data is available.
     *
     * @param flowScope The flow collector to emit results to
     * @param exception The exception to handle
     */
    private suspend fun handleException(
        flowScope: FlowCollector<HoldingsResult>,
        exception: Exception
    ) {
        val hasCachedData = hasCachedData()

        if (hasCachedData.not()) {
            flowScope.emit(
                HoldingsResult.Error(
                    exception = exception,
                    hasCachedData = false
                )
            )
        } else {
            // Exception occurred but showing cached data - no error emitted
        }
    }

    /**
     * Checks if valid cached data is available in local storage.
     *
     * @return true if valid cached data exists, false otherwise
     */
    private suspend fun hasCachedData(): Boolean {
        val cachedResult = holdingsLocalDataSource.getAllHoldings()
        return cachedResult is Result.Success && cachedResult.data.isNotEmpty()
    }

    /**
     * Emits cached holdings data to the flow.
     *
     * @param flowScope The flow collector to emit results to
     */
    private suspend fun emitCachedData(
        flowScope: FlowCollector<HoldingsResult>
    ) {
        val cachedResult = holdingsLocalDataSource.getAllHoldings()
        if (cachedResult is Result.Success && cachedResult.data.isNotEmpty()) {
            val holdingsResponse =
                HoldingsMapper.mapToHoldingsResponse(entities = cachedResult.data)
            flowScope.emit(
                HoldingsResult.Success(
                    holdings = holdingsResponse,
                    isFromCache = true
                )
            )
        }
    }
}