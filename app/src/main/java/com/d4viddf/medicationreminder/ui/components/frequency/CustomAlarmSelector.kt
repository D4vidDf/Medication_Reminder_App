package com.d4viddf.medicationreminder.ui.components.frequency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomAlarmsSelector(
    selectedTimes: List<LocalTime>,
    onTimesSelected: (List<LocalTime>) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                // For simplicity, simulate adding a new time here
                // You can replace this with an actual time picker dialog
                val newTime = LocalTime.now()
                val updatedTimes = selectedTimes.toMutableList().apply { add(newTime) }
                onTimesSelected(updatedTimes)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Alarm")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Display selected times
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTimes.forEach { time ->
                AlarmChip(
                    time = time,
                    onDelete = {
                        // Remove the selected time
                        val updatedTimes = selectedTimes.toMutableList().apply { remove(time) }
                        onTimesSelected(updatedTimes)
                    }
                )
            }
        }
    }
}

@Composable
fun AlarmChip(
    time: LocalTime,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.wrapContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Delete Alarm", tint = Color.Red)
            }
        }
    }
}
