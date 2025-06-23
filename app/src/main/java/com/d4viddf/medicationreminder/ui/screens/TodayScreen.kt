package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.viewmodel.TodayViewModel
import com.d4viddf.medicationreminder.viewmodel.TodayScreenUiState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.d4viddf.medicationreminder.ui.components.MedicationCardTodayFinal
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.d4viddf.medicationreminder.ui.components.TodayMedicationData
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.rememberCoroutineScope
import com.d4viddf.medicationreminder.ui.screens.medication.MedicationDetailsScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row // Added for TakeFutureMedicationDialog
import androidx.compose.foundation.layout.Spacer // Added for TakeFutureMedicationDialog
import androidx.compose.foundation.layout.width // Added for TakeFutureMedicationDialog
import androidx.compose.material3.AlertDialog // Added for TakeFutureMedicationDialog
import androidx.compose.material3.TextButton // Added for TakeFutureMedicationDialog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TodayScreen(
    navController: NavController,
    widthSizeClass: WindowWidthSizeClass,
    viewModel: TodayViewModel = hiltViewModel()
    // No SharedTransitionScope or AnimatedVisibilityScope needed here unless TodayScreen itself does complex transitions
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDialogState by viewModel.showTakeFutureDialog.collectAsState()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Int>() // For medicationId
    val coroutineScope = rememberCoroutineScope()

    val onReminderClick: (medicationId: Int) -> Unit = { medicationId ->
        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            // Navigate to full screen MedicationDetailsScreen
            navController.navigate(Screen.MedicationDetails.createRoute(medicationId, enableSharedTransition = false)) // Shared transition off for now from Today
        } else {
            // Show in detail pane
            coroutineScope.launch {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, medicationId)
            }
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = scaffoldNavigator,
        listPane = {
            // This is a ThreePaneScaffoldPaneScope
            val compactListScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState()) // For compact TopAppBar
            // For LargeTopAppBar, if it should always be visible and large, no scrollBehavior is passed to it.
            // The Scaffold's nestedScroll modifier will apply to the content area.

            Scaffold(
                modifier = Modifier.then(
                    // Apply nestedScroll only if a scroll behavior is used by the app bar
                    if (widthSizeClass == WindowWidthSizeClass.Compact) Modifier.nestedScroll(compactListScrollBehavior.nestedScrollConnection)
                    else Modifier // No nestedScroll connection if LargeTopAppBar is fixed
                ),
                topBar = {
                    if (widthSizeClass == WindowWidthSizeClass.Compact) {
                        TopAppBar(
                            title = { Text(stringResource(id = R.string.today_screen_title)) },
                            scrollBehavior = compactListScrollBehavior // Behavior for compact scrolling TopAppBar
                        )
                    } else {
                        LargeTopAppBar(
                            title = { Text(stringResource(id = R.string.today_screen_title)) }
                            // No scrollBehavior, so it remains large and pinned
                        )
                    }
                }
            ) { innerPadding ->
                HandleUiState(
                    uiState = uiState,
                    innerPadding = innerPadding,
                    viewModel = viewModel,
                    onReminderClick = onReminderClick // Pass the click handler
                )

                showDialogState?.let { dialogState ->
                    TakeFutureMedicationDialog(
                        dialogState = dialogState,
                        onConfirmTakeNow = {
                            viewModel.markFutureMedicationAsTaken(dialogState.reminderId, dialogState.scheduledTime, true)
                        },
                        onConfirmTakeAtScheduledTime = {
                            viewModel.markFutureMedicationAsTaken(dialogState.reminderId, dialogState.scheduledTime, false)
                        },
                        onDismiss = { viewModel.dismissTakeFutureDialog() }
                    )
                }
            }
        },
        detailPane = {
            // This is a ThreePaneScaffoldPaneScope
            val selectedMedicationIdForDetail = scaffoldNavigator.currentDestination?.contentKey
            if (selectedMedicationIdForDetail != null) {
                MedicationDetailsScreen(
                    medicationId = selectedMedicationIdForDetail,
                    navController = navController, // For internal navigation from details if any
                    onNavigateBack = {
                        coroutineScope.launch { scaffoldNavigator.navigateBack() }
                    },
                    isHostedInPane = true, // Important for correct behavior in pane
                    widthSizeClass = widthSizeClass, // Pass width class
                    sharedTransitionScope = null,
                    animatedVisibilityScope = null,
                    onNavigateToAllSchedules = { medId, colorName -> navController.navigate(Screen.AllSchedules.createRoute(medId, colorName, true)) },
                    onNavigateToMedicationHistory = { medId, colorName -> navController.navigate(Screen.MedicationHistory.createRoute(medId, colorName)) },
                    onNavigateToMedicationGraph = { medId, colorName -> navController.navigate(Screen.MedicationGraph.createRoute(medId, colorName)) },
                    onNavigateToMedicationInfo = { medId, colorName -> navController.navigate(Screen.MedicationInfo.createRoute(medId, colorName)) }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.select_reminder_placeholder))
                }
            }
        }
    )
}


