package com.d4viddf.medicationreminder.viewmodel

import androidx.lifecycle.ViewModel
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.repository.MedicationScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AllSchedulesViewModel @Inject constructor(
    private val medicationScheduleRepository: MedicationScheduleRepository
) : ViewModel() {

    fun getSchedules(medicationId: Int): Flow<List<MedicationSchedule>> {
        return medicationScheduleRepository.getSchedulesForMedication(medicationId)
    }
}
