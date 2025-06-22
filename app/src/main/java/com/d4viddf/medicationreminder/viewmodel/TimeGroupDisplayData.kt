package com.d4viddf.medicationreminder.viewmodel

import com.d4viddf.medicationreminder.ui.components.TodayMedicationData
import java.time.LocalTime

data class TimeGroupDisplayData(
    val scheduledTime: LocalTime,
    val reminders: List<TodayMedicationData>,
    val takenCount: Int,
    val totalInGroup: Int
)
