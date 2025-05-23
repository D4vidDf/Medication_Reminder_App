package com.d4viddf.medicationreminder.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.d4viddf.medicationreminder.ui.screens.AppNavigation
import com.d4viddf.medicationreminder.ui.theme.MedicationReminderTheme // Assuming this theme exists

@Composable
fun MedicationReminderApp(languageCode: String) {
    // languageCode is received but not directly used here.
    // stringResource() calls throughout the app will use the context
    // provided by CompositionLocalProvider in MainActivity.
    MedicationReminderTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}
