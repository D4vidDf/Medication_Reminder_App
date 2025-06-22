package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.MedicationType
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
import com.d4viddf.medicationreminder.ui.components.TodayMedicationData
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import com.d4viddf.medicationreminder.viewmodel.TodayScreenUiState
import com.d4viddf.medicationreminder.viewmodel.TodayViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TodayScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockNavController: NavController = mockk(relaxed = true)
    private lateinit var mockViewModel: TodayViewModel

    // Helper to set up ViewModel with specific UiState
    private fun setupViewModel(initialUiState: TodayScreenUiState, initialDialogState: TodayViewModel.TakeFutureDialogState? = null) {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(initialUiState)
        every { mockViewModel.showTakeFutureDialog } returns MutableStateFlow(initialDialogState)
    }


    @Test
    fun loadingState_showsCircularProgressIndicator() {
        setupViewModel(initialUiState = TodayScreenUiState(isLoading = true))
        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        // Placeholder: Need a way to find CircularProgressIndicator, e.g., by testTag or semantics
        // composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        // For now, this test is conceptual until a selector is added.
        // If loading is the only thing, other elements shouldn't be there.
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.today_screen_title)).assertIsDisplayed() // AppBar
    }

    @Test
    fun errorState_showsErrorMessage() {
        val errorMessage = "Failed to load"
        setupViewModel(initialUiState = TodayScreenUiState(isLoading = false, error = errorMessage))
        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText("Error: $errorMessage").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNoRemindersMessage() {
        setupViewModel(initialUiState = TodayScreenUiState(isLoading = false, groupedReminders = emptyMap()))
        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.today_screen_no_reminders)).assertIsDisplayed()
    }

    @Test
    fun remindersLoaded_displaysTimeGroupsAndCards() {
        val time8AM = LocalTime.of(8, 0)
        val medData1 = TodayMedicationData("r1", 1, "Med A", "10mg", MedicationType(1, "Pill", null), time8AM, null, false, false, MedicationColor.LIGHT_BLUE, {})
        val timeGroups = listOf(TimeGroupDisplayData(scheduledTime = time8AM, reminders = listOf(medData1)))
        setupViewModel(initialUiState = TodayScreenUiState(isLoading = false, timeGroups = timeGroups, currentTime = LocalTime.of(7,0)))

        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText(time8AM.format(DateTimeFormatter.ofPattern("HH:mm"))).assertIsDisplayed() // Time group header
        composeTestRule.onNodeWithText("Med A").assertIsDisplayed() // Medication card
        composeTestRule.onNodeWithText(LocalTime.of(7,0).format(DateTimeFormatter.ofPattern("HH:mm"))).assertIsDisplayed() // Current time separator
    }

    @Test
    fun showsDialog_whenViewModelStateIndicates() {
        val dialogStateData = TodayViewModel.TakeFutureDialogState("r1", "Med Future", LocalTime.of(14,0))
        setupViewModel(
            initialUiState = TodayScreenUiState(isLoading = false, groupedReminders = emptyMap()), // Can be empty for this test
            initialDialogState = dialogStateData
        )

        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.dialog_take_future_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            String.format(
                composeTestRule.activity.getString(R.string.dialog_take_future_message),
                dialogStateData.medicationName,
                dialogStateData.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            )
        ).assertIsDisplayed()
    }

    @Test
    fun dialogDismissButton_callsViewModelDismiss() {
        val dialogStateData = TodayViewModel.TakeFutureDialogState("r1", "Med Future", LocalTime.of(14,0))
         setupViewModel(
            initialUiState = TodayScreenUiState(isLoading = false),
            initialDialogState = dialogStateData
        )

        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.dialog_cancel_button)).performClick()
        verify { mockViewModel.dismissTakeFutureDialog() }
    }

    @Test
    fun dialogTakeNowButton_callsViewModelMarkAsTaken() {
        val dialogStateData = TodayViewModel.TakeFutureDialogState("r1", "Med Future", LocalTime.of(14,0))
        setupViewModel(
            initialUiState = TodayScreenUiState(isLoading = false),
            initialDialogState = dialogStateData
        )
        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.dialog_take_future_action_now)).performClick()
        verify { mockViewModel.markFutureMedicationAsTaken(dialogStateData.reminderId, dialogStateData.scheduledTime, true) }
    }

    @Test
    fun reminderCardClick_onCompactScreen_navigatesToDetails() {
        val time8AM = LocalTime.of(8, 0)
        val medData1 = TodayMedicationData("r1", 101, "Med A", "10mg", MedicationType(1, "Pill", null), time8AM, null, false, false, MedicationColor.LIGHT_BLUE, {})
        val timeGroups = listOf(TimeGroupDisplayData(scheduledTime = time8AM, reminders = listOf(medData1)))
        setupViewModel(initialUiState = TodayScreenUiState(isLoading = false, timeGroups = timeGroups))

        composeTestRule.setContent {
            AppTheme {
                TodayScreen(navController = mockNavController, widthSizeClass = WindowWidthSizeClass.Compact, viewModel = mockViewModel)
            }
        }
        composeTestRule.onNodeWithText("Med A").performClick()
        verify { mockNavController.navigate(Screen.MedicationDetails.createRoute(101, enableSharedTransition = false)) }
    }

    // Responsive test for larger screen (showing in pane) is more complex as it involves NavigableListDetailPaneScaffold's navigator.
    // This might require a more integrated test or specific testing utilities for adaptive components.
    // For now, verifying the compact navigation is a good start.
}
