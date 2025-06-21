package com.d4viddf.medicationreminder.ui.components

import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Add // Removed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.screens.Screen // Required for Screen routes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppHorizontalFloatingToolbar(
    onHomeClick: () -> Unit, // Navigates to TodayScreen
    onMedicationLibraryClick: () -> Unit, // New: Navigates to MedicationLibraryScreen
    onCalendarClick: () -> Unit,
    onProfileClick: () -> Unit,
    // onSettingsClick is removed
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentRoute: String? = null
) {
    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_add_24),
                    contentDescription = stringResource(id = R.string.add_medication_title)
                )
            }
        }
    ) {
        // Home (Today Screen)
        val homeSelected = currentRoute == Screen.Today.route
        IconButton(onClick = { if (!homeSelected) onHomeClick() }) {
            Icon(
                painter = painterResource(id = if (homeSelected) R.drawable.ic_home_filled else R.drawable.rounded_home_24),
                contentDescription = stringResource(id = R.string.today_screen_title),
                tint = if (homeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Medication Library
        val librarySelected = currentRoute == Screen.MedicationLibrary.route
        IconButton(onClick = { if (!librarySelected) onMedicationLibraryClick() }) {
            Icon(
                painter = painterResource(id = if (librarySelected) R.drawable.ic_list_alt_filled_24 else R.drawable.ic_list_alt_24), // Using list_alt
                contentDescription = stringResource(id = R.string.medication_library_screen_title),
                tint = if (librarySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calendar
        val calendarSelected = currentRoute == Screen.Calendar.route
        IconButton(onClick = { if (!calendarSelected) onCalendarClick() }) {
            Icon(
                painter = painterResource(id = if (calendarSelected) R.drawable.ic_calendar_filled else R.drawable.ic_calendar),
                contentDescription = stringResource(id = R.string.calendar_screen_title),
                tint = if (calendarSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile
        val profileSelected = currentRoute == Screen.Profile.route
        IconButton(onClick = { if (!profileSelected) onProfileClick() }) {
            Icon(
                painter = painterResource(id = if (profileSelected) R.drawable.ic_person_filled else R.drawable.rounded_person_24),
                contentDescription = stringResource(id = R.string.profile_screen_title),
                tint = if (profileSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Settings icon is removed from here
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Light Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO, apiLevel = 33)
@androidx.compose.ui.tooling.preview.Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun AppHorizontalFloatingToolbarPreview() {
    com.d4viddf.medicationreminder.ui.theme.AppTheme(dynamicColor = false) {
        AppHorizontalFloatingToolbar(
            onHomeClick = {},
            onMedicationLibraryClick = {},
            onCalendarClick = {},
            onProfileClick = {},
            // onSettingsClick = {}, // Removed
            onAddClick = {},
            currentRoute = Screen.Today.route // Updated preview to use Today
        )
    }
}
