package com.d4viddf.medicationreminder.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.screens.Screen
import androidx.compose.ui.unit.dp

@Composable
fun AppNavigationRail(
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentRoute: String? = null
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    NavigationRail(
        modifier = modifier.animateContentSize(),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_medication_title)
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isExpanded) stringResource(R.string.collapse_navigation_rail) else stringResource(R.string.expand_navigation_rail)
                    )
                }
                Spacer(Modifier.height(16.dp)) // Add some space before the first item
            }
        }
    ) {
        val homeSelected = currentRoute == Screen.Home.route
        NavigationRailItem(
            icon = {
                Icon(
                    painter = painterResource(id = if (homeSelected) R.drawable.rounded_home_24 else R.drawable.ic_outline_home_24),
                    contentDescription = stringResource(R.string.home_screen_title)
                )
            },
            selected = homeSelected,
            onClick = onHomeClick,
            label = { if (isExpanded) Text(stringResource(R.string.home_screen_title)) },
            alwaysShowLabel = false, // Let our expanded state control this
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer // Or adjust as needed
            )
        )

        val calendarSelected = currentRoute == Screen.Calendar.route
        NavigationRailItem(
            icon = {
                Icon(
                    painter = painterResource(id = if (calendarSelected) R.drawable.ic_round_calendar_today_24 else R.drawable.ic_outline_calendar_today_24),
                    contentDescription = stringResource(R.string.calendar_screen_title)
                )
            },
            selected = calendarSelected,
            onClick = onCalendarClick,
            label = { if (isExpanded) Text(stringResource(R.string.calendar_screen_title)) },
            alwaysShowLabel = false,
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        val profileSelected = currentRoute == Screen.Profile.route
        NavigationRailItem(
            icon = {
                Icon(
                    painter = painterResource(id = if (profileSelected) R.drawable.rounded_person_24 else R.drawable.ic_outline_person_24),
                    contentDescription = stringResource(R.string.profile_screen_title)
                )
            },
            selected = profileSelected,
            onClick = onProfileClick,
            label = { if (isExpanded) Text(stringResource(R.string.profile_screen_title)) },
            alwaysShowLabel = false,
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        val settingsSelected = currentRoute == Screen.Settings.route
        NavigationRailItem(
            icon = {
                Icon(
                    painter = painterResource(id = if (settingsSelected) R.drawable.rounded_settings_24 else R.drawable.ic_outline_settings_24),
                    contentDescription = stringResource(R.string.settings_screen_title)
                )
            },
            selected = settingsSelected,
            onClick = onSettingsClick,
            label = { if (isExpanded) Text(stringResource(R.string.settings_screen_title)) },
            alwaysShowLabel = false,
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
