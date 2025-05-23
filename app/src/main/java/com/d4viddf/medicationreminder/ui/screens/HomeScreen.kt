package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Scaffold // No longer needed here
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
// import com.d4viddf.medicationreminder.ui.components.BottomNavBar // No longer needed here
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.components.BottomNavBar
import com.d4viddf.medicationreminder.ui.components.MedicationList
import com.d4viddf.medicationreminder.viewmodel.MedicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // onAddMedicationClick and onNavigateToSettings are handled by the global BottomNavBar now
    // However, onAddMedicationClick might still be needed if there's another FAB or button specific to HomeScreen's content.
    // For now, assuming onAddMedicationClick is also handled globally. If not, it should be re-added.
    onMedicationClick: (Int) -> Unit,
    modifier: Modifier = Modifier, // Modifier passed from AppNavigation (includes Scaffold padding)
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications = viewModel.medications.collectAsState().value

    // HomeScreen's content directly uses the modifier passed from AppNavigation
    Column(modifier = modifier) { // Apply the padding from the main Scaffold
        TopAppBar(
            title = { Text(stringResource(R.string.home_screen_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineLarge) }
            // Add navigationIcon or actions if specific to HomeScreen and not global
        )
        MedicationList(
            medications = medications,
            onItemClick = { medication -> onMedicationClick(medication.id) },
            // MedicationList will fill the remaining space provided by the Column
            modifier = Modifier.fillMaxSize() // Or Modifier.weight(1f) if there are other elements in the Column
        )
    }
}
