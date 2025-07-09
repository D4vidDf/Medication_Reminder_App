package com.d4viddf.medicationreminder.appfunctions

import android.content.Context
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.service.AppFunction
import com.d4viddf.medicationreminder.data.MedicationScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NextDoseAppFunction(private val context: Context) : AppFunction {
    private val medicationScheduleRepository = MedicationScheduleRepository(context)

    override suspend fun execute(
        appFunctionContext: AppFunctionContext,
        request: ExecuteAppFunctionRequest
    ): ExecuteAppFunctionResponse {
        val medicationName = request.functionInputs.getString("medicationName")
            ?: return ExecuteAppFunctionResponse.Error.newBuilder()
                .setThrowable(IllegalArgumentException("medicationName parameter is missing"))
                .build()

        return try {
            // Assuming MedicationScheduleRepository has a method like getNextDose
            // This is a suspend function, so it should be called from a coroutine scope
            val nextDoseInfo = withContext(Dispatchers.IO) {
                medicationScheduleRepository.getNextDose(medicationName)
            }

            if (nextDoseInfo != null) {
                val responseData = AppFunctionData.Builder()
                    .putString("nextDoseTime", nextDoseInfo.nextDoseTime)
                    .putString("medicationName", nextDoseInfo.medicationName)
                    .putString("doseAmount", nextDoseInfo.doseAmount)
                    .build()
                ExecuteAppFunctionResponse.Success.newBuilder().setResponse(responseData).build()
            } else {
                ExecuteAppFunctionResponse.Error.newBuilder()
                    .setThrowable(
                        NoSuchElementException(
                            "No schedule found for medication: $medicationName"
                        )
                    )
                    .build()
            }
        } catch (e: Exception) {
            ExecuteAppFunctionResponse.Error.newBuilder().setThrowable(e).build()
        }
    }
}
