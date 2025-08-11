package com.example.test.ui.screens.holdingsScreen

import com.example.test.base.model.ErrorType
import com.example.test.domain.GetUserHoldingsUseCase
import com.example.test.domain.RefreshHoldingsUseCase
import com.example.test.model.repository.HoldingsResult
import com.example.test.model.response.Data
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.doAnswer
import java.io.IOException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class HoldingsViewModelTest {

    private lateinit var viewModel: HoldingsViewModel
    private lateinit var getUserHoldingsUseCase: GetUserHoldingsUseCase
    private lateinit var refreshHoldingsUseCase: RefreshHoldingsUseCase
    private val testDispatcher = StandardTestDispatcher()

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

    @Test
    fun `loadHoldings should emit Loading and then Success state when data is fetched successfully`() =
        runTest {
            // Given
            val mockHoldings = createMockHoldingsResponse()
            getUserHoldingsUseCase = mock {
                on { invoke() } doReturn flowOf(
                    HoldingsResult.Success(
                        mockHoldings,
                        false
                    )
                )
            }
            initViewModel()

            // When
            viewModel.loadHoldings()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is HoldingsUiState.Success)
            val successState = viewModel.uiState.value as HoldingsUiState.Success
            assertEquals(mockHoldings, successState.holdings)
        }

    @Test
    fun `loadHoldings should emit NetworkError when UnknownHostException occurs`() = runTest {
        // Given
        getUserHoldingsUseCase = mock {
            on { invoke() } doReturn flowOf(
                HoldingsResult.Error(UnknownHostException(), hasCachedData = false)
            )
        }
        initViewModel()

        // When
        viewModel.loadHoldings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HoldingsUiState.Error)
        val errorState = viewModel.uiState.value as HoldingsUiState.Error
        assertTrue(errorState.errorType is ErrorType.NetworkError)
    }

    @Test
    fun `loadHoldings should emit NetworkError when IOException occurs`() = runTest {
        // Given
        getUserHoldingsUseCase = mock {
            on { invoke() } doReturn flowOf(
                HoldingsResult.Error(IOException(), hasCachedData = false)
            )
        }
        initViewModel()

        // When
        viewModel.loadHoldings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HoldingsUiState.Error)
        val errorState = viewModel.uiState.value as HoldingsUiState.Error
        assertTrue(errorState.errorType is ErrorType.NetworkError)
    }

    @Test
    fun `loadHoldings should emit GenericError for other exceptions`() = runTest {
        // Given
        getUserHoldingsUseCase = mock {
            on { invoke() } doReturn flowOf(
                HoldingsResult.Error(RuntimeException(), hasCachedData = false)
            )
        }
        initViewModel()

        // When
        viewModel.loadHoldings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HoldingsUiState.Error)
        val errorState = viewModel.uiState.value as HoldingsUiState.Error
        assertTrue(errorState.errorType is ErrorType.GenericError)
    }

    @Test
    fun `refreshHoldings should emit NetworkError when network error occurs`() = runTest {
        // Given
        getUserHoldingsUseCase = mock {
            on { invoke() } doReturn flowOf(
                HoldingsResult.Success(createMockHoldingsResponse(), false)
            )
        }
        refreshHoldingsUseCase = mock {
            onBlocking { invoke() } doAnswer { throw UnknownHostException() }
        }
        initViewModel()

        // When
        viewModel.refreshHoldings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HoldingsUiState.Error)
        val errorState = viewModel.uiState.value as HoldingsUiState.Error
        assertTrue(errorState.errorType is ErrorType.NetworkError)
    }

    @Test
    fun `refreshHoldings should emit GenericError for other exceptions`() = runTest {
        // Given
        getUserHoldingsUseCase = mock {
            on { invoke() } doReturn flowOf(
                HoldingsResult.Success(createMockHoldingsResponse(), false)
            )
        }
        refreshHoldingsUseCase = mock {
            onBlocking { invoke() } doAnswer { throw RuntimeException() }
        }
        initViewModel()

        // When
        viewModel.refreshHoldings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HoldingsUiState.Error)
        val errorState = viewModel.uiState.value as HoldingsUiState.Error
        assertTrue(errorState.errorType is ErrorType.GenericError)
    }

    private fun initViewModel() {
        viewModel = HoldingsViewModel(
            getUserHoldingsUseCase = getUserHoldingsUseCase,
            refreshHoldingsUseCase = refreshHoldingsUseCase,
            dispatcher = testDispatcher
        )
    }

    private fun createMockHoldingsResponse(): HoldingsResponse {
        val mockHolding = UserHolding(
            symbol = "AAPL",
            quantity = 10,
            avgPrice = 150.0,
            ltp = 160.0,
            close = 155.0
        )
        return HoldingsResponse(
            data = Data(
                userHolding = listOf(mockHolding)
            )
        )
    }
} 