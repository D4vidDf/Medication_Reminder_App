package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.MedicationScheduleRepository
import com.d4viddf.medicationreminder.data.ScheduleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MedicationScheduleViewModel @Inject constructor(
    private val scheduleRepository: MedicationScheduleRepository
) : ViewModel() {

    private val _activeSchedule = MutableStateFlow<MedicationSchedule?>(null) // For individual detail view
    val activeSchedule: StateFlow<MedicationSchedule?> = _activeSchedule.asStateFlow()

    private val _dosesPerDay = MutableStateFlow<Int?>(null) // For individual detail view
    val dosesPerDay: StateFlow<Int?> = _dosesPerDay.asStateFlow()

    private val _schedulesMap = MutableStateFlow<Map<Int, MedicationSchedule?>>(emptyMap()) // For HomeScreen
    val schedulesMap: StateFlow<Map<Int, MedicationSchedule?>> = _schedulesMap.asStateFlow()

    fun insertSchedule(schedule: MedicationSchedule) {
        viewModelScope.launch {
            scheduleRepository.insertSchedule(schedule)
        }
    }

    fun updateSchedule(schedule: MedicationSchedule) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: MedicationSchedule) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
        }
    }

    fun loadActiveScheduleForMedication(medicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduleRepository.getSchedulesForMedication(medicationId)
                .map { schedules -> schedules.firstOrNull() } // Assuming one active schedule per medication
                .collect { schedule ->
                    _activeSchedule.value = schedule
                    calculateDosesPerDay(schedule)
                }
        }
    }

    private fun calculateDosesPerDay(schedule: MedicationSchedule?) {
        if (schedule == null) {
            _dosesPerDay.value = null
            return
        }

        _dosesPerDay.value = when (schedule.scheduleType) {
            ScheduleType.DAILY, ScheduleType.CUSTOM_ALARMS -> {
                schedule.specificTimes?.split(',')?.count { it.isNotBlank() } ?: 0
            }
            ScheduleType.INTERVAL -> {
                // For INTERVAL, as per instructions, returning null for now.
                // A more complex calculation like (24 * 60) / (intervalHours * 60 + intervalMinutes) could be done.
                // But this depends on whether intervalStartTime and intervalEndTime define a daily active window.
                // For now, null indicates "Interval dosing" or similar should be displayed.
                null
            }
            ScheduleType.WEEKLY -> { // Assuming weekly also has specific times for active days.
                schedule.specificTimes?.split(',')?.count { it.isNotBlank() } ?: 0
            }
            else -> null // AS_NEEDED or other types
        }
    }

    // Keep the suspend function if it's used elsewhere for direct, one-time fetches.
    suspend fun getActiveScheduleForMedicationOnce(medicationId: Int): MedicationSchedule? {
        return withContext(Dispatchers.IO) {
            scheduleRepository.getSchedulesForMedication(medicationId).firstOrNull()?.firstOrNull()
        }
    }

    fun loadSchedulesForMedicationIds(medicationIds: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            val map = mutableMapOf<Int, MedicationSchedule?>()
            medicationIds.forEach { id ->
                // Assuming getSchedulesForMedication returns Flow<List<MedicationSchedule>>
                // and we want the first schedule if multiple exist (though typically it's one-to-one).
                map[id] = scheduleRepository.getSchedulesForMedication(id).firstOrNull()?.firstOrNull()
            }
            _schedulesMap.value = map
        }
    }
}
