package com.d4viddf.medicationreminder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.Icon // Replaced by Image for Coil
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.d4viddf.medicationreminder.R // For previews & placeholder/error drawables
import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
// Correct import for TodayMedicationData
import com.d4viddf.medicationreminder.ui.components.TodayMedicationData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCardTodayFinal(
    modifier: Modifier = Modifier,
    data: TodayMedicationData,
    shape: RoundedCornerShape
) {
    val baseMedicationColor = data.medicationColor.getColor()
    val onBaseMedicationColor = data.medicationColor.getOnColor()

    val cardBackgroundColor: Color
    val mainContentColor: Color
    val subContentColor: Color
    // val iconTintColor: Color // Icon tint might not be directly applicable to Coil Image easily

    if (data.isTaken) {
        cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        mainContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        subContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        // iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else if (data.isFuture) {
        cardBackgroundColor = baseMedicationColor.copy(alpha = 0.25f)
        // Basic contrast for text on colored background
        mainContentColor = if (baseMedicationColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f)
        subContentColor = if (baseMedicationColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.8f)
        // iconTintColor = mainContentColor
    } else { // Past, not taken
        cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
        mainContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        subContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        // iconTintColor = MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (data.isTaken) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = data.medicationType.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_medication_24), // Generic placeholder
                    error = painterResource(id = R.drawable.ic_medication_24) // Generic error icon
                ),
                contentDescription = data.medicationType.name, // TODO: Use stringResource for accessibility
                modifier = Modifier.size(28.dp)
                // colorFilter = ColorFilter.tint(iconTintColor) // Removed direct tinting for Coil Image
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val words = data.medicationName.split(" ")
                val displayName = if (words.size > 3) {
                    words.take(3).joinToString(" ") + "..."
                } else {
                    data.medicationName
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = mainContentColor,
                    maxLines = 2 // Allow a bit of wrapping for long 3 words, but ellipsis should handle overflow.
                )
                if (data.dosage.isNotBlank()) {
                    Text(
                        text = data.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = subContentColor
                    )
                }

                if (data.isTaken && data.actualTakenTime != null && data.actualTakenTime != data.scheduledTime) {
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 1.dp)) {
                        Text(
                            text = "Taken: ${data.actualTakenTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = mainContentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = data.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = subContentColor
                        )
                    }
                } else if (data.isTaken && data.actualTakenTime != null) {
                     Text(
                        text = "Taken: ${data.actualTakenTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = subContentColor,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = data.isTaken,
                onCheckedChange = data.onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = baseMedicationColor,
                    checkedTrackColor = baseMedicationColor.copy(alpha = SwitchDefaults.TrackAlpha),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledCheckedThumbColor = baseMedicationColor.copy(alpha = 0.5f),
                    disabledCheckedTrackColor = baseMedicationColor.copy(alpha = 0.2f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                enabled = !data.isFuture || data.isTaken,
                modifier = Modifier.size(width = 48.dp, height = 28.dp)
            )
        }
    }
}

// Color.luminance() extension for basic contrast decision (if needed elsewhere, move to a util file)
fun Color.luminance(): Float {
    val red = red * 0.2126f
    val green = green * 0.7152f
    val blue = blue * 0.0722f
    return red + green + blue
}


@Preview(showBackground = true, name = "MedCardTodayFinal - Future", group="Final")
@Composable
fun MedicationCardTodayFinalFuturePreview() {
    AppTheme {
        val sampleType = MedicationType(1, "Pill", null) // imageUrl can be null for preview or use placeholder
        val sampleData = TodayMedicationData("1", 1, "Ibuprofen", "200mg", sampleType, LocalTime.of(14,0), null, false, true, MedicationColor.LIGHT_BLUE, {})
        MedicationCardTodayFinal(data = sampleData, modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp), shape = RoundedCornerShape(12.dp))
    }
}

@Preview(showBackground = true, name = "MedCardTodayFinal - Taken On Time", group="Final")
@Composable
fun MedicationCardTodayFinalTakenOnTimePreview() {
    AppTheme {
        val sampleType = MedicationType(2, "Capsule", "http://example.com/capsule.png")
        val sampleData = TodayMedicationData("2", 2, "Amoxicillin", "500mg", sampleType, LocalTime.of(8,0), LocalTime.of(8,0), true, false, MedicationColor.LIGHT_GREEN, {})
        MedicationCardTodayFinal(data = sampleData, modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp), shape = RoundedCornerShape(12.dp))
    }
}

@Preview(showBackground = true, name = "MedCardTodayFinal - Taken Late", group="Final")
@Composable
fun MedicationCardTodayFinalTakenLatePreview() {
    AppTheme {
        val sampleType = MedicationType(3, "Vitamin D", null)
        val sampleData = TodayMedicationData("3", 3, "Vitamin D", "1000 IU", sampleType, LocalTime.of(9,0), LocalTime.of(9,30), true, false, MedicationColor.LIGHT_YELLOW, {})
        MedicationCardTodayFinal(data = sampleData, modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp), shape = RoundedCornerShape(12.dp))
    }
}

@Preview(showBackground = true, name = "MedCardTodayFinal - Past, Not Taken", group="Final")
@Composable
fun MedicationCardTodayFinalPastNotTakenPreview() {
    AppTheme {
        val sampleType = MedicationType(4, "Paracetamol", null)
        val sampleData = TodayMedicationData("4", 4, "Paracetamol", "500mg", sampleType, LocalTime.of(7,0), null, false, false, MedicationColor.LIGHT_RED, {})
        MedicationCardTodayFinal(data = sampleData, modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp), shape = RoundedCornerShape(12.dp))
    }
}
