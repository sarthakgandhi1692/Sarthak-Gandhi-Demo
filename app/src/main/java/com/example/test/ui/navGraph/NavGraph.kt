package com.example.test.ui.navGraph

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.test.ui.screens.holdingsScreen.HoldingsViewModel
import com.example.test.ui.screens.holdingsScreen.HoldingsScreen

/**
 * Main navigation graph composable that defines the app's navigation structure.
 * Sets up navigation routes and their corresponding composable.
 *
 * @param navController The navigation controller that handles navigation between screens
 * @param startDestination The initial screen to display when the app launches (defaults to Holdings screen)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.Holdings
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Holdings> {
            val viewModel = hiltViewModel<HoldingsViewModel>()
            HoldingsScreen(
                viewModel = viewModel
            )
        }
    }
}