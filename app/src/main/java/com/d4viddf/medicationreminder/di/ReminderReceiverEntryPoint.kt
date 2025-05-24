package com.d4viddf.medicationreminder.di // Or your chosen package

import android.content.Context
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
import com.d4viddf.medicationreminder.notifications.NotificationScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent // Or ApplicationComponent if older Hilt

@EntryPoint
@InstallIn(SingletonComponent::class) // Or ApplicationComponent
import com.d4viddf.medicationreminder.data.UserPreferencesRepository

interface ReminderReceiverEntryPoint {
    fun reminderRepository(): MedicationReminderRepository
    fun notificationScheduler(): NotificationScheduler
    fun userPreferencesRepository(): UserPreferencesRepository
}