package com.d4viddf.medicationreminder.ui.screens.medication


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
// Row import is already present
// Spacer import is already present
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState // Added
// import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons // Base for Icons.Filled.FilterList and Icons.Filled.Close
import androidx.compose.material.icons.filled.Close // Added
import androidx.compose.material.icons.filled.FilterList // Specific import for FilterList
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Removed in previous step
// import androidx.compose.material.icons.filled.CalendarToday // Removed
// import androidx.compose.material.icons.filled.SwapVert // Removed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker // Added
import androidx.compose.material3.DatePickerDefaults // Added
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.HorizontalDivider // Added for clarity, though Divider might resolve to this.
import androidx.compose.material3.Divider // Keep for existing Sort Order Divider if different.
import androidx.compose.material3.DrawerValue // Added
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet // Added
import androidx.compose.material3.ModalNavigationDrawer // Added
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberDrawerState // Added
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Added
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow // Added
import androidx.compose.ui.Alignment // Already present, confirmed
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration // Added import
import androidx.compose.ui.res.painterResource // Added import
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.data.MedicationHistoryEntry
import com.d4viddf.medicationreminder.ui.colors.MedicationColor
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Removed by previous rule, but ensure it's gone or remove again
import com.d4viddf.medicationreminder.ui.theme.AppTheme
import com.d4viddf.medicationreminder.ui.theme.MedicationSpecificTheme
import com.d4viddf.medicationreminder.viewmodel.MedicationHistoryViewModel
import kotlinx.coroutines.flow.collect // Added
import kotlinx.coroutines.launch // Added
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

