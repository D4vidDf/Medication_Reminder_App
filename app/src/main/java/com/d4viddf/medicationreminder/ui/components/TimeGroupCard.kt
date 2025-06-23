package com.d4viddf.medicationreminder.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import com.d4viddf.medicationreminder.viewmodel.TimeGroupDisplayData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeGroupCard(
    modifier: Modifier = Modifier,
    timeGroupData: TimeGroupDisplayData,
    onReminderClick: (medicationId: Int) -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) { // Padding for content inside the group card
            Text(
                text = timeGroupData.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.titleLarge, // More prominent time
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            )

            Text(
                text = "${timeGroupData.takenCount} / ${timeGroupData.totalInGroup} Taken", // Taken dosage summary
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            // Divider line
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // List of reminder items for this time group
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) { // Add some padding around the list of items
                timeGroupData.reminders.forEachIndexed { index, reminderData ->
                    val itemShape = getReminderItemShapeWithinGroup( // Renamed from getReminderCardShapeWithinGroup
                        index = index,
                        totalItemsInGroup = timeGroupData.reminders.size
                    )
                    // Using ReminderItemRow (defined below or imported if made public)
                    // instead of MedicationCardTodayFinal to avoid card-in-card
                    ReminderItemRow(
                        data = reminderData,
                        shape = itemShape,
                        modifier = Modifier
                            .clickable { onReminderClick(reminderData.medicationId) }
                            .padding(horizontal = 8.dp) // Padding for each item row
                    )
                }
            }
        }
    }
}

// Shape logic for items within the group
private fun getReminderItemShapeWithinGroup(index: Int, totalItemsInGroup: Int): RoundedCornerShape {
    val standardCornerRadius = 24.dp // Match original MedicationCard radius
    val connectedEdgeRadius = 2.dp // Minimal rounding for connected edges to maintain slight separation

    return when {
        totalItemsInGroup == 1 -> RoundedCornerShape(standardCornerRadius) // Single item gets full standard rounding
        index == 0 -> RoundedCornerShape(topStart = standardCornerRadius, topEnd = standardCornerRadius, bottomStart = connectedEdgeRadius, bottomEnd = connectedEdgeRadius)
        index == totalItemsInGroup - 1 -> RoundedCornerShape(topStart = connectedEdgeRadius, topEnd = connectedEdgeRadius, bottomStart = standardCornerRadius, bottomEnd = standardCornerRadius)
        else -> RoundedCornerShape(connectedEdgeRadius) // Middle items
    }
}

