package com.example.test.utils

object NumberFormatter {
    
    /**
     * Formats a double number with comma separators and 2 decimal places
     * Example: 1234.56 -> 1,234.56
     */
    fun formatNumber(number: Double): String {
        return String.format("%,.2f", number)
    }
    
    /**
     * Formats an integer with comma separators
     * Example: 1234 -> 1,234
     */
    fun formatInteger(number: Int): String {
        return String.format("%,d", number)
    }
    
    /**
     * Formats a double number with comma separators and specified decimal places
     * Example: formatNumber(1234.567, 3) -> 1,234.567
     */
    fun formatNumber(number: Double, decimalPlaces: Int): String {
        return String.format("%,.${decimalPlaces}f", number)
    }
    
    /**
     * Formats currency with rupee symbol and comma separators
     * Example: 1234.56 -> ₹1,234.56
     */
    fun formatCurrency(amount: Double): String {
        return "₹${formatNumber(amount)}"
    }
    
    /**
     * Formats currency with rupee symbol, comma separators, and negative sign before rupee
     * Example: -1234.56 -> -₹1,234.56
     */
    fun formatCurrencyWithSign(amount: Double): String {
        return if (amount >= 0) {
            "₹${formatNumber(amount)}"
        } else {
            "-₹${formatNumber(-amount)}"
        }
    }
} 