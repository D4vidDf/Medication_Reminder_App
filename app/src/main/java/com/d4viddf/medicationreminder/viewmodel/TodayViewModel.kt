package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
// Corrected import for MedicationRepository
import com.d4viddf.medicationreminder.repository.MedicationRepository
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.MedicationTypeRepository
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


// Moved TodayMedicationData here as it's primarily part of this ViewModel's state structure
data class TodayMedicationData(
    val id: String, // Reminder ID or unique key for this specific reminder instance
    val medicationId: Int, // The ID of the medication itself, for fetching details
    val medicationName: String,
    val dosage: String,
    val medicationType: MedicationType,
    val scheduledTime: LocalTime,
    val actualTakenTime: LocalTime? = null,
    val isTaken: Boolean,
    val isFuture: Boolean,
    val medicationColor: MedicationColor,
    val onToggle: (Boolean) -> Unit
)

data class TodayScreenUiState(
    val groupedReminders: Map<LocalTime, List<TodayMedicationData>> = emptyMap(),
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
                // This is a simplified fetching logic.
                // Actual implementation would involve querying schedules, then reminders,
                // then medication details, and medication history for taken status.
                // For now, let's assume medicationReminderRepository.getRemindersForDate(today)
                // gives us objects that can be mapped to TodayMedicationData.

                // Placeholder: Replace with actual data fetching and processing
                // The MedicationSchedule might be a better source if it contains all necessary info
                // or can be combined with Medication and MedicationReminder.

                // Let's assume we have a function that gives us relevant MedicationSchedule objects for today
                val allSchedules = medicationReminderRepository.getAllSchedules().firstOrNull() ?: emptyList()
                val todaySchedules = allSchedules.filter { it.scheduleDate == today } // Simplified

                val remindersData = mutableListOf<TodayMedicationData>()

                for (schedule in todaySchedules) {
                    val medication = medicationRepository.getMedicationById(schedule.medicationId).firstOrNull()
                    val medicationType = medication?.typeId?.let { typeId ->
                        medicationTypeRepository.getMedicationTypeById(typeId).firstOrNull()
                    }

                    if (medication != null && medicationType != null) {
                        // TODO: Determine actualTakenTime and isTaken from history
                        val isTaken = false // Placeholder
                        val actualTakenTime = null // Placeholder

                        remindersData.add(
                            TodayMedicationData(
                                id = schedule.id.toString(), // Or a more specific reminder ID if available
                                medicationId = medication.id, // Added medicationId
                                medicationName = medication.name,
                                dosage = medication.dosage ?: "",
                                medicationType = medicationType,
                                scheduledTime = schedule.scheduledTime, // Assuming MedicationSchedule has LocalTime
                                actualTakenTime = actualTakenTime,
                                isTaken = isTaken,
                                isFuture = schedule.scheduledTime.isAfter(LocalTime.now()), // Initial future status
                                medicationColor = MedicationColor.valueOf(medication.color ?: MedicationColor.LIGHT_GRAY.name),
                                onToggle = { checked -> handleToggle(schedule.id.toString(), checked) }
                            )
                        )
                    }
                }

                // Update isFuture based on current time from state for consistent comparison
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
                // Trying to mark a future, untaken item as taken: Show dialog
                _showTakeFutureDialog.value = TakeFutureDialogState(
                    reminderId = reminderId,
                    medicationName = reminderData!!.medicationName,
                    scheduledTime = reminderData!!.scheduledTime
                )
                // Do not proceed further with toggle; dialog will handle action
            } else {
                // Proceed with normal toggle logic (marking past/present, or un-marking any)
                val newActualTakenTime = if (isChecked) currentTime else null
                updateReminderState(reminderId, timeSlot!!, isChecked, newActualTakenTime)
            }
        }
    }

    fun markFutureMedicationAsTaken(reminderId: String, scheduledTime: LocalTime, takeAtCurrentTime: Boolean) {
        viewModelScope.launch {
            val actualTakenTime = if (takeAtCurrentTime) _uiState.value.currentTime else scheduledTime
            updateReminderState(reminderId, scheduledTime, true, actualTakenTime)
            _showTakeFutureDialog.value = null // Dismiss dialog
        }
    }

    // Helper function to update reminder state and persist (actual persistence is placeholder)
    private fun updateReminderState(reminderId: String, timeSlot: LocalTime, isTaken: Boolean, actualTakenTime: LocalTime?) {
        val currentGroups = _uiState.value.groupedReminders
        val reminderToUpdate = currentGroups[timeSlot]?.find { it.id == reminderId } ?: return

        val updatedReminder = reminderToUpdate.copy(
            isTaken = isTaken,
            actualTakenTime = actualTakenTime,
            isFuture = reminderToUpdate.scheduledTime.isAfter(_uiState.value.currentTime)
        )

        // Persist to DB (placeholder)
            // if (updatedReminder.isTaken) {
            //   historyRepository.addTakenEntry(updatedReminder.id, medicationId, updatedReminder.actualTakenTime!!, updatedReminder.scheduledTime)
            // } else {
            //   historyRepository.removeTakenEntry(updatedReminder.id, updatedReminder.scheduledTime)
            // }

            val newListForSlot = currentGroups[timeSlot]?.map { // Corrected: use timeSlot
                if (it.id == reminderId) updatedReminder else it
            } ?: emptyList()

            val newGroups = currentGroups.toMutableMap()
            newGroups[timeSlot] = newListForSlot // Corrected: use timeSlot

            _uiState.value = _uiState.value.copy(groupedReminders = newGroups)
            _showTakeFutureDialog.value = null // Ensure dialog is dismissed
        }
    }

    fun dismissTakeFutureDialog() {
        _showTakeFutureDialog.value = null
    }

    // Call this if medication taken status changes from outside (e.g. notification)
    fun refreshReminderStatus() {
        loadTodayReminders() // Simplest way to refresh
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
