package com.example.test.domain

import com.example.test.model.repository.HoldingsRepository
import com.example.test.model.repository.HoldingsResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that provides access to user holdings data as a Flow.
 * Implements the operator invoke pattern for clean architecture.
 *
 * @property holdingsRepository Repository that manages holdings data
 */
class GetUserHoldingsUseCase @Inject constructor(
    private val holdingsRepository: HoldingsRepository
) {

    /**
     * Retrieves a Flow of holdings data from the repository.
     * 
     * @return [Flow] of [HoldingsResult] containing the holdings data or error state
     */
    operator fun invoke(): Flow<HoldingsResult> {
        return holdingsRepository.getHoldingsFlow()
    }
}