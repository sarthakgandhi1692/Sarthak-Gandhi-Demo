package com.example.test.ui.screens.holdingsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.base.model.ErrorType
import com.example.test.di.qualifiers.DispatcherIO
import com.example.test.domain.GetUserHoldingsUseCase
import com.example.test.domain.RefreshHoldingsUseCase
import com.example.test.model.data.PnLMetrics
import com.example.test.model.repository.HoldingsResult
import com.example.test.model.response.HoldingsResponse
import com.example.test.model.response.UserHolding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * ViewModel responsible for managing user holdings data and related UI state.
 * Handles loading, refreshing, and calculating P&L metrics for holdings.
 *
 * @property getUserHoldingsUseCase Use case to retrieve holdings data
 * @property refreshHoldingsUseCase Use case to refresh holdings data
 * @property dispatcher Coroutine dispatcher for IO operations
 */
@HiltViewModel
class HoldingsViewModel
@Inject constructor(
    private val getUserHoldingsUseCase: GetUserHoldingsUseCase,
    private val refreshHoldingsUseCase: RefreshHoldingsUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<HoldingsUiState>(HoldingsUiState.Loading)
    val uiState: StateFlow<HoldingsUiState> = _uiState.asStateFlow()

    init {
        loadHoldings()
    }

    private fun getErrorType(exception: Exception): ErrorType {
        return when (exception) {
            is UnknownHostException,
            is IOException -> ErrorType.NetworkError

            else -> ErrorType.GenericError
        }
    }

    /**
     * Loads user holdings data and updates the UI state accordingly.
     * Collects holdings data flow and calculates P&L metrics on success.
     */
    fun loadHoldings() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { HoldingsUiState.Loading }

            getUserHoldingsUseCase().collect { result ->
                when (result) {
                    is HoldingsResult.Success -> {
                        // Calculate P&L metrics
                        val pnlMetrics = calculatePnLMetrics(result.holdings.data.userHolding)

                        _uiState.update {
                            HoldingsUiState.Success(
                                holdings = result.holdings,
                                pnlMetrics = pnlMetrics
                            )
                        }
                    }

                    is HoldingsResult.Error -> {
                        if (!result.hasCachedData) {
                            val errorType = getErrorType(result.exception)
                            _uiState.update {
                                HoldingsUiState.Error(errorType)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Triggers a refresh of holdings data from the remote source.
     * Updates UI state to loading during refresh and handles any errors.
     */
    fun refreshHoldings() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { HoldingsUiState.Loading }

            try {
                // Trigger refresh in repository
                refreshHoldingsUseCase()

                // The flow will automatically emit new data after refresh
            } catch (e: Exception) {
                val errorType = getErrorType(e)
                _uiState.update {
                    HoldingsUiState.Error(errorType)
                }
            }
        }
    }

    /**
     * Calculates various P&L (Profit and Loss) metrics for the given holdings.
     *
     * @param holdings List of user holdings to calculate metrics for
     * @return [PnLMetrics] containing calculated P&L values
     */
    private fun calculatePnLMetrics(holdings: List<UserHolding>): PnLMetrics {
        val totalPnL = holdings.sumOf { holding ->
            val totalValue = holding.avgPrice * holding.quantity
            val currentValue = holding.ltp * holding.quantity
            currentValue - totalValue
        }

        val totalInvestment = holdings.sumOf { it.avgPrice * it.quantity }
        val currentValue = holdings.sumOf { it.ltp * it.quantity }
        val pnlPercentage = if (totalInvestment > 0) {
            (totalPnL / totalInvestment) * 100
        } else 0.0

        // Calculate Today's P&L: sum of ((Close - LTP) * quantity) for all holdings
        val todaysPnL = holdings.sumOf { holding ->
            (holding.close - holding.ltp) * holding.quantity
        }

        return PnLMetrics(
            totalPnL = totalPnL,
            totalInvestment = totalInvestment,
            currentValue = currentValue,
            pnlPercentage = pnlPercentage,
            todaysPnL = todaysPnL
        )
    }
}

/**
 * Sealed class representing different states of the holdings screen UI.
 */
sealed class HoldingsUiState {
    /** Loading state when data is being fetched */
    object Loading : HoldingsUiState()

    /**
     * Success state containing holdings data and calculated P&L metrics
     *
     * @property holdings The holdings response data
     * @property pnlMetrics Calculated P&L metrics for the holdings
     */
    data class Success(
        val holdings: HoldingsResponse,
        val pnlMetrics: PnLMetrics
    ) : HoldingsUiState()

    /**
     * Error state when data fetching fails
     *
     * @property errorType The type of error that occurred
     */
    data class Error(val errorType: ErrorType) : HoldingsUiState()
}