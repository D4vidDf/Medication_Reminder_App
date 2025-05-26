package com.d4viddf.medicationreminder.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data // Keep one
import androidx.work.ExistingWorkPolicy // Keep one
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.d4viddf.medicationreminder.data.Medication
import com.d4viddf.medicationreminder.data.MedicationReminder
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
import com.d4viddf.medicationreminder.data.MedicationRepository // Added
import com.d4viddf.medicationreminder.workers.ReminderSchedulingWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow // Keep one
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MedicationReminderViewModel @Inject constructor(
    private val reminderRepository: MedicationReminderRepository,
    private val medicationRepository: MedicationRepository, // Added
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _allRemindersForSelectedMedication = MutableStateFlow<List<MedicationReminder>>(emptyList())
    val allRemindersForSelectedMedication: StateFlow<List<MedicationReminder>> = _allRemindersForSelectedMedication

    private val _todaysRemindersForSelectedMedication = MutableStateFlow<List<MedicationReminder>>(emptyList())
    val todaysRemindersForSelectedMedication: StateFlow<List<MedicationReminder>> = _todaysRemindersForSelectedMedication


    private val storableDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME


    fun loadRemindersForMedication(medicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) { // Las operaciones de BD en IO
            reminderRepository.getRemindersForMedication(medicationId).collect { allRemindersList ->
                val sortedAllReminders = allRemindersList.sortedBy { it.reminderTime }
                _allRemindersForSelectedMedication.value = sortedAllReminders

                val today = LocalDate.now()
                _todaysRemindersForSelectedMedication.value = sortedAllReminders.filter {
                    try {
                        LocalDateTime.parse(it.reminderTime, storableDateTimeFormatter).toLocalDate().isEqual(today)
                    } catch (e: Exception) {
                        Log.e("MedReminderVM", "Error parsing reminderTime: ${it.reminderTime}", e)
                        false
                    }
                }
                Log.d("MedReminderVM", "Loaded ${allRemindersList.size} total reminders, ${todaysRemindersForSelectedMedication.value.size} for today (medId: $medicationId).")
            }
        }
    }

    suspend fun getAllRemindersForMedicationOnce(medicationId: Int): List<MedicationReminder> {
        return withContext(Dispatchers.IO) {
            reminderRepository.getRemindersForMedication(medicationId).firstOrNull()?.sortedBy { it.reminderTime } ?: emptyList()
        }
    }

    fun markReminderAsTaken(reminderId: Int, takenAt: String) {

        viewModelScope.launch {

            reminderRepository.markReminderAsTaken(reminderId, takenAt)

        }

    }

    fun markReminderAsTakenAndUpdateLists(reminderId: Int, medicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val nowString = LocalDateTime.now().format(storableDateTimeFormatter)
            Log.d("MedReminderVM", "Marking reminderId $reminderId (medId $medicationId) as taken at $nowString")

            // Fetch the medication
            val medication = medicationRepository.getMedicationById(medicationId)
            if (medication != null) {
                val quantityToDeduct = medication.userDosageQuantity?.toDoubleOrNull() ?: 1.0
                val currentRemainingDoses = medication.remainingDoses ?: 0.0 // Default to 0.0 if null
                val newRemainingDoses = (currentRemainingDoses - quantityToDeduct).coerceAtLeast(0.0)

                Log.d("MedReminderVM", "Medication: ${medication.name}, Original Remaining Doses: ${medication.remainingDoses}, Quantity Deducted: $quantityToDeduct, New Calculated Remaining Doses: $newRemainingDoses")

                val updatedMedication = medication.copy(remainingDoses = newRemainingDoses)
                medicationRepository.updateMedication(updatedMedication)
                Log.d("MedReminderVM", "Updated medication ${medication.name} with new remaining doses: $newRemainingDoses")

            } else {
                Log.e("MedReminderVM", "Medication not found with id: $medicationId. Cannot update remaining doses.")
            }

            // Mark reminder as taken after updating medication stock
            reminderRepository.markReminderAsTaken(reminderId, nowString)
            triggerNextReminderScheduling(medicationId)
        }
    }

    internal fun triggerNextReminderScheduling(medicationId: Int) { // Made internal as per original for consistency
        Log.d("MedReminderVM", "Triggering next reminder scheduling for med ID: $medicationId using injected appContext")
        val workManager = WorkManager.getInstance(this.appContext) // Usa this.appContext
        val data = Data.Builder()
            .putInt(ReminderSchedulingWorker.KEY_MEDICATION_ID, medicationId)
            .putBoolean(ReminderSchedulingWorker.KEY_IS_DAILY_REFRESH, false)
            .build()
        val scheduleNextWorkRequest =
            OneTimeWorkRequestBuilder<ReminderSchedulingWorker>()
                .setInputData(data)
                .addTag("${ReminderSchedulingWorker.WORK_NAME_PREFIX}NextFromDetail_${medicationId}")
                .build()
        workManager.enqueueUniqueWork(
            "${ReminderSchedulingWorker.WORK_NAME_PREFIX}NextScheduledFromDetail_${medicationId}",
            ExistingWorkPolicy.REPLACE,
            scheduleNextWorkRequest
        )
        Log.i("MedReminderVM", "Enqueued ReminderSchedulingWorker for med ID $medicationId.")
    }
}