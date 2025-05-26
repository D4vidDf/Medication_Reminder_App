package com.d4viddf.medicationreminder.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // Added
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.Medication
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.ScheduleType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor

@Composable
fun MedicationCard(
    medication: Medication,
    schedule: MedicationSchedule?, // Added schedule parameter
    onClick: () -> Unit // Callback for navigation
) {
    val color = try {
        MedicationColor.valueOf(medication.color)
    } catch (e: IllegalArgumentException) {
        Log.w("MedicationCard", "Invalid color string: '${medication.color}' for medication '${medication.name}'. Defaulting.", e)
        MedicationColor.LIGHT_ORANGE
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }, // Trigger navigation on click
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Ensure spacing
            verticalAlignment = Alignment.CenterVertically // Align items vertically
        ) {
            Column(
                modifier = Modifier.weight(1f) // Ensure the text column takes available space
            ) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color= color.textColor,
                    fontWeight = FontWeight.Bold
                )

                val noDosageInfoText = stringResource(id = R.string.no_dosage_info)
                val intervalDosingText = stringResource(id = R.string.interval_dosing_display)
                val dosesPerDayFormat = stringResource(id = R.string.doses_per_day_format)
                val dosageTimesADayContentDesc = stringResource(id = R.string.dosage_times_a_day_content_description)
                val dosageIntervalContentDesc = stringResource(id = R.string.dosage_interval_content_description)

                val dosesPerDay = remember(schedule) {
                    when (schedule?.scheduleType) {
                        ScheduleType.DAILY, ScheduleType.CUSTOM_ALARMS, ScheduleType.WEEKLY -> {
                            schedule.specificTimes?.split(',')?.count { it.isNotBlank() } ?: 0
                        }
                        ScheduleType.INTERVAL -> null // Special case for interval
                        else -> 0 // Or null if "0x day" is not desired for AS_NEEDED etc.
                    }
                }

                val dosageText = remember(medication, dosesPerDay, schedule, noDosageInfoText, intervalDosingText, dosesPerDayFormat) {
                    val dosagePart = when {
                        !medication.userDosageQuantity.isNullOrBlank() && !medication.userDosageUnit.isNullOrBlank() ->
                            "${medication.userDosageQuantity} ${medication.userDosageUnit}"
                        !medication.dosage.isNullOrBlank() -> medication.dosage // Fallback to CIMA dosage
                        else -> null
                    }

                    val frequencyPart = when {
                        dosesPerDay != null && dosesPerDay > 0 -> String.format(dosesPerDayFormat, dosesPerDay)
                        schedule?.scheduleType == ScheduleType.INTERVAL -> intervalDosingText
                        else -> null
                    }

                    if (dosagePart != null && frequencyPart != null) {
                        "$dosagePart - $frequencyPart"
                    } else dosagePart ?: noDosageInfoText
                }

                val contentDesc = remember(medication, dosesPerDay, schedule, noDosageInfoText, intervalDosingText, dosageTimesADayContentDesc, dosageIntervalContentDesc) {
                    val baseDosage = when {
                        !medication.userDosageQuantity.isNullOrBlank() && !medication.userDosageUnit.isNullOrBlank() ->
                            "${medication.userDosageQuantity} ${medication.userDosageUnit}"
                        !medication.dosage.isNullOrBlank() -> medication.dosage
                        else -> noDosageInfoText
                    }
                    when {
                        dosesPerDay != null && dosesPerDay > 0 -> String.format(dosageTimesADayContentDesc, baseDosage, dosesPerDay)
                        schedule?.scheduleType == ScheduleType.INTERVAL -> String.format(dosageIntervalContentDesc, baseDosage)
                        else -> baseDosage
                    }
                }

                Text(
                    text = dosageText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = color.textColor,
                    modifier = Modifier.semantics { this.contentDescription = contentDesc }
                )
                // Optional: Display next reminder time if available and relevant
                // medication.reminderTime could be displayed if it represents the *next* scheduled dose.
                // For this task, focusing on "Nx day".
            }

            // Medication avatar at the end
            MedicationAvatar(color = Color.White)
        }
    }
}

@Composable
fun MedicationAvatar(color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = color, shape = CircleShape)
    )
}
