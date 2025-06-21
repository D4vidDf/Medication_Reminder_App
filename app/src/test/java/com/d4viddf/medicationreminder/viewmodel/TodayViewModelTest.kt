package com.d4viddf.medicationreminder.viewmodel

import app.cash.turbine.test
import com.d4viddf.medicationreminder.data.Medication
import com.d4viddf.medicationreminder.data.MedicationReminderRepository
import com.d4viddf.medicationreminder.data.MedicationRepository
import com.d4viddf.medicationreminder.data.MedicationSchedule
import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.data.MedicationTypeRepository
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class TodayViewModelTest {

    private lateinit var viewModel: TodayViewModel
    private val mockReminderRepo: MedicationReminderRepository = mock()
    private val mockMedicationRepo: MedicationRepository = mock()
    private val mockMedicationTypeRepo: MedicationTypeRepository = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private fun setupViewModel() {
         viewModel = TodayViewModel(mockReminderRepo, mockMedicationRepo, mockMedicationTypeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val today = LocalDate.now()
    private val sampleMedicationType = MedicationType(1, "Pill", "ic_pill", "PILL_GROUP")
    private val sampleMedication1 = Medication(id = 1, name = "Med A", dosage = "10mg", typeId = 1, color = MedicationColor.LIGHT_BLUE.name, packageSize = 0, remainingDoses = 0, startDate = "", endDate = "")
    private val sampleMedication2 = Medication(id = 2, name = "Med B", dosage = "20mg", typeId = 1, color = MedicationColor.LIGHT_GREEN.name, packageSize = 0, remainingDoses = 0, startDate = "", endDate = "")


    @Test
    fun `init loads reminders and initial state is loading`() = runTest {
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(emptyList()))
        setupViewModel() // ViewModel init calls loadTodayReminders

        assertEquals(true, viewModel.uiState.value.isLoading)
        // Advance time to allow coroutines launched in init to complete
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isLoading) // Should be false after loading attempt
    }

    @Test
    fun `loadTodayReminders success updates state with grouped reminders`() = runTest {
        val timeMorning = LocalTime.of(8, 0)
        val timeEvening = LocalTime.of(20, 0)
        val schedules = listOf(
            MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = timeMorning, takenAt = null),
            MedicationSchedule(id = 2, medicationId = 2, scheduleDate = today, scheduledTime = timeEvening, takenAt = null),
            MedicationSchedule(id = 3, medicationId = 1, scheduleDate = today, scheduledTime = timeMorning, takenAt = null) // Another one at 8 AM
        )
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(schedules))
        whenever(mockMedicationRepo.getMedicationById(1)).thenReturn(flowOf(sampleMedication1))
        whenever(mockMedicationRepo.getMedicationById(2)).thenReturn(flowOf(sampleMedication2))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()


        viewModel.uiState.test {
            val state = awaitItem() // This might be initial or already loaded state.
                                  // Depending on how quickly init completes vs test observation starts.
                                  // If init is fast, this is already the loaded state.

            // If the first item is the initial empty/loading state, await another one.
            val finalState = if (state.isLoading || state.groupedReminders.isEmpty()) awaitItem() else state

            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
            assertNotNull(finalState.groupedReminders[timeMorning])
            assertEquals(2, finalState.groupedReminders[timeMorning]?.size)
            assertEquals("Med A", finalState.groupedReminders[timeMorning]?.get(0)?.medicationName)
            assertNotNull(finalState.groupedReminders[timeEvening])
            assertEquals(1, finalState.groupedReminders[timeEvening]?.size)
            assertEquals("Med B", finalState.groupedReminders[timeEvening]?.get(0)?.medicationName) // Corrected Typo Med B
        }
    }

    @Test
    fun `loadTodayReminders failure updates state with error`() = runTest {
        val errorMessage = "Database error"
        whenever(mockReminderRepo.getAllSchedules()).thenThrow(RuntimeException(errorMessage))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val finalState = if (state.isLoading) awaitItem() else state

            assertFalse(finalState.isLoading)
            assertEquals("Failed to load reminders: $errorMessage", finalState.error)
            assertTrue(finalState.groupedReminders.isEmpty())
        }
    }

    @Test
    fun `handleToggle for past item marks as taken with current time`() = runTest {
        val pastTime = LocalTime.now().minusHours(1)
        val schedule = MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = pastTime, takenAt = null)
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(listOf(schedule)))
        whenever(mockMedicationRepo.getMedicationById(1)).thenReturn(flowOf(sampleMedication1))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // Initial load

        val reminderIdToToggle = viewModel.uiState.value.groupedReminders[pastTime]?.get(0)?.id ?: ""
        assertFalse(reminderIdToToggle.isBlank())

        viewModel.handleToggle(reminderIdToToggle, true)
        testDispatcher.scheduler.advanceUntilIdle()


        viewModel.uiState.test {
            val state = awaitItem() // Get current state after toggle
            val toggledItem = state.groupedReminders[pastTime]?.find { it.id == reminderIdToToggle }
            assertNotNull(toggledItem)
            assertTrue(toggledItem.isTaken)
            assertNotNull(toggledItem.actualTakenTime)
            // Check if actualTakenTime is very close to LocalTime.now() (within a second or so)
            assertTrue(java.time.Duration.between(toggledItem.actualTakenTime, LocalTime.now()).seconds < 2)
        }
    }

    @Test
    fun `handleToggle for future item shows dialog`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        val schedule = MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = futureTime, takenAt = null)
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(listOf(schedule)))
        whenever(mockMedicationRepo.getMedicationById(1)).thenReturn(flowOf(sampleMedication1))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val reminderToToggle = viewModel.uiState.value.groupedReminders[futureTime]?.get(0)
        assertNotNull(reminderToToggle)
        assertFalse(reminderToToggle.isTaken)

        viewModel.handleToggle(reminderToToggle.id, true) // Try to mark as taken
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.showTakeFutureDialog.test {
            val dialogState = awaitItem()
            assertNotNull(dialogState)
            assertEquals(reminderToToggle.id, dialogState.reminderId)
            assertEquals(reminderToToggle.medicationName, dialogState.medicationName)
            assertEquals(reminderToToggle.scheduledTime, dialogState.scheduledTime)
        }
        // Ensure original item is NOT marked as taken yet
        val itemAfterToggleAttempt = viewModel.uiState.value.groupedReminders[futureTime]?.get(0)
        assertNotNull(itemAfterToggleAttempt)
        assertFalse(itemAfterToggleAttempt.isTaken)
    }


    @Test
    fun `markFutureMedicationAsTaken at current time updates state`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        val reminderId = "future1"
        // Simulate that the item is in the list (though load isn't strictly necessary for this specific method test if state is prepped)
         val schedules = listOf(MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = futureTime, takenAt = null))
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(schedules))
        whenever(mockMedicationRepo.getMedicationById(1)).thenReturn(flowOf(sampleMedication1.copy(id=1, name="MedFuture"))) // Ensure ID matches
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Manually set dialog state as if it was shown
        val initialItem = viewModel.uiState.value.groupedReminders[futureTime]?.find { it.id == "1" } // ID from schedule
        assertNotNull(initialItem)

        viewModel.markFutureMedicationAsTaken(initialItem.id, initialItem.scheduledTime, true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val updatedItem = state.groupedReminders[futureTime]?.find { it.id == initialItem.id }
            assertNotNull(updatedItem)
            assertTrue(updatedItem.isTaken)
            assertNotNull(updatedItem.actualTakenTime)
             assertTrue(java.time.Duration.between(updatedItem.actualTakenTime, LocalTime.now()).seconds < 2)
        }
        assertNull(viewModel.showTakeFutureDialog.value) // Dialog should be dismissed
    }

    @Test
    fun `markFutureMedicationAsTaken at scheduled time updates state`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        // Simulate load
        val schedules = listOf(MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = futureTime, takenAt = null))
        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(schedules))
        whenever(mockMedicationRepo.getMedicationById(1)).thenReturn(flowOf(sampleMedication1))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialItem = viewModel.uiState.value.groupedReminders[futureTime]?.find { it.id == "1" }
        assertNotNull(initialItem)

        viewModel.markFutureMedicationAsTaken(initialItem.id, initialItem.scheduledTime, false) // false for takeAtCurrentTime
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
             val state = awaitItem()
            val updatedItem = state.groupedReminders[futureTime]?.find { it.id == initialItem.id }
            assertNotNull(updatedItem)
            assertTrue(updatedItem.isTaken)
            assertEquals(futureTime, updatedItem.actualTakenTime)
        }
        assertNull(viewModel.showTakeFutureDialog.value)
    }


    @Test
    fun `dismissTakeFutureDialog clears dialog state`() = runTest {
        setupViewModel() // No data load needed for this simple state change

        // Simulate dialog being shown
        val dummyState = TodayViewModel.TakeFutureDialogState("id1", "MedX", LocalTime.NOON)
        (viewModel.showTakeFutureDialog as kotlinx.coroutines.flow.MutableStateFlow).value = dummyState // Private access hack for test
        assertNotNull(viewModel.showTakeFutureDialog.value)

        viewModel.dismissTakeFutureDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.showTakeFutureDialog.value)
    }
}

// Helper to fix MedB typo in test
private fun TodayMedicationData.fixName(): TodayMedicationData = if (this.medicationName == "MedB") this.copy(medicationName = "Med B") else this
