package com.d4viddf.medicationreminder.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Placeholder for actual data fetching logic
data class NextDoseInfo(
    val medicationName: String,
    val nextDoseTime: String,
    val doseAmount: String
)

class MedicationScheduleRepository(private val context: Context) {

    // Dummy data for demonstration
    private val dummySchedules = mapOf(
        "aspirin" to NextDoseInfo("Aspirin", getDummyTime(1), "1 tablet"),
        "ibuprofen" to NextDoseInfo("Ibuprofen", getDummyTime(2), "200mg"),
        "paracetamol" to NextDoseInfo("Paracetamol", getDummyTime(3), "500mg")
    )

    private fun getDummyTime(hoursFromNow: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, hoursFromNow)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    suspend fun getNextDose(medicationName: String): NextDoseInfo? {
        // In a real app, this would query a database or a remote server.
        // For now, it returns dummy data.
        return dummySchedules[medicationName.lowercase(Locale.getDefault())]
    }
}
