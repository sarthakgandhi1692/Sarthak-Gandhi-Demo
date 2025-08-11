package com.example.test.ui.screens.holdingsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.test.R
import com.example.test.model.data.PnLMetrics
import com.example.test.model.response.UserHolding
import com.example.test.ui.utils.getErrorMessage
import com.example.test.utils.Dimensions
import com.example.test.utils.NumberFormatter

/**
 * Main screen composable that displays user's holdings information.
 * Includes a header with refresh button, list of holdings, and a bottom P&L summary bar.
 *
 * @param viewModel ViewModel that manages the holdings data and UI state
 */
@Composable
fun HoldingsScreen(
    viewModel: HoldingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val localContext = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(vertical = Dimensions.holdingsScreenVerticalPadding)
        ) {
            // Header with refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.holdingsScreenHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.holdings_title),
                    fontSize = Dimensions.holdingsTitleFontSize,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { viewModel.refreshHoldings() },
                    enabled = uiState !is HoldingsUiState.Loading
                ) {
                    Text(stringResource(R.string.refresh_button))
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.headerSpacing))

            // Content based on UI state
            when (uiState) {
                is HoldingsUiState.Loading -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = Dimensions.listBottomPadding)
                    ) {
                        items(7) {
                            ShimmerEffect()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimensions.holdingItemSeparatorHeight)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }
                }

                is HoldingsUiState.Success -> {
                    val holdings = (uiState as HoldingsUiState.Success).holdings.data.userHolding
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = Dimensions.listBottomPadding)
                    ) {
                        items(holdings.size) { index ->
                            HoldingItem(holding = holdings[index])
                            if (index < holdings.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Dimensions.holdingItemSeparatorHeight)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                        }
                    }
                }

                is HoldingsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                text = (uiState as HoldingsUiState.Error).errorType.getErrorMessage(
                                    localContext
                                ),
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))
                            Button(onClick = { viewModel.loadHoldings() }) {
                                Text(stringResource(R.string.retry_button))
                            }
                        }
                    }
                }
            }
        }

        // Bottom P&L Bar - positioned with higher z-index
        if (uiState is HoldingsUiState.Success) {
            val pnlMetrics = (uiState as HoldingsUiState.Success).pnlMetrics
            BottomPLBar(
                pnlMetrics = pnlMetrics,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Composable that displays a single holding item with symbol, LTP, quantity, and P&L information.
 *
 * @param holding The holding data to display
 */
@Composable
fun HoldingItem(holding: UserHolding) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.holdingItemPadding)
    ) {
        // Header row with Symbol and LTP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side - Symbol
            Text(
                text = holding.symbol,
                fontWeight = FontWeight.Bold,
                fontSize = Dimensions.textSymbolSize,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ltp_label),
                    fontSize = Dimensions.textLabelSize,
                    color = colorResource(R.color.grey_80)
                )

                Text(
                    modifier = Modifier.padding(start = Dimensions.spacingSmall),
                    text = "₹${NumberFormatter.formatNumber(holding.ltp)}",
                    fontSize = Dimensions.textLtpSize,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.holdingItemSpacing))

        // Bottom row with Quantity and P&L
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side - Quantity
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.net_qty_label),
                    fontSize = Dimensions.textLabelSize,
                    color = colorResource(R.color.grey_80),
                )
                Text(
                    modifier = Modifier.padding(start = Dimensions.spacingSmall),
                    text = NumberFormatter.formatInteger(holding.quantity),
                    fontSize = Dimensions.textQuantitySize,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Right side - P&L
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.pnl_label),
                    fontSize = Dimensions.textLabelSize,
                    color = colorResource(R.color.grey_80),
                )

                // Calculate P&L based on comparison with avgPrice * quantity
                val totalValue = holding.avgPrice * holding.quantity
                val currentValue = holding.ltp * holding.quantity
                val pnl = currentValue - totalValue
                val pnlColor = if (pnl >= 0) {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.error
                }

                // Calculate P&L percentage
                val pnlPercentage = if (totalValue > 0) {
                    (pnl / totalValue) * 100
                } else 0.0

                Text(
                    modifier = Modifier.padding(start = Dimensions.spacingSmall),
                    text = if (pnl >= 0) {
                        "₹${NumberFormatter.formatNumber(pnl)}"
                    } else {
                        "-₹${NumberFormatter.formatNumber(-pnl)}"
                    },
                    fontSize = Dimensions.textPnlSize,
                    color = pnlColor,
                )

                // P&L percentage in brackets with smaller font
                Text(
                    modifier = Modifier.padding(start = Dimensions.spacingSmall),
                    text = "(${NumberFormatter.formatNumber(pnlPercentage, 2)}%)",
                    fontSize = Dimensions.textPercentageSize,
                    color = pnlColor,
                )
            }
        }
    }
}

