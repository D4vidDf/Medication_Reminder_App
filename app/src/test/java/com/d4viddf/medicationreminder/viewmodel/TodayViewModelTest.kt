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
            // Skip initial loading state if it's emitted quickly
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(2, state.timeGroups.size) // Two time groups: 8 AM and 8 PM

            val morningGroup = state.timeGroups.find { it.scheduledTime == timeMorning }
            assertNotNull(morningGroup)
            assertEquals(2, morningGroup.reminders.size)
            assertEquals("Med A", morningGroup.reminders[0].medicationName)
            assertEquals(0, morningGroup.takenCount) // Assuming none taken initially in this test setup
            assertEquals(2, morningGroup.totalInGroup)

            val eveningGroup = state.timeGroups.find { it.scheduledTime == timeEvening }
            assertNotNull(eveningGroup)
            assertEquals(1, eveningGroup.reminders.size)
            assertEquals("Med B", eveningGroup.reminders[0].medicationName)
            assertEquals(0, eveningGroup.takenCount) // Assuming none taken
            assertEquals(1, eveningGroup.totalInGroup)
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
            assertTrue(finalState.timeGroups.isEmpty()) // Check timeGroups
        }
    }

    @Test
    fun `handleToggle for past item marks as taken with current time`() = runTest {
        val pastTime = LocalTime.now().minusHours(1)
        val reminderIdToToggle = "r1"
        val schedules = listOf(
            MedicationSchedule(id = 1, medicationId = 1, scheduleDate = today, scheduledTime = pastTime, takenAt = null)
        )
        // Mock so that the TodayMedicationData has id = "r1"
        // This part of the test needs to be more careful about how schedule.id maps to TodayMedicationData.id
        // In the VM, TodayMedicationData id is schedule.id.toString(). So if schedule.id is 1, data.id is "1".
        // For this test, let's assume the TodayMedicationData we want to toggle will have id "1".
        // So, if schedule.id = 1, then reminderIdToToggle should be "1".

        whenever(mockReminderRepo.getAllSchedules()).thenReturn(flowOf(schedules)) // This is the old mock
        // New mocking strategy based on current ViewModel logic:
        val medication = sampleMedication1.copy(id = 1)
        val reminder = com.d4viddf.medicationreminder.data.MedicationReminder(
            id = 1, medicationId = 1, reminderTime = LocalDateTime.of(today, pastTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), isTaken = false, takenAt = null, medicationScheduleId = 1
        )
        whenever(mockMedicationRepo.getAllMedications()).thenReturn(flowOf(listOf(medication)))
        whenever(mockReminderRepo.getRemindersForMedication(1)).thenReturn(flowOf(listOf(reminder)))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))


        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle() // Initial load

        // Find the item in the new structure
        val targetGroup = viewModel.uiState.value.timeGroups.find { it.scheduledTime == pastTime }
        assertNotNull(targetGroup)
        val itemToToggle = targetGroup.reminders.find { it.id == "1" } // id from MedicationReminder
        assertNotNull(itemToToggle)
        assertEquals("1", itemToToggle.id)


        viewModel.handleToggle(itemToToggle.id, true)
        testDispatcher.scheduler.advanceUntilIdle()


        viewModel.uiState.test {
            var state = awaitItem()
            if(state.timeGroups.find { it.scheduledTime == pastTime }?.reminders?.find { it.id == "1"}?.isTaken == false) {
                state = awaitItem() // ensure state update is captured
            }

            val updatedGroup = state.timeGroups.find { it.scheduledTime == pastTime }
            assertNotNull(updatedGroup)
            val toggledItem = updatedGroup.reminders.find { it.id == "1" }
            assertNotNull(toggledItem)
            assertTrue(toggledItem.isTaken, "Item was not marked as taken")
            assertNotNull(toggledItem.actualTakenTime, "Actual taken time was not set")
            assertEquals(1, updatedGroup.takenCount) // Verify takenCount updated
            // Check if actualTakenTime is very close to LocalTime.now() (within a second or so)
            assertTrue(java.time.Duration.between(toggledItem.actualTakenTime, LocalTime.now()).seconds < 2)
        }
    }

    @Test
    fun `handleToggle for future item shows dialog`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        val medication = sampleMedication1.copy(id = 1)
        val reminder = com.d4viddf.medicationreminder.data.MedicationReminder(
            id = 1, medicationId = 1, reminderTime = LocalDateTime.of(today, futureTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), isTaken = false, takenAt = null, medicationScheduleId = 1
        )
        whenever(mockMedicationRepo.getAllMedications()).thenReturn(flowOf(listOf(medication)))
        whenever(mockReminderRepo.getRemindersForMedication(1)).thenReturn(flowOf(listOf(reminder)))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val targetGroup = viewModel.uiState.value.timeGroups.find { it.scheduledTime == futureTime }
        assertNotNull(targetGroup)
        val itemToToggle = targetGroup.reminders.find { it.id == "1" }
        assertNotNull(itemToToggle)
        assertFalse(itemToToggle.isTaken)

        viewModel.handleToggle(itemToToggle.id, true) // Try to mark as taken
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.showTakeFutureDialog.test {
            val dialogState = awaitItem()
            assertNotNull(dialogState)
            assertEquals(itemToToggle.id, dialogState.reminderId)
            assertEquals(itemToToggle.medicationName, dialogState.medicationName)
            assertEquals(itemToToggle.scheduledTime, dialogState.scheduledTime)
        }
        // Ensure original item is NOT marked as taken yet
        val groupAfterToggle = viewModel.uiState.value.timeGroups.find { it.scheduledTime == futureTime }
        assertNotNull(groupAfterToggle)
        val itemAfterToggleAttempt = groupAfterToggle.reminders.find { it.id == "1" }
        assertNotNull(itemAfterToggleAttempt)
        assertFalse(itemAfterToggleAttempt.isTaken)
    }


    @Test
    fun `markFutureMedicationAsTaken at current time updates state`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        // Simulate load to have the item in state
        val medication = sampleMedication1.copy(id = 1, name = "MedFuture")
        val reminder = com.d4viddf.medicationreminder.data.MedicationReminder(
            id = 1, medicationId = 1, reminderTime = LocalDateTime.of(today, futureTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), isTaken = false, takenAt = null, medicationScheduleId = 1
        )
        whenever(mockMedicationRepo.getAllMedications()).thenReturn(flowOf(listOf(medication)))
        whenever(mockReminderRepo.getRemindersForMedication(1)).thenReturn(flowOf(listOf(reminder)))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialGroup = viewModel.uiState.value.timeGroups.find { it.scheduledTime == futureTime }
        assertNotNull(initialGroup)
        val initialItem = initialGroup.reminders.find { it.id == "1" }
        assertNotNull(initialItem)

        viewModel.markFutureMedicationAsTaken(initialItem.id, initialItem.scheduledTime, true)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
             if(state.timeGroups.find { it.scheduledTime == futureTime }?.reminders?.find { it.id == "1"}?.isTaken == false) {
                state = awaitItem()
            }
            val updatedGroup = state.timeGroups.find { it.scheduledTime == futureTime }
            assertNotNull(updatedGroup)
            val updatedItem = updatedGroup.reminders.find { it.id == "1" }
            assertNotNull(updatedItem)
            assertTrue(updatedItem.isTaken)
            assertNotNull(updatedItem.actualTakenTime)
            assertEquals(1, updatedGroup.takenCount) // Verify takenCount
             assertTrue(java.time.Duration.between(updatedItem.actualTakenTime, LocalTime.now()).seconds < 2)
        }
        assertNull(viewModel.showTakeFutureDialog.value) // Dialog should be dismissed
    }

    @Test
    fun `markFutureMedicationAsTaken at scheduled time updates state`() = runTest {
        val futureTime = LocalTime.now().plusHours(2)
        // Simulate load
        val medication = sampleMedication1.copy(id = 1)
        val reminder = com.d4viddf.medicationreminder.data.MedicationReminder(
            id = 1, medicationId = 1, reminderTime = LocalDateTime.of(today, futureTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), isTaken = false, takenAt = null, medicationScheduleId = 1
        )
        whenever(mockMedicationRepo.getAllMedications()).thenReturn(flowOf(listOf(medication)))
        whenever(mockReminderRepo.getRemindersForMedication(1)).thenReturn(flowOf(listOf(reminder)))
        whenever(mockMedicationTypeRepo.getMedicationTypeById(1)).thenReturn(flowOf(sampleMedicationType))

        setupViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialGroup = viewModel.uiState.value.timeGroups.find { it.scheduledTime == futureTime }
        assertNotNull(initialGroup)
        val initialItem = initialGroup.reminders.find { it.id == "1" }
        assertNotNull(initialItem)

        viewModel.markFutureMedicationAsTaken(initialItem.id, initialItem.scheduledTime, false) // false for takeAtCurrentTime
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            var state = awaitItem()
            if(state.timeGroups.find { it.scheduledTime == futureTime }?.reminders?.find { it.id == "1"}?.isTaken == false) {
                state = awaitItem()
            }
            val updatedGroup = state.timeGroups.find { it.scheduledTime == futureTime }
            assertNotNull(updatedGroup)
            val updatedItem = updatedGroup.reminders.find { it.id == "1" }
            assertNotNull(updatedItem)
            assertTrue(updatedItem.isTaken)
            assertEquals(futureTime, updatedItem.actualTakenTime)
            assertEquals(1, updatedGroup.takenCount) // Verify takenCount
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
