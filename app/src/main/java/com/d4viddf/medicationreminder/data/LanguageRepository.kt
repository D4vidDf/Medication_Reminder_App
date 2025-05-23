package com.d4viddf.medicationreminder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class LanguageRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val currentLanguage: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE]
        }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.APP_LANGUAGE] = languageCode
        }
    }
}