@Composable
private fun HandleUiState(
    uiState: TodayScreenUiState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    viewModel: TodayViewModel,
    onReminderClick: (medicationId: Int) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        uiState.timeGroups.isEmpty() && !uiState.isLoading -> { // Use timeGroups
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.today_screen_no_reminders),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    // Show separator even if list is empty, directly in this Box
                    CurrentTimeSeparator(currentTime = uiState.currentTime)
                }
            }
        }
        else -> {
            BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) { // Wrap with BoxWithConstraints
                val estimatedItemHeight = 120.dp // Estimate of a TimeGroupCard height
                val bottomPadding = (this.maxHeight - estimatedItemHeight).coerceAtLeast(0.dp)

                TodayRemindersList(
                    timeGroups = uiState.timeGroups,
                    currentTime = uiState.currentTime,
                    modifier = Modifier.fillMaxSize(), // TodayRemindersList will fill BoxWithConstraints
                    onReminderClick = onReminderClick,
                    bottomContentPadding = bottomPadding // Pass calculated bottom padding
                )
            }
        }
    }
}

@Composable
fun TodayRemindersList(
    timeGroups: List<TimeGroupDisplayData>,
    currentTime: LocalTime,
    modifier: Modifier = Modifier,
    onReminderClick: (medicationId: Int) -> Unit,
    bottomContentPadding: Dp // New parameter for bottom padding
) {
    // timeGroups are already sorted by the ViewModel
    var separatorInserted = false
    val lazyListState = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(timeGroups, currentTime, lazyListState.isScrollInProgress, lazyListState.layoutInfo) {
        if (lazyListState.isScrollInProgress) return@LaunchedEffect

        val totalLazyColumnItems = lazyListState.layoutInfo.totalItemsCount
        if (totalLazyColumnItems == 0 && timeGroups.isNotEmpty()) {
            return@LaunchedEffect
        }

        var targetIndex = 0
        var _separatorAlreadyInsertedUpToTarget = false

        if (timeGroups.isEmpty()) {
            targetIndex = 0
        } else {
            var found = false
            var currentLazyIndex = 0
            for (group in timeGroups) {
                if (group.scheduledTime.isAfter(currentTime) && !_separatorAlreadyInsertedUpToTarget) {
                    if (group.scheduledTime >= currentTime) {
                        targetIndex = currentLazyIndex
                        found = true
                        break
                    }
                    currentLazyIndex++
                    _separatorAlreadyInsertedUpToTarget = true
                }
                if (group.scheduledTime >= currentTime) {
                    targetIndex = currentLazyIndex
                    found = true
                    break
                }
                currentLazyIndex++
            }
            if (!found && timeGroups.isNotEmpty()) {
                var itemCount = 0
                itemCount = timeGroups.size
                targetIndex = itemCount
            }
        }

        val currentTotalItems = lazyListState.layoutInfo.totalItemsCount
        if (targetIndex >= 0 && targetIndex < currentTotalItems) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(targetIndex)
            }
        } else if (timeGroups.isEmpty() && currentTotalItems > 0 && targetIndex == 0) {
             coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        } else if (currentTotalItems > 0 && targetIndex >= currentTotalItems) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem((currentTotalItems - 1).coerceAtLeast(0))
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = bottomContentPadding) // Apply dynamic bottom padding
    ) {
        timeGroups.forEach { timeGroupData ->
            if (!separatorInserted && timeGroupData.scheduledTime.isAfter(currentTime)) {
                item(key = "separator_before_${timeGroupData.scheduledTime}") {
                    CurrentTimeSeparator(currentTime = currentTime)
                }
                separatorInserted = true
            }

            item(key = "group_${timeGroupData.scheduledTime}") {
                TimeGroupCard(
                    timeGroupData = timeGroupData,
                    onReminderClick = onReminderClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // Padding for each TimeGroupCard
                )
            }
        }

        // If separator hasn't been inserted yet (all reminders are in the past or exactly at current time)
        // and there are items in the list.
        if (!separatorInserted && timeGroups.isNotEmpty()) {
            item(key = "separator_at_end") {
                CurrentTimeSeparator(currentTime = currentTime) // Uncommented
            }
            // No need to set separatorInserted = true here as it's the end
        }
        // If timeGroups is empty, the empty state in HandleUiState shows the separator. (This was already handled there)
    }
}

