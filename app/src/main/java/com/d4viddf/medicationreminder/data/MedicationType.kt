package com.d4viddf.medicationreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.d4viddf.medicationreminder.R // Required for default icon

@Entity(tableName = "medication_types")
data class MedicationType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val imageUrl: String?,
    val iconResId: Int // Added field
) {
    companion object {
        fun defaultType(): MedicationType {
            return MedicationType(
                id = -1, // Or some other indicator of a default/unknown type
                name = "Other",
                imageUrl = null,
                iconResId = R.drawable.ic_medication_24 // Assuming this is a generic medication icon
            )
        }
    }
}
