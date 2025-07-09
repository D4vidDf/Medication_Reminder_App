package com.d4viddf.medicationreminder.appfunctions

import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionService.AppFunctionServiceConfiguration
import androidx.appfunctions.service.AppFunctionConfiguration

class MedicationAppFunctionService : AppFunctionService() {
    override fun onCreateConfiguration(): AppFunctionServiceConfiguration {
        return AppFunctionServiceConfiguration.Builder()
            .addAppFunction(
                AppFunctionConfiguration.Builder()
                    .setFunctionName("CheckNextDose") // Should match the name in capability metadata
                    .setAppFunction(NextDoseAppFunction(applicationContext))
                    .setCapability(getCheckNextDoseCapability()) // From CheckNextDose.kt
                    .build()
            )
            // You can add more AppFunctionConfiguration objects here for other capabilities
            .build()
    }
}
