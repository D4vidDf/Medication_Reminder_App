package com.d4viddf.medicationreminder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.data.Medication
import com.d4viddf.medicationreminder.data.MedicationReminder
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
import com.d4viddf.medicationreminder.data.MedicationRepository
import com.d4viddf.medicationreminder.data.MedicationScheduleRepository
import com.d4viddf.medicationreminder.logic.ReminderCalculator
import com.d4viddf.medicationreminder.ui.components.ProgressDetails
import dagger.hilt.android.lifecycle.HiltViewModel
// Removed one java.time.LocalDate import, keeping the one below
import com.d4viddf.medicationreminder.data.ScheduleType // Added
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate // Keep this one
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderRepository: MedicationReminderRepository, // Keep if used elsewhere, or remove if not.
    private val scheduleRepository: MedicationScheduleRepository
) : ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _medicationProgressDetails = MutableStateFlow<ProgressDetails?>(null)
    val medicationProgressDetails: StateFlow<ProgressDetails?> = _medicationProgressDetails.asStateFlow()

    init {
        observeMedications() // Changed from getAllMedications
    }

    // Renamed from getAllMedications
    private fun observeMedications() {
        viewModelScope.launch {
            // This is a long-lived collection for observing data changes
            // It should not modify _isLoading.value
            medicationRepository.getAllMedications().collect { medications ->
                _medications.value = medications
            }
        }
    }

    fun refreshMedications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Perform the actual data refresh logic here.
                // This might involve a specific repository call to fetch fresh data,
                // or if your `getAllMedications()` flow re-fetches on new collection,
                // you could potentially take the first emission.
                // For simplicity, if your repository automatically updates subscribers
                // when data changes, this refresh might just be about ensuring
                // any cached/stale data source is invalidated or re-queried.
                // If repository.getAllMedications() is a cold flow that fetches on collection:
                medicationRepository.getAllMedications().firstOrNull() // Ensure it re-fetches, result updates _medications via observeMedications
                // Add a small delay if needed to simulate work if the operation is too fast, for testing UI
                // kotlinx.coroutines.delay(1000)
            } catch (e: Exception) {
                // Handle error, log it (e.g., Log.e("MedicationViewModel", "Error refreshing medications", e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observeMedicationAndRemindersForDailyProgress(medicationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderRepository.getRemindersForMedication(medicationId).collect { remindersList ->
                val currentMedication = medicationRepository.getMedicationById(medicationId)
                // Ensure we get the list of schedules, then the first, if any.
                val currentSchedule = scheduleRepository.getSchedulesForMedication(medicationId).firstOrNull()?.firstOrNull()
                calculateAndSetDailyProgressDetails(currentMedication, currentSchedule, remindersList)
            }
        }
    }

    // This function seems to be for individual medication detail view, not directly for refill alerts across all meds.
    // Refill logic will be a separate function.
    suspend fun getMedicationById(medicationId: Int): Medication? { // Keep for other uses
        return withContext(Dispatchers.IO) {
            medicationRepository.getMedicationById(medicationId)
        }
    }

    private suspend fun calculateAndSetDailyProgressDetails(
        medication: Medication?,
        schedule: com.d4viddf.medicationreminder.data.MedicationSchedule?, // Explicitly using the type
        allRemindersForMedication: List<MedicationReminder>
    ) {
        if (medication == null || schedule == null) {
            _medicationProgressDetails.value = ProgressDetails(0, 0, 0, 0f, "N/A")
            return
        }

        val today = LocalDate.now()
        val remindersMapForToday = ReminderCalculator.generateRemindersForPeriod(
            medication = medication,
            schedule = schedule,
            periodStartDate = today,
            periodEndDate = today
        )
        val scheduledTimesTodayList = remindersMapForToday[today] ?: emptyList()
        val totalDosesScheduledToday = scheduledTimesTodayList.size
        Log.d("ProgressCalc", "Med: ${medication.name}, Schedule: ${schedule.scheduleType}, Scheduled for $today: $totalDosesScheduledToday doses. Times: $scheduledTimesTodayList")

        val dosesTakenToday = allRemindersForMedication.count { reminder ->
            try {
                val reminderDateTime = LocalDateTime.parse(reminder.reminderTime, ReminderCalculator.storableDateTimeFormatter)
                reminderDateTime.toLocalDate().isEqual(today) && reminder.isTaken
            } catch (e: DateTimeParseException) {
                Log.e("ProgressCalc", "Error parsing reminderTime: ${reminder.reminderTime}", e)
                false
            }
        }
        Log.d("ProgressCalc", "Doses taken today for ${medication.name}: $dosesTakenToday")

        val progressFraction = if (totalDosesScheduledToday > 0) {
            dosesTakenToday.toFloat() / totalDosesScheduledToday.toFloat()
        } else {
            0f
        }
        val displayText = "$dosesTakenToday / $totalDosesScheduledToday"
        _medicationProgressDetails.value = ProgressDetails(
            taken = dosesTakenToday,
            remaining = (totalDosesScheduledToday - dosesTakenToday).coerceAtLeast(0),
            totalFromPackage = totalDosesScheduledToday,
            progressFraction = progressFraction.coerceIn(0f, 1f),
            displayText = displayText
        )
    }

    suspend fun insertMedication(medication: Medication): Int {
        return withContext(Dispatchers.IO) {
            val id = medicationRepository.insertMedication(medication)
            // Potentially trigger refill check here if a new medication could affect alerts
            checkRefillAlerts()
            id.toInt() // Assuming insertMedication returns Long
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            medicationRepository.updateMedication(medication)
            // Trigger refill check after update as well
            checkRefillAlerts()
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch(Dispatchers.IO) {
            medicationRepository.deleteMedication(medication)
            // And after deletion
            checkRefillAlerts()
        }
    }

    // New function for refill alerts
    fun checkRefillAlerts(refillThresholdDays: Int = 7) {
        viewModelScope.launch(Dispatchers.IO) {
            val allMedications = medicationRepository.getAllMedications().firstOrNull() ?: emptyList()
            val today = LocalDate.now()

            allMedications.forEach { medication ->
                // Skip medications that have an end date in the past
                medication.endDate?.let { endDateStr ->
                    try {
                        val parsedEndDate = LocalDate.parse(endDateStr, ReminderCalculator.dateStorableFormatter)
                        if (parsedEndDate.isBefore(today)) {
                            Log.d("RefillCheck", "Skipping ${medication.name} as its end date ($parsedEndDate) is in the past.")
                            return@forEach // Skips to the next medication in the loop
                        }
                    } catch (e: DateTimeParseException) {
                        Log.e("RefillCheck", "Error parsing end date for ${medication.name}: $endDateStr", e)
                        // Optionally, decide whether to proceed or skip if end date is unparsable
                    }
                }


                val schedules = scheduleRepository.getSchedulesForMedication(medication.id).firstOrNull()
                val schedule = schedules?.firstOrNull() // Assuming one schedule per medication for now

                if (schedule == null) {
                    Log.w("RefillCheck", "No schedule found for medication: ${medication.name} (ID: ${medication.id}). Cannot calculate daily consumption.")
                    return@forEach // Skips to the next medication
                }

                val remindersForTodayMap = ReminderCalculator.generateRemindersForPeriod(
                    medication = medication,
                    schedule = schedule,
                    periodStartDate = today,
                    periodEndDate = today
                )
                val dosesScheduledToday = remindersForTodayMap[today]?.size ?: 0

                if (dosesScheduledToday == 0) {
                    // Log.d("RefillCheck", "No doses scheduled today for ${medication.name}. Skipping refill check for it.")
                    // If no doses today, it might not be actively taken, or it's an AS_NEEDED type.
                    // Depending on desired behavior, one might still want to check stock if it's not AS_NEEDED.
                    // For now, if no doses scheduled today, we assume daily consumption for refill calc is 0.
                    // This means daysLeft will be infinite unless remainingDoses is 0.
                    // A more robust approach might be to average doses over a week if schedule is irregular.
                    // However, for fixed daily/interval schedules, this is fine.
                    if ((medication.remainingDoses ?: 0.0) > 0) { // Only log if it has stock but no doses today
                        Log.d("RefillCheck", "Medication ${medication.name} has remaining stock (${medication.remainingDoses ?: 0.0}) but 0 doses scheduled for today. Days left effectively infinite for today's rate.")
                    }
                }

                val userDosageQty = medication.userDosageQuantity?.toDoubleOrNull() ?: 1.0
                val dailyConsumption = dosesScheduledToday * userDosageQty
                val currentRemainingDoses = medication.remainingDoses ?: 0.0 // Default to 0.0 if null

                if (dailyConsumption > 0) {
                    val daysLeft = currentRemainingDoses / dailyConsumption
                    Log.i("RefillCheck", "Medication: ${medication.name}, Doses Today: $dosesScheduledToday, User Qty: $userDosageQty, Daily Consumption: $dailyConsumption units, Remaining Units: $currentRemainingDoses, Days Left: $daysLeft")
                    if (daysLeft < refillThresholdDays) {
                        // Trigger refill alert (e.g., log, update a LiveData, send a notification)
                        Log.w("RefillAlert", "REFILL ALERT for ${medication.name}: Only $daysLeft days of medication left (threshold: $refillThresholdDays days). Remaining units: $currentRemainingDoses.")
                        // For a real app, this would update some UI state or schedule a notification.
                    }
                } else if (currentRemainingDoses > 0 && dosesScheduledToday == 0 && schedule.scheduleType != ScheduleType.AS_NEEDED) {
                    // This case handles medications that have stock, but no doses scheduled for *today*.
                    // If it's not "As Needed", it might be a concern or an off-day in a cycle.
                    // For simplicity, we are primarily alerting based on *today's* consumption rate.
                    // A more advanced system might average consumption over a typical week.
                    Log.d("RefillCheck", "Medication ${medication.name} has stock ($currentRemainingDoses) but no doses scheduled today, and is not 'As Needed'. Daily consumption for alert is 0.")
                }
            }
        }
    }
}