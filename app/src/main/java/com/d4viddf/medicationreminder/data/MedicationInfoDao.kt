package com.d4viddf.medicationreminder.data

import androidx.room.*

@Dao
interface MedicationInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationInfo(medicationInfo: MedicationInfo)

    @Query("SELECT * FROM medication_info WHERE medicationId = :medicationId")
    suspend fun getMedicationInfoById(medicationId: Int): MedicationInfo?
}
