package com.d4viddf.medicationreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.d4viddf.medicationreminder.data.UserPreferencesRepository
import com.d4viddf.medicationreminder.di.ReminderReceiverEntryPoint
import com.d4viddf.medicationreminder.notifications.NotificationHelper
import com.d4viddf.medicationreminder.services.PreReminderForegroundService
import com.d4viddf.medicationreminder.workers.ReminderSchedulingWorker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    private val job = SupervisorJob() // Create a SupervisorJob for the scope
    private val scope = CoroutineScope(Dispatchers.IO + job) // Create a CoroutineScope with IO dispatcher

    companion object {
        const val ACTION_SHOW_REMINDER = "com.d4viddf.medicationreminder.ACTION_SHOW_REMINDER"
        const val ACTION_TRIGGER_PRE_REMINDER_SERVICE = "com.d4viddf.medicationreminder.ACTION_TRIGGER_PRE_REMINDER_SERVICE"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "extra_medication_dosage"
        const val EXTRA_ACTUAL_REMINDER_TIME_MILLIS = "extra_actual_reminder_time_millis"
        const val EXTRA_IS_INTERVAL = "extra_is_interval"
        const val EXTRA_NEXT_DOSE_TIME_MILLIS = "extra_next_dose_time_millis"
        private const val TAG = "ReminderReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null.")
            return
        }

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderReceiverEntryPoint::class.java
        )
        val localReminderRepository = entryPoint.reminderRepository()
        val localNotificationScheduler = entryPoint.notificationScheduler()
        val localMedicationRepository = entryPoint.medicationRepository() // Added
        val localMedicationTypeRepository = entryPoint.medicationTypeRepository() // Added

        val action = intent.action
        Log.d(TAG, "Received action: $action with Intent extras: ${intent.extras}")

        when (action) {
            ACTION_SHOW_REMINDER -> {
                val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1)
                if (reminderId == -1) { Log.e(TAG, "Invalid reminderId in ACTION_SHOW_REMINDER."); return }

                // Detener el PreReminderForegroundService si estaba activo para este recordatorio
                val stopServiceIntent = Intent(context, PreReminderForegroundService::class.java).apply {
                    this.action = PreReminderForegroundService.ACTION_STOP_PRE_REMINDER
                    putExtra(PreReminderForegroundService.EXTRA_SERVICE_REMINDER_ID, reminderId)
                }
                context.startService(stopServiceIntent) // Intenta detenerlo

                val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
                val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
                val actualReminderTimeMillis = intent.getLongExtra(EXTRA_ACTUAL_REMINDER_TIME_MILLIS, System.currentTimeMillis())
                val isIntervalType = intent.getBooleanExtra(EXTRA_IS_INTERVAL, false)
                val nextDoseTimeMillisExtra = intent.getLongExtra(EXTRA_NEXT_DOSE_TIME_MILLIS, 0L)
                val nextDoseTimeForHelper = if (nextDoseTimeMillisExtra > 0) nextDoseTimeMillisExtra else null

                Log.i(TAG, "ACTION_SHOW_REMINDER for ID: $reminderId, Name: $medicationName. Interval: $isIntervalType, NextDoseMillis: $nextDoseTimeForHelper")

                // Fetch notification sound URI and show notification
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val notificationSoundUri = userPreferencesRepository.notificationSoundUriFlow.firstOrNull()
                        val reminder = localReminderRepository.getReminderById(reminderId) // Fetch reminder for medicationId

                        var medicationColorHex: String? = null
                        var medicationTypeName: String? = null

                        if (reminder != null) {
                            val medication = localMedicationRepository.getMedicationById(reminder.medicationId)
                            if (medication != null) {
                                medicationColorHex = medication.color
                                medication.typeId?.let { actualTypeId -> // Renamed 'typeId' to 'actualTypeId' for clarity
                                    val medicationType = localMedicationTypeRepository.getMedicationTypeById(actualTypeId)
                                    medicationTypeName = medicationType?.name
                                }
                                Log.d(TAG, "Fetched details for FullScreenNotification: Color=$medicationColorHex, TypeName=$medicationTypeName for MedicationId=${medication.id}")
                            } else {
                                Log.w(TAG, "Medication not found for ReminderId: $reminderId, MedicationId: ${reminder.medicationId}")
                            }
                        } else {
                            Log.w(TAG, "Reminder not found for ID: $reminderId when fetching details for FullScreenNotification.")
                        }

                        NotificationHelper.showReminderNotification(
                            context, reminderId, medicationName, medicationDosage,
                            isIntervalType, nextDoseTimeForHelper, actualReminderTimeMillis,
                            notificationSoundUri,
                            medicationColorHex, // Pass fetched color
                            medicationTypeName  // Pass fetched type name
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching data or showing notification for ReminderId: $reminderId", e)
                        // Fallback call with null for new parameters
                        NotificationHelper.showReminderNotification(
                            context, reminderId, medicationName, medicationDosage,
                            isIntervalType, nextDoseTimeForHelper, actualReminderTimeMillis,
                            null, null, null
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_TRIGGER_PRE_REMINDER_SERVICE -> {
                Log.d(TAG, "Received ACTION_TRIGGER_PRE_REMINDER_SERVICE")
                val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1)
                val actualScheduledTimeMillis = intent.getLongExtra(EXTRA_ACTUAL_REMINDER_TIME_MILLIS, -1L)
                val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
                // Val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) // Si el servicio lo necesita

                if (reminderId != -1 && actualScheduledTimeMillis != -1L) {
                    if (actualScheduledTimeMillis < System.currentTimeMillis()){
                        Log.w(TAG, "Pre-reminder trigger for ID $reminderId, but actual scheduled time $actualScheduledTimeMillis is in the past. Not starting service.")
                        return
                    }

                    val serviceIntent = Intent(context, PreReminderForegroundService::class.java).apply {
                        // No necesitas ACTION_START_PRE_REMINDER si onStartCommand lo maneja por defecto
                        putExtra(PreReminderForegroundService.EXTRA_SERVICE_REMINDER_ID, reminderId)
                        putExtra(PreReminderForegroundService.EXTRA_SERVICE_ACTUAL_SCHEDULED_TIME_MILLIS, actualScheduledTimeMillis)
                        putExtra(PreReminderForegroundService.EXTRA_SERVICE_MEDICATION_NAME, medicationName)
                        // ... pasar más datos si es necesario para la notificación del servicio ...
                    }
                    context.startForegroundService(serviceIntent)
                    Log.d(TAG, "Started PreReminderForegroundService for reminderId: $reminderId")
                } else {
                    Log.e(TAG, "Invalid data for starting PreReminderForegroundService. ReminderId: $reminderId, ScheduledTime: $actualScheduledTimeMillis")
                }
            }

            NotificationHelper.ACTION_MARK_AS_TAKEN -> {
                val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1)
                if (reminderId != -1) {
                    Log.i(TAG, "ACTION_MARK_AS_TAKEN for reminder ID $reminderId")
                    val pendingResult = goAsync()
                    scope.launch {
                        var medicationIdToReschedule: Int? = null
                        try {
                            val reminder = localReminderRepository.getReminderById(reminderId)
                            if (reminder == null) {
                                Log.e(TAG, "Reminder with ID $reminderId not found for ACTION_MARK_AS_TAKEN.")
                                // reminderId is non-null Int here, direct call is fine.
                                localNotificationScheduler.cancelAllAlarmsForReminder(context, reminderId)
                                return@launch
                            }
                            medicationIdToReschedule = reminder.medicationId

                            if (reminder.isTaken) {
                                Log.w(TAG, "Reminder ID $reminderId was already marked as taken.")
                                // reminderId is non-null Int here, direct call is fine.
                                localNotificationScheduler.cancelAllAlarmsForReminder(context, reminderId)
                            } else {
                                val nowString = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                localReminderRepository.markReminderAsTaken(reminderId, nowString)
                                Log.d(TAG, "Reminder ID $reminderId marked as taken in DB.")
                                // reminderId is non-null Int here, direct call is fine.
                                localNotificationScheduler.cancelAllAlarmsForReminder(context, reminderId)
                            }

                            medicationIdToReschedule?.let { medId -> // Added ?.let for safety, though medId should be set if reminder was found.
                                Log.d(TAG, "Scheduling next reminder for medication ID: $medId after taken action.")
                                val workManager = WorkManager.getInstance(context.applicationContext)
                                val data = Data.Builder()
                                    .putInt(ReminderSchedulingWorker.KEY_MEDICATION_ID, medId)
                                    .putBoolean(ReminderSchedulingWorker.KEY_IS_DAILY_REFRESH, false)
                                    .build()
                                val scheduleNextWorkRequest = OneTimeWorkRequestBuilder<ReminderSchedulingWorker>()
                                    .setInputData(data)
                                    .addTag("${ReminderSchedulingWorker.WORK_NAME_PREFIX}Next_${medId}")
                                    .build()
                                workManager.enqueueUniqueWork(
                                    "${ReminderSchedulingWorker.WORK_NAME_PREFIX}NextScheduled_${medId}",
                                    ExistingWorkPolicy.REPLACE, // REEMPLAZA para asegurar que solo uno se programe
                                    scheduleNextWorkRequest
                                )
                                Log.i(TAG, "Enqueued ReminderSchedulingWorker for med ID $medId to schedule next reminder.")
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "Error in ACTION_MARK_AS_TAKEN for reminder $reminderId", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    Log.e(TAG, "Invalid reminderId (-1) in ACTION_MARK_AS_TAKEN.")
                }
            }
        }
    }
}