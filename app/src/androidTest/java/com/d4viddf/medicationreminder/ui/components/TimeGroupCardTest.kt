package com.d4viddf.medicationreminder.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import com.d4viddf.medicationreminder.viewmodel.TimeGroupDisplayData
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimeGroupCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockOnReminderClick: (medicationId: Int) -> Unit = mock()

    private val sampleMedicationType = MedicationType(id = 1, name = "Pill", imageUrl = null)

    @Test
    fun displaysScheduledTime() {
        val time = LocalTime.of(9, 30)
        val groupData = TimeGroupDisplayData(
            scheduledTime = time,
            reminders = emptyList()
        )
        composeTestRule.setContent {
            AppTheme {
                TimeGroupCard(timeGroupData = groupData, onReminderClick = mockOnReminderClick)
            }
        }
        composeTestRule.onNodeWithText(time.format(DateTimeFormatter.ofPattern("HH:mm"))).assertIsDisplayed()
    }

    @Test
    fun displaysTakenDosageSummary() {
        val time = LocalTime.of(9, 0)
        val reminder1 = TodayMedicationData("r1", 1, "MedA", "10mg", sampleMedicationType, time, actualTakenTime = time, isTaken = true, isFuture = false, medicationColor = MedicationColor.LIGHT_BLUE, onToggle = {})
        val reminder2 = TodayMedicationData("r2", 2, "MedB", "20mg", sampleMedicationType, time, null, isTaken = false, isFuture = false, medicationColor = MedicationColor.LIGHT_RED, onToggle = {})
        val groupData = TimeGroupDisplayData(
            scheduledTime = time,
            reminders = listOf(reminder1, reminder2),
            takenCount = 1,
            totalInGroup = 2
        )
        composeTestRule.setContent {
            AppTheme {
                TimeGroupCard(timeGroupData = groupData, onReminderClick = mockOnReminderClick)
            }
        }
        composeTestRule.onNodeWithText("1 / 2 Taken").assertIsDisplayed()
    }

    @Test
    fun displaysMultipleMedicationCards() {
        val time = LocalTime.of(10, 0)
        val reminder1 = TodayMedicationData("r1", 101, "Med A", "50mg", sampleMedicationType, time, null, false, false, MedicationColor.LIGHT_BLUE, {})
        val reminder2 = TodayMedicationData("r2", 102, "Med B", "10ml", sampleMedicationType, time, null, true, false, MedicationColor.LIGHT_GREEN, {})
        val groupData = TimeGroupDisplayData(
            scheduledTime = time,
            reminders = listOf(reminder1, reminder2),
            takenCount = 1, // reflect one taken
            totalInGroup = 2
        )

        composeTestRule.setContent {
            AppTheme {
                TimeGroupCard(timeGroupData = groupData, onReminderClick = mockOnReminderClick)
            }
        }
        composeTestRule.onNodeWithText("Med A").assertIsDisplayed()
        composeTestRule.onNodeWithText("50mg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Med B").assertIsDisplayed()
        composeTestRule.onNodeWithText("10ml").assertIsDisplayed()
    }

    @Test
    fun medicationCardClick_invokesOnReminderClick() {
        val time = LocalTime.of(11, 0)
        val reminderMedId = 105
        val reminder1 = TodayMedicationData("r1", reminderMedId, "Clickable Med", "50mg", sampleMedicationType, time, null, false, false, MedicationColor.LIGHT_RED, {})
        val groupData = TimeGroupDisplayData(
            scheduledTime = time,
            reminders = listOf(reminder1)
        )

        composeTestRule.setContent {
            AppTheme {
                TimeGroupCard(timeGroupData = groupData, onReminderClick = mockOnReminderClick)
            }
        }
        // MedicationCardTodayFinal itself is clickable now via its modifier in TimeGroupCard
        composeTestRule.onNodeWithText("Clickable Med").performClick()
        verify(mockOnReminderClick).invoke(reminderMedId)
    }

    @Test
    fun medicationNameTruncation_isAppliedByMedicationCard() {
        val time = LocalTime.of(12, 0)
        val longName = "Medication With A Very Long Name That Exceeds Three Words"
        val expectedTruncatedName = "Medication With A..."
        val reminder = TodayMedicationData("r1", 1, longName, "10mg", sampleMedicationType, time, null, false, false, MedicationColor.LIGHT_BLUE, {})
        val groupData = TimeGroupDisplayData(scheduledTime = time, reminders = listOf(reminder))

        composeTestRule.setContent {
            AppTheme {
                TimeGroupCard(timeGroupData = groupData, onReminderClick = mockOnReminderClick)
            }
        }
        composeTestRule.onNodeWithText(expectedTruncatedName).assertIsDisplayed()
        composeTestRule.onNodeWithText(longName).assertDoesNotExist()
    }
}
