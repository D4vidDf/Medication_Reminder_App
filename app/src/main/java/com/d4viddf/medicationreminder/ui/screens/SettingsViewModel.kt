package com.d4viddf.medicationreminder.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.LanguageRepository
import com.d4viddf.medicationreminder.data.PreReminderRepository
import com.d4viddf.medicationreminder.data.ThemeRepository
import com.d4viddf.medicationreminder.data.ThemeSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val themeRepository: ThemeRepository,
    private val preReminderRepository: PreReminderRepository
) : ViewModel() {

    val currentLanguage: StateFlow<String?> = languageRepository.currentLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            languageRepository.saveLanguage(languageCode)
        }
    }

    val currentTheme: StateFlow<ThemeSetting> = themeRepository.currentTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeSetting.SYSTEM)

    fun saveTheme(themeSetting: ThemeSetting) {
        viewModelScope.launch {
            themeRepository.saveTheme(themeSetting)
        }
    }

    val preRemindersEnabled: StateFlow<Boolean> = preReminderRepository.preRemindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true) // Default to true

    fun savePreRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preReminderRepository.savePreRemindersEnabled(enabled)
        }
    }
}