// New Composable for individual reminder item (row) - defined within this file for now
@Composable
private fun ReminderItemRow(
    modifier: Modifier = Modifier,
    data: TodayMedicationData,
    shape: RoundedCornerShape
) {
    // Use properties from MedicationColor enum directly
    val primaryMedicationColor = data.medicationColor.backgroundColor
    val onPrimaryMedicationColor = data.medicationColor.textColor // Or onBackgroundColor, decide on one

    val itemBackgroundColor = if (data.isTaken) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) // Slightly more distinct when taken
    } else if (data.isFuture) {
        primaryMedicationColor.copy(alpha = 0.20f)
    } else {
        Color.Transparent
    }

    val contentColor = if (data.isTaken || !data.isFuture) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        onPrimaryMedicationColor // Use the 'on' color from the enum for future items
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(itemBackgroundColor, shape = shape)
            .clip(shape)
            .padding(horizontal = 16.dp, vertical = 16.dp), // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically
        // horizontalArrangement = Arrangement.SpaceBetween // Will be handled by weights and Spacers
    ) {
        // Left part: Image and Time (if not taken or taken at scheduled time)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = data.medicationType.imageUrl,
                    placeholder = painterResource(id = R.drawable.ic_medication_24), // Ensure this drawable exists
                    error = painterResource(id = R.drawable.ic_medication_24)
                ),
                contentDescription = data.medicationType.name, // TODO: Use stringResource
                modifier = Modifier.size(48.dp) // Increased image size
            )
            // Display scheduled time if not taken, or taken at a different time (original time)
            if (!data.isTaken || (data.isTaken && data.actualTakenTime != null && data.actualTakenTime != data.scheduledTime)) {
                Text(
                    text = data.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.isTaken) contentColor.copy(alpha = 0.7f) else contentColor, // Mute if taken and different
                    textDecoration = if (data.isTaken && data.actualTakenTime != null && data.actualTakenTime != data.scheduledTime) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }


        Spacer(modifier = Modifier.width(16.dp))

        // Center part: Medication Name and Dosage
        Column(modifier = Modifier.weight(1f)) {
            val displayName = data.medicationName.split(" ").joinToString(" ") // No explicit truncation with "..."
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), // Slightly larger name
                color = contentColor,
                maxLines = 2, // Allow wrapping up to 2 lines
                overflow = TextOverflow.Ellipsis
            )
            if (data.dosage.isNotBlank()) {
                Text(
                    text = data.dosage,
                    style = MaterialTheme.typography.bodyMedium, // Slightly larger dosage
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right part: "Taken at HH:MM" status (if taken) and Switch
        Column(horizontalAlignment = Alignment.End) {
            if (data.isTaken && data.actualTakenTime != null) {
                Text(
                    text = "Taken: ${data.actualTakenTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                // Add a spacer to maintain height if "Taken" text is not visible,
                // to keep switch alignment consistent.
                Spacer(modifier = Modifier.height(MaterialTheme.typography.labelMedium.lineHeight.value.dp + 4.dp))
            }
            Switch(
                checked = data.isTaken,
                onCheckedChange = data.onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = primaryMedicationColor,
                    checkedTrackColor = primaryMedicationColor.copy(alpha = SwitchDefaults.TrackAlpha),
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


@Preview(showBackground = true, name = "TimeGroupCard - Single Item")
@Composable
fun TimeGroupCardSingleItemPreview() {
    AppTheme {
        val reminder = TodayMedicationData(
            id = "1", medicationId = 101, medicationName = "Aspirin", dosage = "100mg",
            medicationType = MedicationType(1, "Pill", null), // Corrected MedicationType constructor
            scheduledTime = LocalTime.of(8, 0), isTaken = false, isFuture = false,
            medicationColor = MedicationColor.LIGHT_BLUE, onToggle = {}
        )
        val groupData = TimeGroupDisplayData(
            scheduledTime = LocalTime.of(8, 0),
            reminders = listOf(reminder),
            takenCount = 0,
            totalInGroup = 1
        )
        TimeGroupCard(timeGroupData = groupData, onReminderClick = {})
    }
}

@Preview(showBackground = true, name = "TimeGroupCard - Multiple Items")
@Composable
fun TimeGroupCardMultipleItemsPreview() {
    AppTheme {
        val reminder1 = TodayMedicationData(
            id = "1", medicationId = 101, medicationName = "Aspirin Extra Strength Long Name", dosage = "100mg",
            medicationType = MedicationType(1, "Pill", null),
            scheduledTime = LocalTime.of(9, 0), isTaken = false, isFuture = true,
            medicationColor = MedicationColor.LIGHT_RED, onToggle = {}
        )
        val reminder2 = TodayMedicationData(
            id = "2", medicationId = 102, medicationName = "Vitamin C", dosage = "500mg",
            medicationType = MedicationType(2, "Capsule", null),
            scheduledTime = LocalTime.of(9, 0), isTaken = true, actualTakenTime = LocalTime.of(9,5), isFuture = false,
            medicationColor = MedicationColor.LIGHT_GREEN, onToggle = {}
        )
        val reminder3 = TodayMedicationData(
            id = "3", medicationId = 103, medicationName = "Metformin", dosage = "500mg",
            medicationType = MedicationType(1, "Pill", null),
            scheduledTime = LocalTime.of(9, 0), isTaken = false, isFuture = false,
            medicationColor = MedicationColor.LIGHT_PURPLE, onToggle = {}
        )
        val groupData = TimeGroupDisplayData(
            scheduledTime = LocalTime.of(9, 0),
            reminders = listOf(reminder1, reminder2, reminder3),
            takenCount = 1, // reminder2 is taken
            totalInGroup = 3
        )
        TimeGroupCard(timeGroupData = groupData, onReminderClick = {})
    }
}
