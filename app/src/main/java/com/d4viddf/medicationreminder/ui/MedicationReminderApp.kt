package com.d4viddf.medicationreminder.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.d4viddf.medicationreminder.ui.components.BottomNavBar
import com.d4viddf.medicationreminder.ui.screens.AppNavigation
import com.d4viddf.medicationreminder.ui.screens.Screen // Import your Screen sealed class
import com.d4viddf.medicationreminder.ui.theme.AppTheme

@Composable
fun MedicationReminderApp(darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Determine selectedIndex based on the current route
        val selectedIndex = when (currentRoute) {
            Screen.Home.route -> 0
            Screen.Settings.route -> 1
            else -> 0 // Default to Home
        }

        // Define which routes should show the BottomNavBar
        val bottomBarVisible = currentRoute == Screen.Home.route || currentRoute == Screen.Settings.route
        // You might want to hide it for AddMedicationScreen, MedicationDetailsScreen, etc.
        // For this iteration, AddMedication and MedicationDetails will also show it if not explicitly excluded.
        // Let's refine bottomBarVisible to explicitly include only Home and Settings for now.

        Scaffold(
            bottomBar = {
                if (bottomBarVisible) {
                    BottomNavBar(
                        selectedIndex = selectedIndex,
                        onHomeClick = {
                            if (currentRoute != Screen.Home.route) { // Avoid navigating to the same screen
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onSettingsClick = {
                            if (currentRoute != Screen.Settings.route) { // Avoid navigating to the same screen
                                navController.navigate(Screen.Settings.route) {
                                    // Consistent navigation behavior with Home
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        onAddClick = {
                            navController.navigate(Screen.AddMedication.route)
                            // Consider popUpTo behavior if Add is a main tab vs. a flow
                        }
                    )
                }
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding) 
            )
        }
    }
}
