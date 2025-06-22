package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
// Corrected import for MedicationRepository
import com.d4viddf.medicationreminder.repository.MedicationRepository
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.MedicationTypeRepository
// Removed local definition, ensure this import is correct if TodayMedicationData is in its own file in ui.components
import com.d4viddf.medicationreminder.ui.components.TodayMedicationData
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.data.MedicationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
// import kotlinx.coroutines.flow.firstOrNull // Ensure this is present - replaced by take and singleOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import android.util.Log // Added import for Log
import java.time.LocalDateTime

// TodayMedicationData class definition is now removed from here.
// It should be in app/src/main/java/com/d4viddf/medicationreminder/ui/components/TodayMedicationData.kt

data class TodayScreenUiState(
    val groupedReminders: Map<LocalTime, List<TodayMedicationData>> = emptyMap(), // Uses imported TodayMedicationData
    val currentTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val medicationReminderRepository: MedicationReminderRepository,
    private val medicationRepository: MedicationRepository, // To get medication details like name, type, color
    private val medicationTypeRepository: MedicationTypeRepository // To get MedicationType object
    // Add other repositories if needed, e.g., for medication history to check if taken
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayScreenUiState())
    val uiState: StateFlow<TodayScreenUiState> = _uiState.asStateFlow()

    // Dialog state
    data class TakeFutureDialogState(
    val reminderId: String, // This is TodayMedicationData.id
        val medicationName: String,
        val scheduledTime: LocalTime
    )
    private val _showTakeFutureDialog = MutableStateFlow<TakeFutureDialogState?>(null)
    val showTakeFutureDialog: StateFlow<TakeFutureDialogState?> = _showTakeFutureDialog.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadTodayReminders()
        startCurrentTimeUpdater()
    }

    private fun startCurrentTimeUpdater() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(currentTime = LocalTime.now())
                delay(1000 * 30) // Update every 30 seconds, adjust as needed
            }
        }
    }

    fun loadTodayReminders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val today = LocalDate.now()
                // Using take(1).singleOrNull()
                val allMedications: List<com.d4viddf.medicationreminder.data.Medication>? =
                    medicationRepository.getAllMedications().take(1).singleOrNull()
                val remindersData = mutableListOf<TodayMedicationData>()
                val isoDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

                for (medication in (allMedications ?: emptyList())) {
                    val medicationReminders: List<com.d4viddf.medicationreminder.data.MedicationReminder>? =
                        medicationReminderRepository.getRemindersForMedication(medication.id).take(1).singleOrNull()

                    val medicationType = medication.typeId?.let { typeId ->
                        val typeResult: com.d4viddf.medicationreminder.data.MedicationType? =
                            medicationTypeRepository.getMedicationTypeById(typeId).take(1).singleOrNull()
                        typeResult
                    } ?: MedicationType.defaultType() // Use defaultType now that it's fixed

                    for (reminder in (medicationReminders ?: emptyList())) { // Iterate over potentially null list
                        try {
                            val scheduledLocalDateTime = LocalDateTime.parse(reminder.reminderTime, isoDateTimeFormatter)
                            if (scheduledLocalDateTime.toLocalDate() == today) {
                                val actualTakenTimeLocal: LocalTime? = reminder.takenAt?.let {
                                    try { LocalDateTime.parse(it, isoDateTimeFormatter).toLocalTime() }
                                    catch (e: Exception) { null }
                                }

                                remindersData.add(
                                    TodayMedicationData(
                                        id = reminder.id.toString(),
                                        medicationId = medication.id,
                                        medicationName = medication.name,
                                        dosage = medication.dosage ?: "",
                                        medicationType = medicationType, // medicationType will not be null here
                                        scheduledTime = scheduledLocalDateTime.toLocalTime(),
                                        actualTakenTime = actualTakenTimeLocal,
                                        isTaken = reminder.isTaken,
                                        isFuture = scheduledLocalDateTime.toLocalTime().isAfter(LocalTime.now()),
                                        medicationColor = MedicationColor.valueOf(medication.color ?: MedicationColor.LIGHT_ORANGE.name), // Default to LIGHT_ORANGE if color is null
                                        onToggle = { checked -> handleToggle(reminder.id.toString(), checked) }
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("TodayViewModel", "Error processing reminder ${reminder.id} for med ${medication.name}: ${e.message}")
                        }
                    }
                }

                val currentTimeFromState = _uiState.value.currentTime
                val processedReminders = remindersData.map { data ->
                    data.copy(isFuture = data.scheduledTime.isAfter(currentTimeFromState))
                }.sortedBy { it.scheduledTime }

                val grouped = processedReminders.groupBy { it.scheduledTime }
                _uiState.value = _uiState.value.copy(groupedReminders = grouped, isLoading = false, error = null)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load reminders: ${e.message}")
            }
        }
    }

    private fun handleToggle(reminderId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val currentGroups = _uiState.value.groupedReminders
            var reminderData: TodayMedicationData? = null
            var timeSlot: LocalTime? = null

            currentGroups.forEach { (ts, list) ->
                list.find { it.id == reminderId }?.let {
                    reminderData = it
                    timeSlot = ts
                    return@forEach
                }
            }

            if (reminderData == null || timeSlot == null) return@launch

            val currentTime = _uiState.value.currentTime
            val isActuallyFuture = reminderData!!.scheduledTime.isAfter(currentTime)

            if (isChecked && isActuallyFuture && !reminderData!!.isTaken) {
                _showTakeFutureDialog.value = TakeFutureDialogState( // Corrected: Use _showTakeFutureDialog
                    reminderId = reminderId,
                    medicationName = reminderData!!.medicationName,
                    scheduledTime = reminderData!!.scheduledTime
                )
            } else {
                val newActualTakenTime = if (isChecked) currentTime else null
                updateReminderState(reminderId, timeSlot!!, isChecked, newActualTakenTime)
            }
        }
    }

    fun markFutureMedicationAsTaken(reminderId: String, scheduledTime: LocalTime, takeAtCurrentTime: Boolean) {
        viewModelScope.launch {
            val actualTakenTime = if (takeAtCurrentTime) _uiState.value.currentTime else scheduledTime
            updateReminderState(reminderId, scheduledTime, true, actualTakenTime)
            _showTakeFutureDialog.value = null // Corrected: Use _showTakeFutureDialog
        }
    }

    private fun updateReminderState(reminderId: String, timeSlot: LocalTime, isTaken: Boolean, actualTakenTime: LocalTime?) {
        val currentGroups = _uiState.value.groupedReminders
        val reminderToUpdate = currentGroups[timeSlot]?.find { it.id == reminderId } ?: return

        val updatedReminder = reminderToUpdate.copy(
            isTaken = isTaken,
            actualTakenTime = actualTakenTime,
            isFuture = reminderToUpdate.scheduledTime.isAfter(_uiState.value.currentTime)
        )

        val newListForSlot = currentGroups[timeSlot]?.map {
            if (it.id == reminderId) updatedReminder else it
        } ?: emptyList()

        val newGroups = currentGroups.toMutableMap()
        newGroups[timeSlot] = newListForSlot

        _uiState.value = _uiState.value.copy(groupedReminders = newGroups)
        if(isTaken || !reminderToUpdate.isFuture) { // Only dismiss dialog if not triggering it again
             _showTakeFutureDialog.value = null // Corrected: Use _showTakeFutureDialog
        }
    }

    fun dismissTakeFutureDialog() {
        _showTakeFutureDialog.value = null // Corrected: Use _showTakeFutureDialog
    }

    fun refreshReminderStatus() { // Corrected: Use loadTodayReminders
        loadTodayReminders()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Corrected: Use timerJob
    }
} // Added missing class closing brace
