package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
    isMainScaffold: Boolean, // Added parameter
    modifier: Modifier = Modifier // Add this line
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.then(if (isMainScaffold) Modifier.fillMaxSize() else Modifier) // Apply incoming modifier and then conditional padding
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddMedicationClick = { navController.navigate(Screen.AddMedication.route) }, // Kept
                onMedicationClick = { medicationId -> // Kept
                    navController.navigate(Screen.MedicationDetails.createRoute(medicationId))
                },
                // Removed onNavigateToSettings, onNavigateToCalendar, onNavigateToProfile
                widthSizeClass = widthSizeClass // Kept
            )
        }
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MedicationDetails.route) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            if (medicationId != null) {
                MedicationDetailsScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
