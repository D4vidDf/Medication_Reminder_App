package com.d4viddf.medicationreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
// Assuming Context.dataStore is accessible via the existing import from LanguageRepository
// import com.d4viddf.medicationreminder.data.dataStore 
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreReminderRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val PRE_REMINDERS_ENABLED = booleanPreferencesKey("pre_reminders_enabled")
    }

    val preRemindersEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PRE_REMINDERS_ENABLED] ?: true // Default to true
        }

    suspend fun savePreRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PRE_REMINDERS_ENABLED] = enabled
        }
    }
}
