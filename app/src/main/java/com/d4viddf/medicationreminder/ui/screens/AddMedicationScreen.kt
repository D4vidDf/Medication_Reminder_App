package com.d4viddf.medicationreminder.ui.screens

import MedicationSearchResult
import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.ui.components.ColorSelector
import com.d4viddf.medicationreminder.ui.components.FrequencySelector
import com.d4viddf.medicationreminder.ui.components.GenericTextFieldInput
import com.d4viddf.medicationreminder.ui.components.MedicationNameInput
import com.d4viddf.medicationreminder.ui.components.MedicationTypeSelector
import com.d4viddf.medicationreminder.viewmodel.MedicationViewModel
import com.d4viddf.medicationreminder.viewmodel.MedicationScheduleViewModel
import com.d4viddf.medicationreminder.viewmodel.MedicationInfoViewModel
import com.d4viddf.medicationreminder.data.Medication
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.MedicationInfo
import com.d4viddf.medicationreminder.data.ScheduleType
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    medicationViewModel: MedicationViewModel = hiltViewModel(),
    medicationScheduleViewModel: MedicationScheduleViewModel = hiltViewModel(),
    medicationInfoViewModel: MedicationInfoViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(0) }
    var selectedTypeId by remember { mutableStateOf(1) }
    var selectedColor by remember { mutableStateOf(Color(0xFF264443)) }
    var startDate by remember { mutableStateOf("Select Start Date") }
    var endDate by remember { mutableStateOf("Select End Date") }
    var frequency by remember { mutableStateOf("Once a day") }
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var packageSize by remember { mutableStateOf("") }
    var selectedTimes by remember { mutableStateOf(listOf<LocalTime>()) }
    var intervalHours by remember { mutableStateOf(0) }
    var intervalMinutes by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(listOf<Int>()) }

    // Medication search result to be saved
    var medicationSearchResult by remember { mutableStateOf<MedicationSearchResult?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medication") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { if (step > 0) step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                val isNextButtonEnabled = when (step) {
                    0 -> medicationName.isNotBlank()
                    1 -> dosage.isNotBlank() && packageSize.isNotBlank() && packageSize.toIntOrNull() != null && packageSize.toInt() > 0
                    else -> true
                }

                FilledTonalButton(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            coroutineScope.launch {
                                val medicationId = medicationViewModel.insertMedication(
                                    Medication(
                                        name = medicationName,
                                        typeId = selectedTypeId,
                                        color = selectedColor.hashCode(),
                                        dosage = if (dosage.isNotEmpty()) dosage else null,
                                        packageSize = packageSize.toInt(),
                                        remainingDoses = packageSize.toInt(),
                                        startDate = if (startDate != "Select Start Date") startDate else null,
                                        endDate = if (endDate != "Select End Date") endDate else null,
                                        reminderTime = null
                                    )
                                )

                                medicationId?.let {
                                    // Save Medication Schedule
                                    val scheduleType = when (frequency) {
                                        "Once a day" -> ScheduleType.DAILY
                                        "Weekly" -> ScheduleType.WEEKLY
                                        "As Needed" -> ScheduleType.AS_NEEDED
                                        "Interval" -> ScheduleType.INTERVAL
                                        "Multiple times a day" -> ScheduleType.CUSTOM_ALARMS
                                        else -> ScheduleType.DAILY
                                    }

                                    medicationScheduleViewModel.insertSchedule(
                                        MedicationSchedule(
                                            medicationId = it,
                                            scheduleType = scheduleType,
                                            intervalHours = if (frequency == "Interval") intervalHours else null,
                                            intervalMinutes = if (frequency == "Interval") intervalMinutes else null,
                                            daysOfWeek = if (frequency == "Once a day" || frequency == "Weekly") selectedDays.joinToString(",") else null,
                                            specificTimes = if (frequency == "Multiple times a day") selectedTimes.joinToString(",") { time ->
                                                time.toString()
                                            } else null
                                        )
                                    )

                                    // Save Medication Info
                                    medicationSearchResult?.let { result ->
                                        medicationInfoViewModel.insertMedicationInfo(
                                            MedicationInfo(
                                                medicationId = it,
                                                description = result.description,
                                                atcCode = result.atcCode,
                                                safetyNotes = result.safetyNotes,
                                                administrationRoutes = result.administrationRoutes.joinToString(","),
                                                dosage = result.dosage,
                                                documentUrls = result.documentUrls.joinToString(","),
                                                nregistro = result.nregistro,
                                                labtitular = result.labtitular,
                                                comercializado = result.comercializado,
                                                requiereReceta = result.requiereReceta,
                                                generico = result.generico
                                            )
                                        )
                                    }
                                }
                                onNavigateBack()
                            }
                        }
                    },
                    enabled = isNextButtonEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                    )
                ) {
                    Text(if (step < 3) "Next" else "Save Medication")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (step) {
                0 -> {
                    MedicationTypeSelector(
                        selectedTypeId = selectedTypeId,
                        onTypeSelected = { selectedTypeId = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MedicationNameInput(
                        medicationName = medicationName,
                        onMedicationNameChange = { medicationName = it },
                        onMedicationSelected = { result ->
                            medicationSearchResult = result
                            if (result != null) {
                                medicationName = result.name
                                dosage = result.dosage ?: ""
                            }
                        }
                    )
                }
                1 -> {
                    GenericTextFieldInput(
                        label = "Dosage",
                        value = dosage,
                        onValueChange = { dosage = it },
                        description = "Enter the dosage as indicated by your healthcare provider.",
                        isError = dosage.isBlank()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GenericTextFieldInput(
                        label = "Package Size",
                        value = packageSize,
                        onValueChange = { packageSize = it },
                        description = "Enter the number of doses available in the package.",
                        isError = packageSize.toIntOrNull() == null || packageSize.toInt() <= 0
                    )
                }
                2 -> {
                    FrequencySelector(
                        selectedFrequency = frequency,
                        onFrequencySelected = { frequency = it },
                        selectedDays = selectedDays,
                        onDaysSelected = { selectedDays = it },
                        selectedTimes = selectedTimes,
                        onTimesSelected = { selectedTimes = it },
                        onIntervalChanged = { hours, minutes ->
                            intervalHours = hours
                            intervalMinutes = minutes
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    DatePickerButton(
                        label = "Start Date",
                        date = startDate,
                        onDateSelected = { startDate = it },
                        context = context
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DatePickerButton(
                        label = "End Date",
                        date = endDate,
                        onDateSelected = { endDate = it },
                        context = context
                    )
                }
                3 -> {
                    MedicationSummary(
                        typeId = selectedTypeId,
                        medicationName = medicationName,
                        color = selectedColor,
                        dosage = dosage,
                        packageSize = packageSize,
                        frequency = frequency,
                        startDate = startDate,
                        endDate = endDate
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ColorSelector(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }
            }
        }
    }
}


@Composable
fun DatePickerButton(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit,
    context: Context
) {
    val calendar = Calendar.getInstance()

    Button(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = "$label: $date")
    }
}

@Composable
fun MedicationSummary(
    typeId: Int,
    medicationName: String,
    color: Color,
    dosage: String,
    packageSize: String,
    frequency: String,
    startDate: String,
    endDate: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Medication Summary", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Medication Name: $medicationName")
        Text(text = "Medication Type ID: $typeId")
        Text(text = "Dosage: $dosage")
        Text(text = "Package Size: $packageSize")
        Text(text = "Frequency: $frequency")
        Text(text = "Start Date: $startDate")
        Text(text = "End Date: $endDate")
    }
}
