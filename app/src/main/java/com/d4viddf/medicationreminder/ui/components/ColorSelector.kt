package com.d4viddf.medicationreminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.ui.colors.medicationColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelector(
    selectedColor: MedicationColor,
    onColorSelected: (MedicationColor) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Color selection row
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showBottomSheet = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.color_selector_label), modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(selectedColor.backgroundColor, CircleShape).semantics{
                        // Add content description for accessibility
                        contentDescription = stringResource(R.string.color_selector_selected_color_description_prefix) + medicationColorToString(selectedColor)
        }

            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(medicationColorToString(selectedColor))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.color_selector_expand_description))
        }
    }

    // Bottom sheet with color grid
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.color_selector_label), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(medicationColors.size) { index ->
                        val color = medicationColors[index]
                        val isSelected = color == selectedColor
                        val cornerShape = when (index) {
                            0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                            2 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 16.dp, bottomEnd = 8.dp)
                            9 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 16.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                            11 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 8.dp, bottomEnd = 16.dp)
                            else -> RoundedCornerShape(8.dp)
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .background(color.backgroundColor, cornerShape)
                                .clickable {
                                    onColorSelected(color)
                                    showBottomSheet = false
                                }
                                .semantics{
                                    // Add content description for accessibility
                                    contentDescription = stringResource(R.string.color_selector_color_description_prefix) + medicationColorToString(color)
                                },
                            contentAlignment = Alignment.Center,

                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.color_selector_selected_icon_description),
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun medicationColorToString(color: MedicationColor): String {
    return when (color) {
        MedicationColor.ORANGE -> stringResource(R.string.medication_color_orange)
        MedicationColor.PINK -> stringResource(R.string.medication_color_pink)
        MedicationColor.GREEN -> stringResource(R.string.medication_color_dark_green)
        MedicationColor.BLUE -> stringResource(R.string.medication_color_blue)
        MedicationColor.PURPLE -> stringResource(R.string.medication_color_purple)
        MedicationColor.YELLOW -> stringResource(R.string.medication_color_golden)
        MedicationColor.LIGHT_YELLOW -> stringResource(R.string.medication_color_light_yellow)
        MedicationColor.LIGHT_ORANGE -> stringResource(R.string.medication_color_light_orange)
        MedicationColor.LIGHT_PINK -> stringResource(R.string.medication_color_light_pink)
        MedicationColor.LIGHT_PURPLE -> stringResource(R.string.medication_color_light_purple)
        MedicationColor.LIGHT_GREEN -> stringResource(R.string.medication_color_light_green)
        MedicationColor.LIGHT_BLUE -> stringResource(R.string.medication_color_light_blue)
    }
}