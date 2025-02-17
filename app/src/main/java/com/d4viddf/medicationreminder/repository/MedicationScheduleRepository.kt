package com.d4viddf.medicationreminder.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationScheduleRepository @Inject constructor(
    private val medicationScheduleDao: MedicationScheduleDao,
    private val firebaseSyncDao: FirebaseSyncDao
) {

    fun getSchedulesForMedication(medicationId: Int): Flow<List<MedicationSchedule>> =
        medicationScheduleDao.getSchedulesForMedication(medicationId)

    suspend fun insertSchedule(schedule: MedicationSchedule) {
        medicationScheduleDao.insertSchedule(schedule)
        firebaseSyncDao.insertSyncRecord(
            FirebaseSync(entityName = "MedicationSchedule", entityId = schedule.id, syncStatus = SyncStatus.PENDING)
        )
    }

    suspend fun updateSchedule(schedule: MedicationSchedule) {
        medicationScheduleDao.updateSchedule(schedule)
        firebaseSyncDao.insertSyncRecord(
            FirebaseSync(entityName = "MedicationSchedule", entityId = schedule.id, syncStatus = SyncStatus.PENDING)
        )
    }

    suspend fun deleteSchedule(schedule: MedicationSchedule) {
        medicationScheduleDao.deleteSchedule(schedule)
        firebaseSyncDao.insertSyncRecord(
            FirebaseSync(entityName = "MedicationSchedule", entityId = schedule.id, syncStatus = SyncStatus.PENDING)
        )
    }
}
