package com.d4viddf.medicationreminder.ui.screens

// Removed selectable and selectableGroup as they are not used with SegmentedButtons
// Removed RadioButton import

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.viewmodel.SettingsViewModel

data class LanguageOption(val displayName: String, val tag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLanguageTag by viewModel.currentLanguageTag.collectAsState()
    val currentThemeKey by viewModel.currentTheme.collectAsState()
    val alarmVolume by viewModel.alarmVolume.collectAsState()

    val languages = remember {
        listOf(
            LanguageOption(context.getString(R.string.language_english), "en"),
            LanguageOption(context.getString(R.string.language_spanish), "es"),
            LanguageOption(context.getString(R.string.language_galician), "gl"),
            LanguageOption(context.getString(R.string.language_euskera), "eu"),
            LanguageOption(context.getString(R.string.language_catalan), "ca")
        )
    }
    var expandedLanguageDropdown by remember { mutableStateOf(false) }
    val selectedLanguageDisplay = remember(currentLanguageTag, languages) {
        languages.find { it.tag == currentLanguageTag }?.displayName ?: languages.firstOrNull()?.displayName ?: ""
    }

    val themeOptions = remember {
        listOf(
            context.getString(R.string.settings_theme_light_option) to com.d4viddf.medicationreminder.data.ThemeKeys.LIGHT,
            context.getString(R.string.settings_theme_dark_option) to com.d4viddf.medicationreminder.data.ThemeKeys.DARK,
            context.getString(R.string.settings_theme_system_option) to com.d4viddf.medicationreminder.data.ThemeKeys.SYSTEM
        )
    }
    // The selectedThemeDisplayName is now derived from the ViewModel's currentThemeKey
    val selectedThemeDisplayName = remember(currentThemeKey, themeOptions) {
        themeOptions.find { it.second == currentThemeKey }?.first ?: themeOptions.first { it.second == com.d4viddf.medicationreminder.data.ThemeKeys.SYSTEM }.first
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() // Added fillMaxSize
                .verticalScroll(rememberScrollState()) // Added verticalScroll for potentially long content
                .padding(16.dp) // Outer padding for the whole screen content
        ) {
            // Language Selection Group
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) { // Inner padding for content
                    Text(
                        text = stringResource(id = R.string.settings_language_label),
                        style = MaterialTheme.typography.titleMedium, // Expressive: titleMedium or titleSmall
                        modifier = Modifier.padding(bottom = 12.dp) // Adjusted padding
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedLanguageDropdown,
                        onExpandedChange = { expandedLanguageDropdown = !expandedLanguageDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedLanguageDisplay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.settings_language_label)) }, // Added label for context
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguageDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge // Ensure text style
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLanguageDropdown,
                            onDismissRequest = { expandedLanguageDropdown = false }
                        ) {
                            languages.forEach { languageOption ->
                                DropdownMenuItem(
                                    text = { Text(languageOption.displayName, style = MaterialTheme.typography.bodyLarge) }, // Applied style
                                    onClick = {
                                        viewModel.updateLanguageTag(languageOption.tag)
                                        expandedLanguageDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Spacer between sections

            // Theme Selection Group
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) { // Inner padding for content
                    Text(
                        text = stringResource(id = R.string.settings_theme_label),
                        style = MaterialTheme.typography.titleMedium, // Expressive: titleMedium or titleSmall
                        modifier = Modifier.padding(bottom = 12.dp) // Adjusted padding
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeOptions.forEachIndexed { index, (themeDisplayName, themeKey) ->
                            SegmentedButton(
                                selected = (currentThemeKey == themeKey),
                                onClick = { viewModel.updateTheme(themeKey) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                                label = { Text(themeDisplayName, style = MaterialTheme.typography.labelLarge) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Spacer between sections

            // Alarm Volume Setting Group
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.settings_alarm_volume_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = alarmVolume,
                            onValueChange = { viewModel.setAlarmVolume(it) },
                            valueRange = 0f..1f,
                            steps = 9, // Creates 10 steps (0%, 10%, ..., 100%)
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${(alarmVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(40.dp) // Fixed width for percentage text
                        )
                    }
                }
            }
        }
    }
}
