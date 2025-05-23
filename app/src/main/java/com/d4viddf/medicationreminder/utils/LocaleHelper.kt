package com.d4viddf.medicationreminder.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun updateLocale(context: Context, languageCode: String): ContextWrapper {
        val config = Configuration(context.resources.configuration)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        
        // Deprecated in API 33, but needed for older versions
        // config.setLocale(locale) // Use setLocales for API 24+

        val updatedContext = context.createConfigurationContext(config)
        return ContextWrapper(updatedContext)
    }
}
