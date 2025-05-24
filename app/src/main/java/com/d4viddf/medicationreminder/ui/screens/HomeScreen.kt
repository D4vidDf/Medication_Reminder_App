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
import androidx.compose.material3.LoadingIndicator // Added import
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.input.nestedscroll.nestedScroll // Not directly used with PullToRefreshBox
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size // Added import for LoadingIndicator size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp // Added import for LoadingIndicator size
import com.d4viddf.medicationreminder.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    // The LaunchedEffect for initial load might still be relevant if not handled by pull-to-refresh itself
    // or if a refresh isn't triggered automatically on composition by the ViewModel.
    // However, the ViewModel's init block already calls refreshMedications.

    // Removed pullToRefreshState and its related LaunchedEffects,
    // as PullToRefreshBox will handle the state via isRefreshing prop.

    val medicationListClickHandler: (Int) -> Unit = { medicationId ->
        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            onMedicationClick(medicationId) // Navigate to full details screen
        } else {
            selectedMedicationId = medicationId // Show in detail pane
        }
    }

    if (widthSizeClass == WindowWidthSizeClass.Compact) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshMedications() },
            modifier = modifier.fillMaxSize(), // Apply the modifier here
            indicator = {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    LoadingIndicator(
                        modifier = Modifier.size(36.dp), // Example size
                        color =
                            MaterialTheme.colorScheme.primary,

                    )
                }
            }
        ) {
            MedicationList(
                medications = medications,
                onItemClick = { medication -> medicationListClickHandler(medication.id) },
                modifier = Modifier.fillMaxSize() // MedicationList fills the PullToRefreshBox content area
            )
        }
    } else { // Medium or Expanded - List/Detail View
        Row(modifier = modifier.fillMaxSize()) {
            // Medication List Pane
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshMedications() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                indicator = {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        LoadingIndicator(
                            modifier = Modifier.size(36.dp), // Example size
                            color =
                                MaterialTheme.colorScheme.primary,

                            )
                    }
                }
            ) {
                MedicationList(
                    medications = medications,
                    onItemClick = { medication -> medicationListClickHandler(medication.id) },
                    modifier = Modifier.fillMaxSize() // MedicationList fills the PullToRefreshBox content area
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
