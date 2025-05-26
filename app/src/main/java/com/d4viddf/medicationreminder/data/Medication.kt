package com.d4viddf.medicationreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val typeId: Int?,
    val color: String,
    val dosage: String?,          // Dosage information, e.g., "500 mg"
    val packageSize: Int,         // Number of doses in the package
    val remainingDoses: Double?,      // Number of doses left in the package, now Double and nullable
    val startDate: String?,       // Start date of taking medication (optional)
    val endDate: String?,          // End date if the medication is not chronic (optional)
    val reminderTime: String?, // Nullable in case the reminder time is not set
    val userDosageQuantity: String?, // e.g., "1", "0.5", "2"
    val userDosageUnit: String?      // e.g., "pill", "mL", "mg", "spray", "drop"
)
