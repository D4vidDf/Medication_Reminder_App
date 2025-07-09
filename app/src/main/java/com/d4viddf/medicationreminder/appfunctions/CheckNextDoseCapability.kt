package com.d4viddf.medicationreminder.appfunctions

import android.content.Context
import androidx.appactions.interaction.capabilities.core.BaseCapability
import androidx.appactions.interaction.capabilities.core.Capability
import androidx.appactions.interaction.capabilities.core.CapabilityFactory
import androidx.appactions.interaction.capabilities.core.impl.Builder
import androidx.appactions.interaction.capabilities.core.impl.converters.TypeConverters
import androidx.appactions.interaction.capabilities.core.impl.spec.ActionSpec
import androidx.appactions.interaction.capabilities.core.properties.Property
import androidx.appactions.interaction.service.AppInteractionService
import com.d4viddf.medicationreminder.di.AppFunctionsEntryPoint
import com.google.common.util.concurrent.Futures
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Optional
import com.google.common.util.concurrent.SettableFuture
import androidx.appactions.interaction.capabilities.core.impl.concurrent.FutureCallback
import androidx.appactions.interaction.capabilities.core.impl.concurrent.FuturesUtil
import androidx.appactions.interaction.capabilities.core.FulfillmentResult
import androidx.appactions.interaction.capabilities.core.ExecutionResult

@CapabilityFactory(name = "actions.intent.GET_THING")
class CheckNextDoseFactory : CapabilityFactory<CheckNextDose> {
    override fun create(capability: Capability): CheckNextDose {
        return CheckNextDose(capability)
    }
}

class CheckNextDose(capability: Capability) : BaseCapability(capability) {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Register a fulfillment listener for the 'GET_THING' intent.
        // This is where your app's logic runs when the capability is invoked.
        registerFulfillmentListener { arguments ->
            val future = SettableFuture.create<FulfillmentResult>()
            scope.launch {
                // Safely get the medication name from the arguments.
                val medicationName = arguments.getParamValue("thing.name") as String?
                if (medicationName.isNullOrBlank()) {
                    future.set(FulfillmentResult.Builder().build())
                    return@launch
                }

                // Use the Hilt EntryPoint to get the repository.
                val entryPoint = EntryPointAccessors.fromApplication(
                    AppInteractionService.getApplicationContext(),
                    AppFunctionsEntryPoint::class.java
                )
                val scheduleRepository = entryPoint.medicationScheduleRepository()

                // Fetch the next dose from your repository.
                // This is a placeholder for your actual repository method.
                val nextReminder = scheduleRepository.getNextReminderForMedication(medicationName)

                val responseText = if (nextReminder != null) {
                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
                    val time = nextReminder.reminderTime.toLocalTime().format(timeFormatter)
                    "Your next dose of $medicationName is at $time."
                } else {
                    "You don't have any upcoming doses scheduled for $medicationName."
                }

                // Build the result and send it back to the agent.
                val result = FulfillmentResult.Builder()
                    .setExecutionResult(
                        ExecutionResult.Builder<Output>().setOutput(Output(responseText)).build()
                    )
                    .build()
                future.set(result)
            }
            future
        }
    }

    // Defines the output the function will return to the agent.
    class Output(val text: String)
}
