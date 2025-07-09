package com.d4viddf.medicationreminder.di

import com.d4viddf.medicationreminder.repository.MedicationScheduleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppFunctionsEntryPoint {
    fun medicationScheduleRepository(): MedicationScheduleRepository
}
