package com.example.test.domain

import com.example.test.model.repository.HoldingsRepository
import javax.inject.Inject

class RefreshHoldingsUseCase @Inject constructor(
    private val holdingsRepository: HoldingsRepository
) {
    suspend operator fun invoke() {
        holdingsRepository.triggerRefresh()
    }
}