/**
 * Bottom bar composable that displays P&L metrics with expandable detailed view.
 * Shows total P&L and percentage by default, with additional metrics when expanded.
 *
 * @param pnlMetrics The P&L metrics to display
 * @param modifier Optional modifier for customizing the layout
 */
@Composable
fun BottomPLBar(
    pnlMetrics: PnLMetrics,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val totalPnL = pnlMetrics.totalPnL
    val totalPnLColor = if (totalPnL >= 0) Color.Green else Color.Red

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = Dimensions.bottomBarCornerRadius,
                    topEnd = Dimensions.bottomBarCornerRadius
                )
            )
            .background(
                colorResource(R.color.grey_bottom_sheet)
            )
            .shadow(
                elevation = Dimensions.shadowElevation,
                shape = RoundedCornerShape(
                    topStart = Dimensions.bottomBarCornerRadius,
                    topEnd = Dimensions.bottomBarCornerRadius
                )
            )
            .padding(Dimensions.bottomBarPadding)
    ) {
        // Expanded content (shown first when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(300)
            ),
            exit = shrinkVertically(
                animationSpec = tween(300)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimensions.bottomBarExpandedBottomPadding)
            ) {
                // Current Value row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.current_value),
                        fontSize = Dimensions.bottomBarLabelSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(
                            amount = pnlMetrics.currentValue
                        ),
                        fontSize = Dimensions.bottomBarValueSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.bottomBarRowSpacing))

                // Total Investment row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.total_investment),
                        fontSize = Dimensions.bottomBarLabelSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NumberFormatter.formatCurrency(
                            amount = pnlMetrics.totalInvestment
                        ),
                        fontSize = Dimensions.bottomBarValueSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.bottomBarRowSpacing))

                // Today's P&L row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.todays_pnl),
                        fontSize = Dimensions.bottomBarLabelSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val todaysPnL = pnlMetrics.todaysPnL
                    val todaysPnLColor = if (todaysPnL >= 0) Color.Green else Color.Red

                    Text(
                        text = NumberFormatter.formatCurrencyWithSign(amount = todaysPnL),
                        fontSize = Dimensions.bottomBarValueSize,
                        color = todaysPnLColor
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.bottomBarRowSpacing))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.bottomBarDividerHeight)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(Dimensions.bottomBarRowSpacing))

                // Disclaimer
                Text(
                    text = stringResource(R.string.pnl_disclaimer),
                    fontSize = Dimensions.bottomBarDisclaimerSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Main P&L row (always visible at bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = null
                ) {
                    isExpanded = !isExpanded
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - P&L label
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profit_loss_title),
                    fontSize = Dimensions.bottomBarTitleSize,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.padding(start = Dimensions.spacingSmall))
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(300),
                    label = "icon_rotation"
                )

                Icon(
                    modifier = Modifier
                        .size(Dimensions.bottomBarIconSize)
                        .rotate(rotation),
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse_content_description) else stringResource(
                        R.string.expand_content_description
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side - Total P&L value
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = NumberFormatter.formatCurrencyWithSign(totalPnL),
                    fontSize = Dimensions.bottomBarMainValueSize,
                    color = totalPnLColor
                )

                // P&L percentage in brackets with smaller font
                Text(
                    modifier = Modifier.padding(start = Dimensions.spacingSmall),
                    text = "(${
                        NumberFormatter.formatNumber(
                            number = pnlMetrics.pnlPercentage,
                            decimalPlaces = 2
                        )
                    }%)",
                    fontSize = Dimensions.bottomBarPercentageSize,
                    color = totalPnLColor
                )
            }
        }
    }
}

/**
 * Creates a shimmer loading effect animation.
 * Used to show loading state for holding items.
 */
@Composable
fun ShimmerEffect() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(
            x = translateAnim.value - 1000f,
            y = 0f
        ),
        end = Offset(
            x = translateAnim.value,
            y = 1000f
        )
    )

    ShimmerHoldingItem(brush = brush)
}

/**
 * Displays a placeholder holding item with shimmer animation effect.
 * Used during loading state to show loading feedback to the user.
 *
 * @param brush The gradient brush that creates the shimmer effect
 */
@Composable
fun ShimmerHoldingItem(brush: Brush) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.holdingItemPadding)
    ) {
        // Header row with Symbol and LTP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Symbol placeholder
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            // LTP placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.holdingItemSpacing))

        // Bottom row with Quantity and P&L
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Quantity placeholder
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            // P&L placeholder
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}