package com.d4viddf.medicationreminder.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField // Using OutlinedTextField for a clear M3 style
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier, // Added modifier parameter
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentLanguageCode by viewModel.currentLanguage.collectAsState()
    val currentThemeSetting by viewModel.currentTheme.collectAsState()
    val preRemindersEnabled by viewModel.preRemindersEnabled.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    var languageDropdownExpanded by remember { mutableStateOf(false) }
    val languages = listOf(
        stringResource(R.string.language_english) to "en",
        stringResource(R.string.language_spanish) to "es",
        stringResource(R.string.language_galician) to "gl",
        stringResource(R.string.language_basque) to "eu",
        stringResource(R.string.language_catalan) to "ca"
    )
    val currentLanguageDisplayName = languages.find { it.second == currentLanguageCode }?.first
        ?: languages.find { it.second == "en" }?.first

    var themeDropdownExpanded by remember { mutableStateOf(false) }
    val themes = listOf(
        stringResource(R.string.theme_light) to ThemeSetting.LIGHT,
        stringResource(R.string.theme_dark) to ThemeSetting.DARK,
        stringResource(R.string.theme_system) to ThemeSetting.SYSTEM
    )
    val currentThemeDisplayName = themes.find { it.second == currentThemeSetting }?.first
        ?: stringResource(R.string.theme_system)

    Scaffold(
        modifier = modifier, // Apply the modifier to the Scaffold
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_screen_title)) }, // Default M3 style is titleLarge
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_back_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // M3 colors
                    containerColor = MaterialTheme.colorScheme.surface, // Or surfaceContainer for a slight tint
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp), // Consistent screen padding
            verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing between sections
        ) {
            // Language Setting Section
            Text(
                stringResource(R.string.setting_label_language),
                style = MaterialTheme.typography.titleMedium, // M3 typography for section title
                modifier = Modifier.padding(bottom = 8.dp) // Space between title and dropdown
            )
            ExposedDropdownMenuBox(
                expanded = languageDropdownExpanded,
                onExpandedChange = { languageDropdownExpanded = !languageDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField( // Using OutlinedTextField for M3 style
                    value = currentLanguageDisplayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    // Use label parameter within TextField for better M3 alignment
                    // label = { Text(stringResource(R.string.setting_label_language)) }, 
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors() // M3 colors
                )
                ExposedDropdownMenu(
                    expanded = languageDropdownExpanded,
                    onDismissRequest = { languageDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    languages.forEach { (name, code) ->
                        DropdownMenuItem(
                            text = { Text(name, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                viewModel.saveLanguage(code)
                                languageDropdownExpanded = false
                                activity?.recreate()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp)) // Divider after section

            // Theme Setting Section
            Text(
                stringResource(R.string.setting_label_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = themeDropdownExpanded,
                onExpandedChange = { themeDropdownExpanded = !themeDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentThemeDisplayName,
                    onValueChange = {},
                    readOnly = true,
                    // label = { Text(stringResource(R.string.setting_label_theme)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = themeDropdownExpanded,
                    onDismissRequest = { themeDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    themes.forEach { (name, themeSetting) ->
                        DropdownMenuItem(
                            text = { Text(name, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                viewModel.saveTheme(themeSetting)
                                themeDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))

            // Pre-Reminder Toggle using ListItem for a different M3 setting style
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.setting_label_pre_reminders),
                        style = MaterialTheme.typography.titleMedium // Consistent label style
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(R.string.setting_description_pre_reminders),
                        style = MaterialTheme.typography.bodyMedium // M3 style for description
                    )
                },
                trailingContent = {
                    Switch(
                        checked = preRemindersEnabled,
                        onCheckedChange = { viewModel.savePreRemindersEnabled(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.savePreRemindersEnabled(!preRemindersEnabled) }
            )
            HorizontalDivider()
        }
    }
}
