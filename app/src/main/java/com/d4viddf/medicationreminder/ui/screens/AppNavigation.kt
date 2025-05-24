package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Define the routes for navigation
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMedication : Screen("addMedication")
    object MedicationDetails : Screen("medicationDetails/{id}") {
        fun createRoute(id: Int) = "medicationDetails/$id"
    }
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")

}


@Composable
fun AppNavigation(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    paddingValues: PaddingValues = PaddingValues(), // Added parameter
    isMainScaffold: Boolean // Added parameter
) {
    val animationSpec = tween<IntOffset>(durationMillis = 300) // Or other duration like 400
    val fadeAnimationSpec = tween<Float>(durationMillis = 300)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        // Apply padding from parent scaffold if this is the main content area,
        // otherwise, apply system bar padding directly for edge-to-edge when not nested in a main scaffold.
        modifier = if (isMainScaffold) Modifier.padding(paddingValues) else Modifier.windowInsetsPadding(WindowInsets.systemBars),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = animationSpec) +
                    fadeIn(animationSpec = fadeAnimationSpec)
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = animationSpec) +
                    fadeOut(animationSpec = fadeAnimationSpec)
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = animationSpec) +
                    fadeIn(animationSpec = fadeAnimationSpec)
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = animationSpec) +
                    fadeOut(animationSpec = fadeAnimationSpec)
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddMedicationClick = { navController.navigate(Screen.AddMedication.route) },
                onMedicationClick = { medicationId ->
                    navController.navigate(Screen.MedicationDetails.createRoute(medicationId))
                },
                widthSizeClass = widthSizeClass
                // modifier = Modifier.fillMaxSize() // Removed
            )
        }
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
                // modifier = Modifier.fillMaxSize() // Removed
            )
        }
        composable(Screen.MedicationDetails.route) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            if (medicationId != null) {
                MedicationDetailsScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() }
                    // modifier = Modifier.fillMaxSize() // Removed
                )
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
                // modifier = Modifier.fillMaxSize() // Removed
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() }
                // modifier = Modifier.fillMaxSize() // Removed
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
                // modifier = Modifier.fillMaxSize() // Removed
            )
        }
    }
}