// Sealed interface for list items
sealed interface HistoryListItemType
data class MonthHeader(val monthYear: String, val id: String = "month_header_$monthYear") : HistoryListItemType
data class HistoryEntryItem(val entry: MedicationHistoryEntry, val originalId: String) : HistoryListItemType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    medicationId: Int,
    colorName: String,
    onNavigateBack: () -> Unit,
    viewModel: MedicationHistoryViewModel? = hiltViewModel(), // Made nullable for preview
    selectedDate: String? = null, // Existing parameter
    selectedMonth: String? = null // New parameter for YYYY-MM
) {
    val medicationColor = remember(colorName) {
        try {
            MedicationColor.valueOf(colorName)
        } catch (e: IllegalArgumentException) {
            MedicationColor.LIGHT_ORANGE // Fallback
        }
    }

    // val medicationName by viewModel?.medicationName?.collectAsState() ?: remember { mutableStateOf( "Medication History (Preview)") }
    // historyEntries, isLoading, error, currentFilter, sortAscending will be accessed via viewModel in the new composables or collected where needed.

    // var showDateRangeDialog by remember { mutableStateOf(false) } // REMOVED - No longer needed at screen level
    // val currentFilter by viewModel?.dateFilter?.collectAsState() ?: remember { mutableStateOf<Pair<LocalDate?, LocalDate?>?>(null) } // REMOVED - No longer needed for screen-level dialog

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()) // Changed scroll behavior

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val isLargeScreen = screenWidthDp >= 840.dp

    // var currentMonthYearText by remember { mutableStateOf("") } // REMOVED state for MonthYear text

    LaunchedEffect(medicationId, viewModel, selectedDate, selectedMonth) { // Added selectedMonth to key
        var parsedSelectedDate: LocalDate? = null
        var parsedSelectedMonth: YearMonth? = null

        if (selectedDate != null && selectedDate.isNotBlank()) {
            try {
                parsedSelectedDate = LocalDate.parse(selectedDate)
                // Log.d("MedHistoryScreen", "Parsed selectedDate: $parsedSelectedDate")
            } catch (e: DateTimeParseException) {
                // Log.e("MedHistoryScreen", "Failed to parse selectedDate string: '$selectedDate'", e)
            }
        }

        if (selectedMonth != null && selectedMonth.isNotBlank()) {
            try {
                parsedSelectedMonth = YearMonth.parse(selectedMonth) // YearMonth.parse expects "YYYY-MM"
                // Log.d("MedHistoryScreen", "Parsed selectedMonth: $parsedSelectedMonth")
            } catch (e: DateTimeParseException) {
                // Log.e("MedHistoryScreen", "Failed to parse selectedMonth string: '$selectedMonth'", e)
            }
        }
        // ViewModel will prioritize selectedDate if both are somehow provided
        viewModel?.loadInitialHistory(medicationId, parsedSelectedDate, parsedSelectedMonth)
    }

    // REMOVED DateRangePickerDialog for Phone Layout - It's now inside HistoryFilterPane, used by ModalDrawer

    MedicationSpecificTheme(medicationColor = medicationColor) {
        if (isLargeScreen) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        title = { Text("History") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_arrow_back_ios_24),
                                    contentDescription = stringResource(id = R.string.back_button_cd)
                                )
                            }
                        },
                        actions = { /* No actions for large screen */ },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Spacer(modifier = Modifier.weight(1f)) // REMOVED Spacer
                        HistoryListContent(
                            viewModel = viewModel,
                            listModifier = Modifier.weight(2f).padding(horizontal = 8.dp) // Adjusted weight to 2f
                        )
                        if (viewModel != null) {
                            HistoryFilterPane(
                                viewModel = viewModel,
                                medicationColor = medicationColor,
                                modifier = Modifier.weight(1f).padding(start = 8.dp, end = 16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f)) // Placeholder for filter pane
                        }
                    }
                }
            }
        } else { // Phone layout
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.fillMaxSize()) { // Sheet takes full screen
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (viewModel != null) {
                                PhoneFilterSheetContent(
                                    viewModel = viewModel,
                                    medicationColor = medicationColor,
                                    onDismiss = { scope.launch { drawerState.close() } },
                                    modifier = Modifier.align(Alignment.Center)
                                                // .padding(top = 48.dp) // Example padding
                                )
                            } else {
                                Text("Loading filters...",
                                     modifier = Modifier.align(Alignment.Center).padding(16.dp))
                            }

                            IconButton(
                                onClick = { scope.launch { drawerState.close() } },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.close_drawer_cd)
                                )
                            }
                        }
                    }
                }
            ) { // Main content for ModalNavigationDrawer (visible when drawer is closed)
                Scaffold(
                    modifier = Modifier.fillMaxSize(), // Modifier for the Scaffold itself
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(stringResource(id = R.string.medication_history_screen_title)) },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.rounded_arrow_back_ios_24),
                                        contentDescription = stringResource(id = R.string.back_button_cd)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Filled.FilterList,
                                        contentDescription = stringResource(id = R.string.med_history_filter_button_cd)
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior // Apply scrollBehavior here
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        HistoryListContent(
                            viewModel = viewModel,
                            listModifier = Modifier.fillMaxSize() // Original padding is within HistoryListContent now if needed
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneFilterSheetContent(
    viewModel: MedicationHistoryViewModel,
    medicationColor: MedicationColor,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDateDialog by remember { mutableStateOf(false) }
    val currentFilter by viewModel.dateFilter.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filter",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp)
        )

        // Date Range Button
        val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }
        val dateButtonText = currentFilter?.let {
            val start = it.first?.format(dateFormatter) ?: "..."
            val end = it.second?.format(dateFormatter) ?: "..."
            if (it.first != null && it.second != null) "$start - $end" else "Select Date Range"
        } ?: "Select Date Range"

        OutlinedButton(
            onClick = { showDateDialog = true },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(dateButtonText)
        }

        if (showDateDialog) {
            val dateRangePickerState = rememberDateRangePickerState(
                initialSelectedStartDateMillis = currentFilter?.first?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                initialSelectedEndDateMillis = currentFilter?.second?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= Instant.now().toEpochMilli()
                    override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDateDialog = false },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            val startDateMillis = dateRangePickerState.selectedStartDateMillis
                            val endDateMillis = dateRangePickerState.selectedEndDateMillis
                            if (startDateMillis != null && endDateMillis != null) {
                                val startDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                                val endDate = Instant.ofEpochMilli(endDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                                viewModel.setDateFilter(startDate, endDate)
                            }
                            showDateDialog = false
                            onDismiss()
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
                    ) { Text("OK") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
                }
            ) {
                DateRangePicker(state = dateRangePickerState, title = null, headline = null, showModeToggle = true)
            }
        }

        // Sort Order Button
        OutlinedButton(
            onClick = {
                viewModel.setSortOrder(!sortAscending)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Text(if (sortAscending) "Sort: Oldest First" else "Sort: Newest First")
        }

        // Clear All Filters Button
        OutlinedButton(
            onClick = {
                viewModel.setDateFilter(null, null)
                // Optionally reset sort order here if desired: viewModel.setSortOrder(false) // Default to newest
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Text("Clear All Filters")
        }
    }
}

