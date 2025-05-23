package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check // Ensure this import is present
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.viewmodel.SettingsViewModel // Import the ViewModel

data class LanguageOption( // Keep this data class here or move to a common place if used elsewhere
    val code: String,
    val displayNameResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel() // Use Hilt to inject the ViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    // val coroutineScope = rememberCoroutineScope() // No longer needed for saveLanguage

    val languageOptions = remember {
        listOf(
            LanguageOption("es", R.string.language_spanish_display),
            LanguageOption("en", R.string.language_english_display),
            LanguageOption("gl", R.string.language_galician_display),
            LanguageOption("eu", R.string.language_euskera_display),
            LanguageOption("ca", R.string.language_catalan_display)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.medication_detail_back_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_language_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(languageOptions) { langOption ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // No need for coroutineScope.launch here, ViewModel handles it
                            viewModel.saveLanguage(langOption.code)
                            // MainActivity logic should automatically handle recreation.
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(langOption.displayNameResId), style = MaterialTheme.typography.bodyLarge)
                    if (currentLanguage == langOption.code) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.content_description_selected_language), // Updated in previous subtask
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Divider()
            }
        }
    }
}
