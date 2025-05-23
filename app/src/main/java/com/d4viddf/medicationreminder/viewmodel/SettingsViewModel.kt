package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.preferences.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {

    val currentLanguage: StateFlow<String> = languageManager.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LanguageManager.DEFAULT_LANGUAGE
        )

    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            languageManager.saveLanguage(languageCode)
        }
    }
}
