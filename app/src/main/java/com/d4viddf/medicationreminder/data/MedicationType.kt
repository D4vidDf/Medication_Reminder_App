package com.d4viddf.medicationreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// No R import needed if we don't set a default iconResId here

@Entity(tableName = "medication_types")
data class MedicationType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val imageUrl: String?
) {
    companion object {
        fun defaultType(): MedicationType {
            return MedicationType(
                id = -1, // Indicates a default/unknown type
                name = "Other", // Default name
                imageUrl = null // No default image URL
            )
        }
    }
}
