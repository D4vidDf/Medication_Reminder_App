package com.d4viddf.medicationreminder.ui.components.frequency

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d4viddf.medicationreminder.ui.components.NotificationSelector
import com.d4viddf.medicationreminder.ui.theme.MedicationReminderTheme
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FrequencySelector(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    onDaysSelected: (List<Int>) -> Unit,
    selectedDays: List<Int>,
    onIntervalChanged: (Int, Int) -> Unit,  // hours and minutes
    onTimesSelected: (List<LocalTime>) -> Unit,
    selectedTimes: List<LocalTime>,
    modifier: Modifier = Modifier
) {
    val frequencies = listOf("Once a day", "Multiple times a day", "Interval")

    Column(modifier = modifier.padding(0.dp, 16.dp)) {
        // Title and Dropdown Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminder",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            DropdownMenu(
                selectedFrequency = selectedFrequency,
                options = frequencies,
                onSelectedOption = onFrequencySelected
            )
        }

        // Show NotificationSelector for "Once a day"
        if (selectedFrequency == "Once a day") {
            NotificationSelector(
                onDaysSelected = onDaysSelected,
                selectedDays = selectedDays
            )
        }

        // Show IntervalSelector for "Interval"
        if (selectedFrequency == "Interval") {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interval",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = {
                        // Show info dialog about interval
                    }
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info")
                }
            }
            IntervalSelector(
                onIntervalChanged = onIntervalChanged,
            )
        }

        // Show Custom Alarms for "Multiple times a day"
        if (selectedFrequency == "Multiple times a day") {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Alarms",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = {
                        // Show info dialog about custom alarms
                    }
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info")
                }
            }
            CustomAlarmsSelector(
                selectedTimes = selectedTimes,
                onTimesSelected = onTimesSelected
            )
        }
    }
}

@Composable
fun DropdownMenu(
    selectedFrequency: String,
    options: List<String>,
    onSelectedOption: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(64.dp)
            )
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(64.dp))
                .padding(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Text(selectedFrequency, style = MaterialTheme.typography.bodyLarge)
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelectedOption(option)
                    }
                )
            }
        }
    }
}
