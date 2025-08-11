package com.example.test.ui.navGraph

import kotlinx.serialization.Serializable

/**
 * Sealed class representing all possible navigation destinations in the app.
 * Each screen is defined as a serializable object to support navigation arguments.
 */
@Serializable
sealed class Screen {

    /**
     * Holdings screen destination that displays user's portfolio holdings.
     */
    @Serializable
    data object Holdings : Screen()
}