// OriginalFilterControlsRow is no longer needed and should be deleted.
// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// private fun OriginalFilterControlsRow(...) { ... }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryFilterPane(
    viewModel: MedicationHistoryViewModel?, // Nullable for preview, but logic below assumes non-null for core functionality
    medicationColor: MedicationColor,
    modifier: Modifier = Modifier
    // Removed onFiltersApplied parameter
) {
    // Ensure viewModel is not null for the core logic
    if (viewModel == null) {
        // Optionally display a placeholder or nothing if viewModel is null (e.g., in Preview)
        Text("Filter pane unavailable in this preview.", modifier = modifier)
        return
    }

    val currentFilter by viewModel.dateFilter.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    // Initialize DateRangePickerState
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = currentFilter?.first?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = currentFilter?.second?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= Instant.now().toEpochMilli()
            override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now().year
        }
    )

    // Removed redundant LaunchedEffect(currentFilter) as rememberDateRangePickerState handles re-initialization based on initial...Millis properties.

    // Sync DateRangePicker selections back to ViewModel
    LaunchedEffect(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val startMillis = dateRangePickerState.selectedStartDateMillis
        val endMillis = dateRangePickerState.selectedEndDateMillis
        if (startMillis != null && endMillis != null) {
            val startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            if (startDate != currentFilter?.first || endDate != currentFilter?.second) {
                viewModel.setDateFilter(startDate, endDate)
            }
        } else if (startMillis == null && endMillis == null && currentFilter != null) {
            // This handles if user deselects range in picker (if possible) and current filter is not already null
            viewModel.setDateFilter(null, null)
        }
    }

    Column(
        modifier = modifier.padding(top = 16.dp), // Keep overall padding
        verticalArrangement = Arrangement.spacedBy(8.dp) // Adjust spacing for tighter layout
    ) {
        val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }

        // Date Display Fields (Top)
        Row(
            modifier = Modifier.fillMaxWidth(), // Removed bottom padding, handled by Column's spacedBy
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = currentFilter?.first?.format(dateFormatter) ?: "",
                onValueChange = {},
                label = { Text("From") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Start Date") }
            )
            OutlinedTextField(
                value = currentFilter?.second?.format(dateFormatter) ?: "",
                onValueChange = {},
                label = { Text("To") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                placeholder = { Text("End Date") }
            )
        }

        // Label and Clear Button Row
        Row(
            modifier = Modifier.fillMaxWidth(), // Removed bottom padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selected Range",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp) // Adjusted padding
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { viewModel.setDateFilter(null, null) }
                // No specific padding here, relies on Row's arrangement or default button padding
            ) {
                Text("Clear")
            }
        }

        // Docked DateRangePicker
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.fillMaxWidth(),
            numberOfMonths = 1,
            showModeToggle = false,   // Changed to false as per refined plan
            title = null,
            headline = null
            // Colors should be preserved if set previously from DatePickerDefaults call (not shown here)
        )

        // REMOVED the old "Clear Date Filter" button as it's now combined with the label above.
        // REMOVED the old Text label for DateRangePicker as "Selected Range" serves this purpose.

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Sort Order Section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
            Text(
                stringResource(id = R.string.med_history_sort_order_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            OutlinedButton(
                onClick = {
                    viewModel.setSortOrder(!sortAscending)
                    // No onDismiss here anymore
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_swap_vert),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = if (sortAscending) stringResource(id = R.string.med_history_sort_by_oldest_button)
                           else stringResource(id = R.string.med_history_sort_by_newest_button),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        // Removed the old display-only DatePicker and related Spacer
    }
}


@Composable
private fun HistoryListContent(
    viewModel: MedicationHistoryViewModel?,
    listModifier: Modifier = Modifier
    // REMOVED onVisibleMonthYearChanged parameter
) {
    val historyEntries by viewModel?.filteredAndSortedHistory?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val isLoading by viewModel?.isLoading?.collectAsState() ?: remember { mutableStateOf(false) }
    val error by viewModel?.error?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
    // sortAscending is used by remember key for processHistoryEntries, so it should be collected here.
    val sortAscending by viewModel?.sortAscending?.collectAsState() ?: remember { mutableStateOf(false) }


    when {
        isLoading -> Box(modifier = listModifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        error != null -> Box(modifier = listModifier, contentAlignment = Alignment.Center) {
            Text(error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        historyEntries.isEmpty() -> Box(modifier = listModifier, contentAlignment = Alignment.Center) {
            Text(
                stringResource(id = R.string.med_history_no_history_found),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        else -> {
            val groupedItems = remember(historyEntries, sortAscending) { // sortAscending is needed here
                processHistoryEntries(historyEntries, sortAscending)
            }

            // REMOVED listState definition
            // REMOVED monthYearFormatter definition
            // REMOVED LaunchedEffect for observing scroll and updating month year

            if (groupedItems.isEmpty()) {
                Box(modifier = listModifier, contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(id = R.string.med_history_no_history_found),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = listModifier) { // REMOVED state = listState
                    items(groupedItems, key = { item ->
                        when (item) {
                            is MonthHeader -> item.id
                            is HistoryEntryItem -> item.originalId
                        }
                    }) { item ->
                        when (item) {
                            is MonthHeader -> {
                                Text(
                                    text = item.monthYear,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            is HistoryEntryItem -> {
                                MedicationHistoryListItem(entry = item.entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Function to process history entries and insert month headers - remains unchanged
private fun processHistoryEntries(
    entries: List<MedicationHistoryEntry>,
    sortAscending: Boolean // This parameter is used by the logic within the function if needed, or by remember key
): List<HistoryListItemType> {
    if (entries.isEmpty()) return emptyList()

    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val result = mutableListOf<HistoryListItemType>()
    var currentMonthYear = ""

    // The sorting is now handled by the ViewModel, so we respect the order of `entries`
    // If sortAscending was to be used here to re-sort, it would be done before grouping.
    // However, the prompt implies ViewModel handles sorting, so this function mainly groups.
    for (entry in entries) {
        val entryMonthYear = entry.originalDateTimeTaken.format(monthYearFormatter)
        if (entryMonthYear != currentMonthYear) {
            currentMonthYear = entryMonthYear
            result.add(MonthHeader(monthYear = currentMonthYear))
        }
        result.add(HistoryEntryItem(entry = entry, originalId = entry.id))
    }
    return result
}

@Composable
fun MedicationHistoryListItem(entry: MedicationHistoryEntry) { // This composable remains unchanged
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = entry.dateTaken.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.med_history_item_taken_at_prefix) + entry.timeTaken.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Medication History Screen (Loading)")
@Composable
fun MedicationHistoryScreenPreview_Loading() {
    AppTheme {
        MedicationHistoryScreen(
            medicationId = 1,
            colorName = "LIGHT_BLUE",
            onNavigateBack = {},
            viewModel = null
        )
    }
}

@Preview(showBackground = true, name = "Medication History List Item")
@Composable
fun MedicationHistoryListItemPreview() {
    AppTheme {
        MedicationHistoryListItem(
            entry = MedicationHistoryEntry(
                id = "preview1",
                medicationName = "Sample Med",
                dateTaken = LocalDate.now(),
                timeTaken = LocalTime.now(),
                originalDateTimeTaken = LocalDateTime.now()
            )
        )
    }
}
