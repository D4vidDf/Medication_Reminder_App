package com.d4viddf.medicationreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
// Assuming Context.dataStore is accessible via the existing import from LanguageRepository
// import com.d4viddf.medicationreminder.data.dataStore 
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeSetting {
    LIGHT, DARK, SYSTEM
}

@Singleton
class ThemeRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    val currentTheme: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            ThemeSetting.valueOf(preferences[PreferencesKeys.APP_THEME] ?: ThemeSetting.SYSTEM.name)
        }

    suspend fun saveTheme(themeSetting: ThemeSetting) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.APP_THEME] = themeSetting.name
        }
    }
}
