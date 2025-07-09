package com.d4viddf.medicationreminder.appfunctions

import androidx.appfunctions.AppFunctionService // Correct import
import androidx.appfunctions.AppFunctionService.AppFunctionServiceConfiguration // Correct import
import androidx.appfunctions.service.AppFunctionConfiguration // Correct import

class MedicationAppFunctionService : AppFunctionService() { // Correct base class

    override fun onCreateConfiguration(): AppFunctionServiceConfiguration { // Correct return type and method
        return AppFunctionServiceConfiguration.Builder()
            .addAppFunction(
                AppFunctionConfiguration.Builder()
                    .setFunctionName("CheckNextDose") // Must match the name in capability metadata
                    .setAppFunction(NextDoseAppFunction(applicationContext)) // Provide instance of your AppFunction impl
                    .setCapability(getCheckNextDoseCapability()) // Reference to the capability metadata
                    .build()
            )
            // Example: Add more AppFunctionConfiguration objects here for other capabilities
            // .addAppFunction(
            //     AppFunctionConfiguration.Builder()
            //         .setFunctionName("AnotherCapability")
            //         .setAppFunction(AnotherAppFunction(applicationContext))
            //         .setCapability(getAnotherCapabilityMetadata())
            //         .build()
            // )
            .build()
    }
}