@Composable
fun CurrentTimeSeparator(currentTime: LocalTime) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = MaterialTheme.colorScheme.primary, radius = size.minDimension / 2) // Uncommented
        }
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun getCardShape(index: Int, totalItemsInGroup: Int): RoundedCornerShape {
    val cornerRadius = 12.dp
    val noCornerRadius = 0.dp

    return when {
        totalItemsInGroup == 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = noCornerRadius, bottomEnd = noCornerRadius)
        index == totalItemsInGroup - 1 -> RoundedCornerShape(topStart = noCornerRadius, topEnd = noCornerRadius, bottomStart = cornerRadius, bottomEnd = cornerRadius)
        else -> RoundedCornerShape(noCornerRadius)
    }
}

@Composable
fun TakeFutureMedicationDialog(
    dialogState: TodayViewModel.TakeFutureDialogState,
    onConfirmTakeNow: () -> Unit,
    onConfirmTakeAtScheduledTime: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_take_future_title)) },
        text = {
            Text(text = stringResource(
                R.string.dialog_take_future_message,
                dialogState.medicationName,
                dialogState.scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            ))
        },
        confirmButton = {
            TextButton(onClick = onConfirmTakeNow) {
                Text(stringResource(R.string.dialog_take_future_action_now))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onConfirmTakeAtScheduledTime) {
                    Text(stringResource(R.string.dialog_take_future_action_scheduled_time))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_cancel_button))
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Today Screen Compact")
@Composable
fun TodayScreenCompactPreview() {
    AppTheme {
        TodayScreen(
            navController = rememberNavController(),
            widthSizeClass = WindowWidthSizeClass.Compact
        )
    }
}

@Preview(showBackground = true, name = "Today Screen Medium", widthDp = 700)
@Composable
fun TodayScreenMediumPreview() {
    AppTheme {
        TodayScreen(
            navController = rememberNavController(),
            widthSizeClass = WindowWidthSizeClass.Medium
        )
    }
}

@Preview(showBackground = true, name = "Today Screen Expanded", widthDp = 1024)
@Composable
fun TodayScreenExpandedPreview() {
    AppTheme {
        TodayScreen(
            navController = rememberNavController(),
            widthSizeClass = WindowWidthSizeClass.Expanded
        )
    }
}
