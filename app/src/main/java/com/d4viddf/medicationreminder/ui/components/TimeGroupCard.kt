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
    val baseMedicationColor = data.medicationColor.getColor()
    // Colors based on state (similar to MedicationCardTodayFinal but for a Row background)
    val itemBackgroundColor = if (data.isTaken) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    } else if (data.isFuture) {
        baseMedicationColor.copy(alpha = 0.20f) // Lighter alpha for non-card background
    } else {
        Color.Transparent // Or a very subtle background if needed for past, untaken
    }
    val contentColor = if (data.isTaken || !data.isFuture) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        // Basic contrast for text on colored background, might need adjustment
        if (baseMedicationColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(itemBackgroundColor, shape = shape) // Apply background and shape to the Row
            .clip(shape) // Clip content to the shape
            .padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = data.medicationType.imageUrl,
                placeholder = painterResource(id = R.drawable.ic_medication_24),
                error = painterResource(id = R.drawable.ic_medication_24)
            ),
            contentDescription = data.medicationType.name, // TODO: Use stringResource
            modifier = Modifier.size(40.dp) // Adjusted size
        )

        Spacer(modifier = Modifier.width(16.dp)) // Adjusted spacer

        Column(modifier = Modifier.weight(1f)) {
            val words = data.medicationName.split(" ")
            val displayName = if (words.size > 3) {
                words.take(3).joinToString(" ") + "..."
            } else {
                data.medicationName
            }
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Adjusted style
                color = contentColor,
                maxLines = 1, // Ensure single line for name like original card
                overflow = TextOverflow.Ellipsis // Add ellipsis for overflow
            )
            if (data.dosage.isNotBlank()) {
                Text(
                    text = data.dosage,
                    style = MaterialTheme.typography.bodyLarge, // Adjusted style
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            // "Taken at" time display can remain as is, or be adjusted if needed
            if (data.isTaken && data.actualTakenTime != null && data.actualTakenTime != data.scheduledTime) {
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) { // Increased top padding slightly
                    Text("Taken: ${data.actualTakenTime.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = contentColor)
                    Spacer(Modifier.width(4.dp))
                    Text(data.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough), color = contentColor.copy(alpha = 0.7f))
                }
            } else if (data.isTaken && data.actualTakenTime != null) {
                Text("Taken: ${data.actualTakenTime.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f), modifier = Modifier.padding(top = 2.dp)) // Increased top padding
            }
        }
        Spacer(modifier = Modifier.width(12.dp)) // Adjusted spacer
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
