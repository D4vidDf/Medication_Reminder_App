package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.ui.components.MedicationList
import com.d4viddf.medicationreminder.viewmodel.MedicationViewModel
// Removed AppHorizontalFloatingToolbar and AppNavigationRail imports
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
// Removed Log import as navigation lambdas are removed
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
// Removed width import as it's not used directly for fixed width here
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
// Removed unit.dp import as it's not used directly for fixed width here
import com.d4viddf.medicationreminder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // Removed onNavigateToSettings, onNavigateToCalendar, onNavigateToProfile
    onAddMedicationClick: () -> Unit, // Kept from previous logic, though now handled by App level nav components
    onMedicationClick: (Int) -> Unit, // For compact navigation or detail view trigger
    widthSizeClass: WindowWidthSizeClass,
    viewModel: MedicationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier // Added modifier to be used by NavHost,
) {
    val medications by viewModel.medications.collectAsState() // Use 'by' delegate
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var selectedMedicationId by rememberSaveable { mutableStateOf<Int?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshMedications()
        }
    }

    // Update the pull-to-refresh indicator state based on the ViewModel's isRefreshing state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            // If VM starts refreshing (e.g. initial load), show indicator
            if (!pullToRefreshState.isRefreshing) pullToRefreshState.startRefresh()
        } else {
            // If VM stops refreshing, hide indicator
            if (pullToRefreshState.isRefreshing) pullToRefreshState.endRefresh()
        }
    }

    val medicationListClickHandler: (Int) -> Unit = { medicationId ->
        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            onMedicationClick(medicationId) // Navigate to full details screen
        } else {
            selectedMedicationId = medicationId // Show in detail pane
        }
    }

    if (widthSizeClass == WindowWidthSizeClass.Compact) {
        Box(modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
            MedicationList(
                medications = medications,
                onItemClick = { medication -> medicationListClickHandler(medication.id) },
                modifier = Modifier.fillMaxSize() // Ensure it fills the space given by NavHost
            )
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    } else { // Medium or Expanded - List/Detail View
        Row(modifier = modifier.fillMaxSize()) {
            // Medication List Pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                MedicationList(
                    medications = medications,
                    onItemClick = { medication -> medicationListClickHandler(medication.id) },
                    modifier = Modifier.fillMaxSize() // Fill the Box
                )
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Detail Pane
            Box(modifier = Modifier.weight(2f).fillMaxHeight()) {
                if (selectedMedicationId == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(id = R.string.select_medication_placeholder))
                    }
                } else {
                    MedicationDetailsScreen(
                        medicationId = selectedMedicationId!!,
                        onNavigateBack = { selectedMedicationId = null } // Clear selection
                    )
                }
            }
        }
    }
}
