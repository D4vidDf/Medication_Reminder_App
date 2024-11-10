package com.d4viddf.medicationreminder.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationSelector(
    onDaysSelected: (List<Int>) -> Unit,
    selectedDays: List<Int>
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val scrollState = rememberScrollState()
    var selectedDaysState by remember { mutableStateOf(selectedDays) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Select the days",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp)) // Padding at the start
            daysOfWeek.forEachIndexed { index, day ->
                val isSelected = selectedDaysState.contains(index + 1)
                val color = if (isSelected) Color(0xFF264443) else Color(0xFFEFF0F4)
                val textColor = if (isSelected) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(100.dp)
                        .background(color, shape = RoundedCornerShape(50))
                        .clickable {
                            selectedDaysState = if (isSelected) {
                                selectedDaysState - (index + 1)
                            } else {
                                selectedDaysState + (index + 1)
                            }
                            onDaysSelected(selectedDaysState)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (isSelected) Color(0xFFF0BF70) else Color.White,
                                    shape = RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp)) // Padding at the end
        }
    }
}
