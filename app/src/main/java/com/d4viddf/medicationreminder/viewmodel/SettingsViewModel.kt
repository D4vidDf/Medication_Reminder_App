package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Language Preference
    val currentLanguageTag: StateFlow<String> = userPreferencesRepository.languageTagFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun updateLanguageTag(newTag: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguageTag(newTag)
        }
    }

    // Theme Preference
    val currentTheme: StateFlow<String> = userPreferencesRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.d4viddf.medicationreminder.data.ThemeKeys.SYSTEM // Default to System
        )

    fun updateTheme(themeKey: String) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(themeKey)
        }
    }

    // Alarm Volume Preference
    val alarmVolume: StateFlow<Float> = userPreferencesRepository.alarmVolumeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f // Default to full volume, consistent with repository
        )

    fun setAlarmVolume(volume: Float) {
        viewModelScope.launch {
            // Ensure volume is within 0.0 to 1.0 range, though repository also coerces
            userPreferencesRepository.updateAlarmVolume(volume.coerceIn(0.0f, 1.0f))
        }
    }
}
