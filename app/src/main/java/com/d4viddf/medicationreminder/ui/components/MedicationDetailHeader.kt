package com.d4viddf.medicationreminder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import androidx.compose.ui.semantics.contentDescription // Moved
import androidx.compose.ui.semantics.semantics // Moved
import com.d4viddf.medicationreminder.R // Moved

@Composable
fun MedicationDetailHeader(
    medicationName: String?,
    userDosageQuantity: String?,
    userDosageUnit: String?,
    cimaDosage: String?, // Fallback CIMA dosage
    dosesPerDay: Int?,
    medicationImageUrl: String?,
    colorScheme: MedicationColor,
    modifier: Modifier = Modifier
) {
    val loadingText = stringResource(id = R.string.medication_detail_header_loading)
    val noDosageText = stringResource(id = R.string.no_dosage_info) // Use new fallback
    val imageAccText = stringResource(id = R.string.medication_detail_header_image_acc)
    val intervalDosingText = stringResource(id = R.string.interval_dosing_display)
    val dosesPerDayFormat = stringResource(id = R.string.doses_per_day_format)
    val dosageTimesADayContentDesc = stringResource(id = R.string.dosage_times_a_day_content_description)
    val dosageIntervalContentDesc = stringResource(id = R.string.dosage_interval_content_description)


    val displayName = remember(medicationName, loadingText) {
        val words = medicationName?.split(" ")
        if (words != null && words.size > 3) {
            words.take(3).joinToString(" ")
        } else {
            medicationName ?: loadingText
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(), // El modifier se aplica al Row principal
        verticalAlignment = Alignment.CenterVertically // Alineación vertical de los elementos del Row
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.textColor,
                lineHeight = 40.sp, // Ajustar según sea necesario
                maxLines = 2, // Permitir hasta 2 líneas para el nombre
                overflow = TextOverflow.Ellipsis // Añadir elipsis si el texto es muy largo
            )
            Spacer(modifier = Modifier.height(8.dp))

            val dosageText = remember(userDosageQuantity, userDosageUnit, cimaDosage, dosesPerDay, intervalDosingText, dosesPerDayFormat, noDosageText) {
                val dosagePart = when {
                    !userDosageQuantity.isNullOrBlank() && !userDosageUnit.isNullOrBlank() -> "$userDosageQuantity $userDosageUnit"
                    !cimaDosage.isNullOrBlank() -> cimaDosage
                    else -> null
                }

                val frequencyPart = when {
                    dosesPerDay != null && dosesPerDay > 0 -> String.format(dosesPerDayFormat, dosesPerDay)
                    dosesPerDay == null -> intervalDosingText // Explicitly null for interval, show "Interval"
                    else -> null // dosesPerDay is 0 or not applicable
                }

                if (dosagePart != null && frequencyPart != null) {
                    "$dosagePart - $frequencyPart"
                } else dosagePart ?: noDosageText
            }

            val dosageContentDescription = remember(userDosageQuantity, userDosageUnit, cimaDosage, dosesPerDay, intervalDosingText, noDosageText, dosageTimesADayContentDesc, dosageIntervalContentDesc) {
                val baseDosage = when {
                    !userDosageQuantity.isNullOrBlank() && !userDosageUnit.isNullOrBlank() -> "$userDosageQuantity $userDosageUnit"
                    !cimaDosage.isNullOrBlank() -> cimaDosage
                    else -> noDosageText
                }
                when {
                    dosesPerDay != null && dosesPerDay > 0 -> String.format(dosageTimesADayContentDesc, baseDosage, dosesPerDay)
                    dosesPerDay == null -> String.format(dosageIntervalContentDesc, baseDosage) // Interval
                    else -> baseDosage // Just dosage if no frequency info
                }
            }

            Text(
                text = dosageText,
                fontSize = 20.sp,
                color = colorScheme.textColor,
                modifier = Modifier.semantics { contentDescription = dosageContentDescription }
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = rememberAsyncImagePainter(model = medicationImageUrl ?: "https://placehold.co/100x100.png"),
            contentDescription = imageAccText, // Generic description, specific dosage info is in Text
            modifier = Modifier.size(64.dp)
        )
    }
}