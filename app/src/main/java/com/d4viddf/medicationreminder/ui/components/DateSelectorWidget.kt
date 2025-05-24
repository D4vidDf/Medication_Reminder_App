package com.d4viddf.medicationreminder.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.d4viddf.medicationreminder.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelectorWidget(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember { generateDatesList() } // Generate a list of dates, e.g., the upcoming week.

    LazyRow {
        items(dates) { date ->
            val isSelected = date == selectedDate
            DateItem(
                date = date,
                isSelected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    val itemStateDescription = if (isSelected) stringResource(R.string.state_selected) else stringResource(R.string.state_not_selected)
    val fullDateDescription = stringResource(
        R.string.date_item_desc,
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
        date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), // Using SHORT for month to be less verbose visually if needed, but FULL for a11y
        date.dayOfMonth,
        itemStateDescription
    )

    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .semantics { contentDescription = fullDateDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text( // Visually, keep it short
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateDatesList(): List<LocalDate> {
    val today = LocalDate.now()
    return (0..6).map { today.plusDays(it.toLong()) } // Generate a week starting from today.
}
