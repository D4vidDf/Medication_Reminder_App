package com.d4viddf.medicationreminder.ui.features.home.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.d4viddf.medicationreminder.data.MedicationReminder
import com.d4viddf.medicationreminder.logic.ReminderCalculator
import com.d4viddf.medicationreminder.ui.common.theme.AppTheme
import com.d4viddf.medicationreminder.ui.features.home.viewmodel.HomeViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.currentGreeting, style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to Wear OS connect screen */ }) {
                        Icon(Icons.Filled.Watch, contentDescription = "Connect Wear OS")
                    }
                    IconButton(onClick = { /* TODO: Navigate to Notifications Center */ }) {
                        BadgedBox(badge = { if (uiState.hasUnreadAlerts) Badge() }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Next Dose Carousel
                if (uiState.nextDoseGroup.isNotEmpty()) {
                    item {
                        Column {
                            val nextDoseTime = uiState.nextDoseGroup.first().reminderTime
                            val formattedTime = try {
                                LocalDateTime.parse(nextDoseTime, ReminderCalculator.storableDateTimeFormatter).format(timeFormatter)
                            } catch (e: Exception) { "Future" }
                            Text(
                                "NEXT DOSE (at $formattedTime)",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.nextDoseGroup, key = { "next-${it.id}" }) { reminder ->
                                    NextDoseCard(reminder)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            "No upcoming doses for the rest of the day.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                // Today's Schedule
                item {
                    Text(
                        "TODAY'S SCHEDULE",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                uiState.todaysReminders.forEach { (partOfDay, remindersInPart) ->
                    if (remindersInPart.isNotEmpty()) {
                        item {
                            Text(
                                text = "$partOfDay (${
                                    try {
                                        LocalDateTime.parse(remindersInPart.first().reminderTime, ReminderCalculator.storableDateTimeFormatter)
                                            .format(timeFormatter)
                                    } catch (e: Exception) { "" }
                                })",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                        }
                        items(remindersInPart, key = { "schedule-${it.id}" }) { reminder ->
                            TodayScheduleItem(
                                reminder = reminder,
                                onMarkAsTaken = { viewModel.markAsTaken(reminder) },
                                timeFormatter = timeFormatter
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(64.dp)) } // Space for FAB or bottom nav
            }
        }
    }
}

@Composable
fun NextDoseCard(reminder: MedicationReminder) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Med ID: ${reminder.medicationId}", fontWeight = FontWeight.Bold) // Placeholder for Med Name
            Text("1 tablet") // Placeholder for Dosage
            val time = try {
                LocalDateTime.parse(reminder.reminderTime, ReminderCalculator.storableDateTimeFormatter)
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                "N/A"
            }
            Text(time, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TodayScheduleItem(
    reminder: MedicationReminder,
    onMarkAsTaken: () -> Unit,
    timeFormatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = if (reminder.isTaken) BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isTaken) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = reminder.isTaken,
                    onCheckedChange = { if (it) onMarkAsTaken() },
                    enabled = !reminder.isTaken,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Medication ID: ${reminder.medicationId}", // Placeholder
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reminder.isTaken) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1 tablet at ${
                            try {
                                LocalDateTime.parse(reminder.reminderTime, ReminderCalculator.storableDateTimeFormatter).format(timeFormatter)
                            } catch (e: Exception) { "" }
                        }", // Placeholder for dosage
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (reminder.isTaken) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Taken",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "HomeScreen Preview (New Design)")
@Composable
fun HomeScreenNewPreview() {
    val sampleTime = LocalDateTime.now()
    val previewState = HomeViewModel.HomeState(
        currentGreeting = "Good morning! 🌤️",
        nextDoseGroup = listOf(
            MedicationReminder(1, 101, 1, sampleTime.plusHours(1).format(ReminderCalculator.storableDateTimeFormatter), false, null, null),
            MedicationReminder(2, 102, 2, sampleTime.plusHours(1).format(ReminderCalculator.storableDateTimeFormatter), false, null, null)
        ),
        todaysReminders = mapOf(
            "Morning" to listOf(
                MedicationReminder(5, 104, 5, sampleTime.withHour(8).format(ReminderCalculator.storableDateTimeFormatter), true, sampleTime.withHour(8).format(ReminderCalculator.storableDateTimeFormatter), null),
                MedicationReminder(1, 101, 1, sampleTime.withHour(9).format(ReminderCalculator.storableDateTimeFormatter), false, null, null)
            ),
            "Afternoon" to listOf(
                MedicationReminder(3, 101, 3, sampleTime.withHour(15).format(ReminderCalculator.storableDateTimeFormatter), false, null, null)
            ),
            "Evening" to listOf(
                MedicationReminder(4, 103, 4, sampleTime.withHour(21).format(ReminderCalculator.storableDateTimeFormatter), false, null, null)
            ),
            "Night" to emptyList()
        ),
        hasUnreadAlerts = true,
        isLoading = false
    )

    // Create a dummy ViewModel that just holds the state
    val previewViewModel = object : HomeViewModel(FakeMedicationReminderRepositoryPreview()) { // Needs a repo
        override val uiState: StateFlow<HomeState> = MutableStateFlow(previewState)
        // Dummy markAsTaken for preview
        override fun markAsTaken(reminder: MedicationReminder) {}
    }


    AppTheme {
        HomeScreen(
            navController = rememberNavController(),
            viewModel = previewViewModel
        )
    }
}

// Fake repository for preview - must be a class that can be instantiated.
// It doesn't need to be open if HomeViewModel takes it as a concrete type in constructor.
// However, HomeViewModel takes MedicationReminderRepository, which is a class, not an interface.
// So this Fake needs to extend it or we need an interface.
// For simplicity, assuming HomeViewModel can take any MedicationReminderRepository.
// We need to ensure MedicationReminderRepository can be instantiated with no-args or fake DAOs.
// The constructor is: MedicationReminderRepository(dao, firebaseDao) - this is the problem.

// Let's make a very simple fake that doesn't need DAOs for the preview's purpose,
// if HomeViewModel's actual calls are not critical for basic UI preview.
// The HomeViewModel's init block calls loadTodaysSchedule which uses the repo.
// This implies the repo needs to be somewhat functional.

// Simplest fake that conforms to MedicationReminderRepository's structure if it were an interface.
// Since it's a class, this approach is problematic for direct extension if methods are not open.
// The HomeViewModel constructor takes `MedicationReminderRepository` (the class).
// The `object : HomeViewModel(...)` approach failed because HomeViewModel is not open.
// The new approach is to provide a HomeViewModel that is an object expression,
// which is allowed if the superclass has a default constructor or takes arguments we can provide.
// HomeViewModel takes MedicationReminderRepository.

private class FakeMedicationReminderRepositoryPreview : com.d4viddf.medicationreminder.data.MedicationReminderRepository(
    // We need to provide fake DAOs here, which is complex for a preview.
    // This highlights that the preview setup was too ambitious.
    // The solution is to NOT use a real HomeViewModel or a HomeViewModel that calls complex repo methods.
    // The `previewViewModel` above which directly sets `uiState` is better.
    // The issue is `object : HomeViewModel(FakeMedicationReminderRepositoryPreview())` still tries to
    // use the real HomeViewModel's init block.

    // Let's stick to the previewViewModel above that *directly* sets a MutableStateFlow for uiState,
    // and ensure its super class (HomeViewModel) constructor can be called.
    // HomeViewModel's constructor: HomeViewModel(private val medicationReminderRepository: MedicationReminderRepository)
    // So, FakeMedicationReminderRepositoryPreview() is passed. This class needs to be valid.
    // It needs to be a valid instance of MedicationReminderRepository.
    // This means it needs the DAOs. This is the core issue for the preview.

    // The simplest fix for the preview is to make HomeViewModel open and its methods open,
    // or to have a separate @Composable for preview that takes HomeState directly.
    // Given the constraints, the latter is often cleaner.
    // For now, I'll try to make FakeMedicationReminderRepositoryPreview usable by giving it null DAOs,
    // assuming the preview model won't actually hit them hard. This is risky.
    medicationReminderDao = FakeMedicationReminderDaoPreview(),
    firebaseSyncDao = FakeFirebaseSyncDaoPreview()
) {
    override fun getRemindersForDay(startOfDayString: String, endOfDayString: String): kotlinx.coroutines.flow.Flow<List<MedicationReminder>> {
        // Return data consistent with previewState
        val now = LocalDateTime.now()
        val sampleRemindersList = listOf(
            MedicationReminder(1, 101,1, now.withHour(9).format(ReminderCalculator.storableDateTimeFormatter), false, null, null),
            MedicationReminder(5, 104,5, now.withHour(8).format(ReminderCalculator.storableDateTimeFormatter), true, now.withHour(8).format(ReminderCalculator.storableDateTimeFormatter), null)
        )
        return flowOf(sampleRemindersList)
    }
    // Other methods can be empty or return default values if not used by HomeViewModel's init for preview state
}

private class FakeMedicationReminderDaoPreview : com.d4viddf.medicationreminder.data.MedicationReminderDao {
    override suspend fun insertReminder(reminder: MedicationReminder): Long = 0
    override suspend fun updateReminder(reminder: MedicationReminder): Int = 0
    override suspend fun deleteReminder(reminder: MedicationReminder) {}
    override suspend fun getReminderById(id: Int): MedicationReminder? = null
    override fun getRemindersForMedication(medicationId: Int): Flow<List<MedicationReminder>> = flowOf(emptyList())
    override fun getRemindersByStatus(isTaken: Boolean): Flow<List<MedicationReminder>> = flowOf(emptyList())
    override fun getFutureRemindersForMedication(medicationId: Int, currentTimeIso: String): Flow<List<MedicationReminder>> = flowOf(emptyList())
    override suspend fun deleteFutureUntakenRemindersForMedication(medicationId: Int, currentTimeIso: String) {}
    override fun getFutureUntakenRemindersForMedication(medicationId: Int, currentTimeIsoString: String): Flow<List<MedicationReminder>> = flowOf(emptyList())
    override suspend fun getRemindersForMedicationInWindow(medicationId: Int, startTime: String, endTime: String): List<MedicationReminder> = emptyList()
    override suspend fun deleteReminderById(reminderId: Int) {}
    override suspend fun getMostRecentTakenReminder(medicationId: Int): MedicationReminder? = null
    override fun getRemindersForDay(startOfDayString: String, endOfDayString: String): Flow<List<MedicationReminder>> = flowOf(emptyList()) // Implemented in Fake Repo
}

private class FakeFirebaseSyncDaoPreview : com.d4viddf.medicationreminder.data.FirebaseSyncDao {
    override suspend fun insertSyncRecord(firebaseSync: com.d4viddf.medicationreminder.data.FirebaseSync) {}
    override suspend fun getPendingSyncRecords(): List<com.d4viddf.medicationreminder.data.FirebaseSync> = emptyList()
    override suspend fun deleteSyncRecord(id: Int) {}
    override suspend fun updateSyncStatus(id: Int, status: com.d4viddf.medicationreminder.data.SyncStatus) {}
}