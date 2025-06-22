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
                style = MaterialTheme.typography.titleMedium, // Or titleLarge for more prominence
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp) // Padding for the time header
            )

            // List of medication cards for this time group
            timeGroupData.reminders.forEachIndexed { index, reminderData ->
                val cardShape = getReminderCardShapeWithinGroup(
                    index = index,
                    totalItemsInGroup = timeGroupData.reminders.size
                )
                MedicationCardTodayFinal(
                    data = reminderData, // isFuture will be determined by TodayScreen/ViewModel logic when creating TodayMedicationData
                    shape = cardShape,
                    modifier = Modifier
                        .padding(horizontal = 8.dp) // Horizontal padding for cards within the group
                        .padding(top = if (index == 0) 8.dp else 2.dp, bottom = if (index == timeGroupData.reminders.size -1) 4.dp else 2.dp)
                        .clickable { onReminderClick(reminderData.medicationId) }
                )
            }
        }
    }
}

fun getReminderCardShapeWithinGroup(index: Int, totalItemsInGroup: Int): RoundedCornerShape {
    val outerCornerRadius = 0.dp // Group card has the outer rounding
    val innerCornerRadius = 8.dp // Rounding for items within the group, if they are distinct

    // If we want items inside the group to also look connected:
    if (totalItemsInGroup == 1) return RoundedCornerShape(innerCornerRadius)
    return when (index) {
        0 -> RoundedCornerShape(topStart = innerCornerRadius, topEnd = innerCornerRadius, bottomStart = 2.dp, bottomEnd = 2.dp)
        totalItemsInGroup - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = innerCornerRadius, bottomEnd = innerCornerRadius)
        else -> RoundedCornerShape(2.dp) // Middle items slightly rounded to detach a bit
    }
    // Alternative: all items inside have same rounded corners e.g. RoundedCornerShape(8.dp)
    // For now, using the connected look logic.
}


@Preview(showBackground = true, name = "TimeGroupCard - Single Item")
@Composable
fun TimeGroupCardSingleItemPreview() {
    AppTheme {
        val reminder = TodayMedicationData(
            id = "1", medicationId = 101, medicationName = "Aspirin", dosage = "100mg",
            medicationType = MedicationType(1, "Pill", null),
            scheduledTime = LocalTime.of(8, 0), isTaken = false, isFuture = false,
            medicationColor = MedicationColor.LIGHT_BLUE, onToggle = {}
        )
        val groupData = TimeGroupDisplayData(
            scheduledTime = LocalTime.of(8, 0),
            reminders = listOf(reminder)
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
            reminders = listOf(reminder1, reminder2, reminder3)
        )
        TimeGroupCard(timeGroupData = groupData, onReminderClick = {})
    }
}
