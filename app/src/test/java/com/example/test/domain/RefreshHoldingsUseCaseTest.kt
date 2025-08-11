package com.example.test.domain

import com.example.test.model.repository.HoldingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RefreshHoldingsUseCaseTest {

    @Mock
    private lateinit var mockHoldingsRepository: HoldingsRepository

    private lateinit var refreshHoldingsUseCase: RefreshHoldingsUseCase

    @Before
    fun setUp() {
        refreshHoldingsUseCase = RefreshHoldingsUseCase(mockHoldingsRepository)
    }

    @Test
    fun `invoke should call repository triggerRefresh`() = runTest {
        // Given
        whenever(mockHoldingsRepository.triggerRefresh()).thenReturn(Unit)

        // When
        refreshHoldingsUseCase.invoke()

        // Then
        verify(mockHoldingsRepository, times(1)).triggerRefresh()
    }

    @Test
    fun `invoke should handle multiple calls correctly`() = runTest {
        // Given
        whenever(mockHoldingsRepository.triggerRefresh()).thenReturn(Unit)

        // When
        refreshHoldingsUseCase.invoke()
        refreshHoldingsUseCase.invoke()
        refreshHoldingsUseCase.invoke()

        // Then
        verify(mockHoldingsRepository, times(3)).triggerRefresh()
    }

    @Test
    fun `invoke should work as suspend function`() = runTest {
        // Given
        whenever(mockHoldingsRepository.triggerRefresh()).thenReturn(Unit)

        // When
        refreshHoldingsUseCase.invoke()

        // Then
        verify(mockHoldingsRepository, times(1)).triggerRefresh()
    }
} 