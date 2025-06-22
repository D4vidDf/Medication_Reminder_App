package com.d4viddf.medicationreminder.ui.components

import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import java.time.LocalTime

data class TodayMedicationData(
    val id: String, // Reminder ID or unique key for this specific reminder instance
    val medicationId: Int, // The ID of the medication itself, for fetching details
    val medicationName: String,
    val dosage: String,
    val medicationType: MedicationType, // This will be used for imageUrl
    val scheduledTime: LocalTime,
    val actualTakenTime: LocalTime? = null,
    val isTaken: Boolean,
    val isFuture: Boolean,
    val medicationColor: MedicationColor,
    val onToggle: (Boolean) -> Unit
)
