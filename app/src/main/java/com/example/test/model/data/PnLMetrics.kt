package com.example.test.model.data

/**
 * Data class containing various Profit and Loss metrics for holdings.
 *
 * @property totalPnL Total profit/loss across all holdings
 * @property totalInvestment Total amount invested
 * @property currentValue Current market value of all holdings
 * @property pnlPercentage Percentage of profit/loss relative to total investment
 * @property todaysPnL Profit/loss for the current day
 */
data class PnLMetrics(
    val totalPnL: Double,
    val totalInvestment: Double,
    val currentValue: Double,
    val pnlPercentage: Double,
    val todaysPnL: Double
)