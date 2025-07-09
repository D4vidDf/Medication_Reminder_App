package com.d4viddf.medicationreminder.appfunctions

import androidx.appfunctions.metadata.AppFunctionBuilder
import androidx.appfunctions.metadata.AppFunctionMetadata
import androidx.appfunctions.metadata.AppFunctionParameterBuilder
import androidx.appfunctions.metadata.AppFunctionResponseBuilder
import androidx.appfunctions.metadata.AppFunctionSchemaMetadata

// Define the capability to check the next dose of a medication.
// This metadata object describes the function to the AI Agent.
fun getCheckNextDoseCapability(): AppFunctionMetadata {
    return AppFunctionBuilder()
        .setFunctionName("CheckNextDose")
        .setDescription("Checks the next scheduled dose for a given medication.")
        .addParameter(
            AppFunctionParameterBuilder()
                .setName("medicationName")
                .setDescription("The name of the medication to check.")
                .setDataType(AppFunctionSchemaMetadata.DataType.STRING)
                .setIsRequired(true)
                .build()
        )
        .setResponse(
            AppFunctionResponseBuilder()
                .addResponsePart(
                    AppFunctionParameterBuilder()
                        .setName("nextDoseTime")
                        .setDescription("The time of the next scheduled dose.")
                        .setDataType(AppFunctionSchemaMetadata.DataType.STRING) // Consider using a more specific time/date type if available
                        .build()
                )
                .addResponsePart(
                    AppFunctionParameterBuilder()
                        .setName("medicationName")
                        .setDescription("The name of the medication for which the next dose was checked.")
                        .setDataType(AppFunctionSchemaMetadata.DataType.STRING)
                        .build()
                )
                 .addResponsePart(
                    AppFunctionParameterBuilder()
                        .setName("doseAmount")
                        .setDescription("The amount of the medication for the next dose.")
                        .setDataType(AppFunctionSchemaMetadata.DataType.STRING) // Or NUMBER if appropriate
                        .build()
                )
                .build()
        )
        .build()
}
