package com.example.test.ui.screens.holdingsScreen

import com.example.test.domain.GetUserHoldingsUseCase
import com.example.test.domain.RefreshHoldingsUseCase
import com.example.test.model.repository.HoldingsResult
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HoldingsViewModelTest {

    private lateinit var viewModel: HoldingsViewModel
    private lateinit var getUserHoldingsUseCase: GetUserHoldingsUseCase
    private lateinit var refreshHoldingsUseCase: RefreshHoldingsUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testHoldings = listOf(
        UserHolding(
            symbol = "AAPL",
            avgPrice = 150.0,
            close = 160.0,
            ltp = 165.0,
            quantity = 10
        ),
        UserHolding(
            symbol = "GOOGL",
            avgPrice = 2500.0,
            close = 2600.0,
            ltp = 2550.0,
            quantity = 5
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUserHoldingsUseCase = mock()
        refreshHoldingsUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HoldingsViewModel(
        getUserHoldingsUseCase = getUserHoldingsUseCase,
        refreshHoldingsUseCase = refreshHoldingsUseCase,
        dispatcher = testDispatcher
    )

    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        val delayedFlow = flow {
            delay(100) // Delay the emission
            emit(HoldingsResult.Success(HoldingsResponse(Data(testHoldings)), isFromCache = false))
        }
        whenever(getUserHoldingsUseCase()).thenReturn(delayedFlow)

        // When
        viewModel = createViewModel()

        // Then
        assertEquals(HoldingsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadHoldings transitions from Loading to Success`() = runTest {
        // Given
        val holdingsFlow = MutableStateFlow<HoldingsResult>(
            HoldingsResult.Success(HoldingsResponse(Data(testHoldings)), isFromCache = false)
        )
        whenever(getUserHoldingsUseCase()).thenReturn(holdingsFlow)

        // When
        viewModel = createViewModel()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Success)
    }

    @Test
    fun `loadHoldings success updates state with correct holdings and metrics`() = runTest {
        // Given
        val successResponse = HoldingsResult.Success(
            HoldingsResponse(Data(testHoldings)),
            isFromCache = false
        )
        whenever(getUserHoldingsUseCase()).thenReturn(flowOf(successResponse))

        // When
        viewModel = createViewModel()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Success)
        
        if (currentState is HoldingsUiState.Success) {
            assertEquals(testHoldings, currentState.holdings.data.userHolding)
            
            with(currentState.pnlMetrics) {
                // AAPL: (165 - 150) * 10 = 150
                // GOOGL: (2550 - 2500) * 5 = 250
                assertEquals(400.0, totalPnL, 0.01)
                
                // AAPL: 150 * 10 = 1500
                // GOOGL: 2500 * 5 = 12500
                assertEquals(14000.0, totalInvestment, 0.01)
                
                // AAPL: 165 * 10 = 1650
                // GOOGL: 2550 * 5 = 12750
                assertEquals(14400.0, currentValue, 0.01)
                
                // (400 / 14000) * 100
                assertEquals(2.857, pnlPercentage, 0.001)
                
                // AAPL: (160 - 165) * 10 = -50
                // GOOGL: (2600 - 2550) * 5 = 250
                assertEquals(200.0, todaysPnL, 0.01)
            }
        }
    }

    @Test
    fun `loadHoldings error without cache transitions from Loading to Error`() = runTest {
        // Given
        val errorResponse = HoldingsResult.Error(Exception("Network error"), hasCachedData = false)
        whenever(getUserHoldingsUseCase()).thenReturn(flowOf(errorResponse))

        // When
        viewModel = createViewModel()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Error)
        if (currentState is HoldingsUiState.Error) {
            assertEquals("Network error", currentState.message)
        }
    }

    @Test
    fun `loadHoldings error with cache maintains Success state`() = runTest {
        // Given
        val holdingsFlow = MutableStateFlow<HoldingsResult>(
            HoldingsResult.Success(HoldingsResponse(Data(testHoldings)), isFromCache = true)
        )
        whenever(getUserHoldingsUseCase()).thenReturn(holdingsFlow)

        // When - Initialize and wait for first emission
        viewModel = createViewModel()

        // Then - Verify initial success state
        assertTrue(viewModel.uiState.value is HoldingsUiState.Success)

        // When - Update flow with error
        holdingsFlow.value = HoldingsResult.Error(Exception("Network error"), hasCachedData = true)

        // Then - State should still be Success
        assertTrue(viewModel.uiState.value is HoldingsUiState.Success)
    }

    @Test
    fun `refreshHoldings shows Loading state before completion`() = runTest {
        // Given
        val holdingsFlow = MutableStateFlow<HoldingsResult>(
            HoldingsResult.Success(HoldingsResponse(Data(testHoldings)), isFromCache = false)
        )
        whenever(getUserHoldingsUseCase()).thenReturn(holdingsFlow)
        
        // Initialize and wait for initial state
        viewModel = createViewModel()

        // Capture the current state
        val initialState = viewModel.uiState.value

        // When - Start refresh
        viewModel.refreshHoldings()
        
        // Then - verify Loading state
        assertEquals(HoldingsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `refreshHoldings error transitions from Loading to Error`() = runTest {
        // Given
        val successResponse = HoldingsResult.Success(
            HoldingsResponse(Data(testHoldings)),
            isFromCache = false
        )
        whenever(getUserHoldingsUseCase()).thenReturn(flowOf(successResponse))
        whenever(refreshHoldingsUseCase()).thenThrow(RuntimeException("Refresh failed"))

        viewModel = createViewModel()

        // When
        viewModel.refreshHoldings()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Error)
        if (currentState is HoldingsUiState.Error) {
            assertEquals("Refresh failed", currentState.message)
        }
    }

    @Test
    fun `calculatePnLMetrics handles empty holdings list correctly`() = runTest {
        // Given
        val emptyResponse = HoldingsResult.Success(
            HoldingsResponse(Data(emptyList())),
            isFromCache = false
        )
        whenever(getUserHoldingsUseCase()).thenReturn(flowOf(emptyResponse))

        // When
        viewModel = createViewModel()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Success)
        if (currentState is HoldingsUiState.Success) {
            with(currentState.pnlMetrics) {
                assertEquals(0.0, totalPnL, 0.01)
                assertEquals(0.0, totalInvestment, 0.01)
                assertEquals(0.0, currentValue, 0.01)
                assertEquals(0.0, pnlPercentage, 0.01)
                assertEquals(0.0, todaysPnL, 0.01)
            }
        }
    }

    @Test
    fun `calculatePnLMetrics handles negative PnL values correctly`() = runTest {
        // Given
        val holdingsWithLoss = listOf(
            UserHolding(
                symbol = "AAPL",
                avgPrice = 180.0,  // Bought high
                close = 170.0,
                ltp = 165.0,       // Current price lower
                quantity = 10
            )
        )
        val response = HoldingsResult.Success(
            HoldingsResponse(Data(holdingsWithLoss)),
            isFromCache = false
        )
        whenever(getUserHoldingsUseCase()).thenReturn(flowOf(response))

        // When
        viewModel = createViewModel()

        // Then
        val currentState = viewModel.uiState.value
        assertTrue(currentState is HoldingsUiState.Success)
        if (currentState is HoldingsUiState.Success) {
            with(currentState.pnlMetrics) {
                // Total PnL: (165 - 180) * 10 = -150
                assertEquals(-150.0, totalPnL, 0.01)
                // Total Investment: 180 * 10 = 1800
                assertEquals(1800.0, totalInvestment, 0.01)
                // Current Value: 165 * 10 = 1650
                assertEquals(1650.0, currentValue, 0.01)
                // PnL Percentage: (-150 / 1800) * 100 = -8.333
                assertEquals(-8.333, pnlPercentage, 0.001)
                // Today's PnL: (170 - 165) * 10 = 50
                assertEquals(50.0, todaysPnL, 0.01)
            }
        }
    }
} 