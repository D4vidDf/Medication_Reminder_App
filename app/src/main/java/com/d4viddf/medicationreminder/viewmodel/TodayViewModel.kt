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
    val timeGroups: List<TimeGroupDisplayData> = emptyList(), // Changed from groupedReminders Map
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
                        medicationTypeRepository.getMedicationTypeById(typeId) // This is a suspend fun returning MedicationType?
                    } ?: MedicationType.defaultType() // Use defaultType

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

                // Group by scheduledTime and then map to TimeGroupDisplayData
                val finalTimeGroups = processedReminders
                    .groupBy { it.scheduledTime }
                    .map { (time, reminderList) ->
                        TimeGroupDisplayData(scheduledTime = time, reminders = reminderList)
                    }
                    .sortedBy { it.scheduledTime } // Ensure the final list of groups is sorted by time

                _uiState.value = _uiState.value.copy(timeGroups = finalTimeGroups, isLoading = false, error = null)

            } catch (e: Exception) {
                // Ensure timeGroups is empty on error, not just relying on initial state
                _uiState.value = _uiState.value.copy(timeGroups = emptyList(), isLoading = false, error = "Failed to load reminders: ${e.message}")
            }
        }
    }

    private fun handleToggle(reminderId: String, isChecked: Boolean) {
        viewModelScope.launch {
            val currentTimeGroups = _uiState.value.timeGroups
            var reminderData: TodayMedicationData? = null
            var groupIndex = -1

            for (idx in currentTimeGroups.indices) {
                val foundReminder = currentTimeGroups[idx].reminders.find { it.id == reminderId }
                if (foundReminder != null) {
                    reminderData = foundReminder
                    groupIndex = idx
                    break
                }
            }

            if (reminderData == null || groupIndex == -1) return@launch

            val currentTime = _uiState.value.currentTime
            // Use reminderData which is non-null here
            val isActuallyFuture = reminderData!!.scheduledTime.isAfter(currentTime)

            if (isChecked && isActuallyFuture && !reminderData!!.isTaken) {
                _showTakeFutureDialog.value = TakeFutureDialogState(
                    reminderId = reminderId,
                    medicationName = reminderData!!.medicationName,
                    scheduledTime = reminderData!!.scheduledTime
                )
            } else {
                val newActualTakenTime = if (isChecked) currentTime else null
                // Pass groupIndex or scheduledTime for finding the group in updateReminderState
                updateReminderState(reminderId, reminderData!!.scheduledTime, isChecked, newActualTakenTime)
            }
        }
    }

    fun markFutureMedicationAsTaken(reminderId: String, scheduledTime: LocalTime, takeAtCurrentTime: Boolean) {
        viewModelScope.launch {
            val actualTakenTime = if (takeAtCurrentTime) _uiState.value.currentTime else scheduledTime
            // Pass scheduledTime to identify the group
            updateReminderState(reminderId, scheduledTime, true, actualTakenTime)
            _showTakeFutureDialog.value = null
        }
    }

    // timeSlot parameter is now the original scheduledTime of the reminder to find its group
    private fun updateReminderState(reminderId: String, originalScheduledTime: LocalTime, isTaken: Boolean, actualTakenTime: LocalTime?) {
        val currentTimeGroups = _uiState.value.timeGroups

        val targetGroupIndex = currentTimeGroups.indexOfFirst { it.scheduledTime == originalScheduledTime }
        if (targetGroupIndex == -1) return

        val targetGroup = currentTimeGroups[targetGroupIndex]
        val reminderToUpdate = targetGroup.reminders.find { it.id == reminderId } ?: return

        val updatedReminder = reminderToUpdate.copy(
            isTaken = isTaken,
            actualTakenTime = actualTakenTime,
            isFuture = reminderToUpdate.scheduledTime.isAfter(_uiState.value.currentTime) // Re-evaluate isFuture
        )

        val updatedRemindersInGroup = targetGroup.reminders.map {
            if (it.id == reminderId) updatedReminder else it
        }

        val updatedTimeGroup = targetGroup.copy(reminders = updatedRemindersInGroup)

        val newTimeGroups = currentTimeGroups.toMutableList().apply {
            this[targetGroupIndex] = updatedTimeGroup
        }

        _uiState.value = _uiState.value.copy(timeGroups = newTimeGroups)

        // If the dialog was shown for this specific reminder, dismiss it now that action has been taken/processed.
        if (_showTakeFutureDialog.value?.reminderId == reminderId) {
            _showTakeFutureDialog.value = null
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
