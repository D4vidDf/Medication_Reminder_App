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
// import androidx.compose.material.icons.Icons // Keep this if other icons from core material are used, if not, specific imports are better.
import androidx.compose.material.icons.Icons // Base for Icons.Filled.FilterList
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
import androidx.compose.material3.Divider
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
                        HistoryFilterPane(
                            viewModel = viewModel,
                            medicationColor = medicationColor,
                            modifier = Modifier.weight(1f).padding(start = 8.dp, end = 16.dp)
                        )
                    }
                }
            }
        } else { // Phone layout
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

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
                        actions = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = stringResource(id = R.string.med_history_filter_drawer_button_cd)
                                )
                            }
                        },
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
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.85f)) {
                            HistoryFilterPane(
                                viewModel = viewModel,
                                medicationColor = medicationColor,
                                modifier = Modifier.padding(16.dp),
                                onFiltersApplied = { scope.launch { drawerState.close() } }
                            )
                        }
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        HistoryListContent(
                            viewModel = viewModel,
                            listModifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                        )
                    }
                }
            }
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
    viewModel: MedicationHistoryViewModel?,
    medicationColor: MedicationColor,
    modifier: Modifier = Modifier,
    onFiltersApplied: (() -> Unit)? = null
) {
    val currentFilter by viewModel?.dateFilter?.collectAsState() ?: remember { mutableStateOf<Pair<LocalDate?, LocalDate?>?>(null) }
    val sortAscending by viewModel?.sortAscending?.collectAsState() ?: remember { mutableStateOf(false) }
    var showDateRangeDialogInPane by remember { mutableStateOf(false) }

    if (showDateRangeDialogInPane) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = currentFilter?.first?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            initialSelectedEndDateMillis = currentFilter?.second?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= Instant.now().toEpochMilli()
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year <= LocalDate.now().year
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangeDialogInPane = false },
            confirmButton = {
                Button(
                    onClick = {
                        val startDateMillis = dateRangePickerState.selectedStartDateMillis
                        val endDateMillis = dateRangePickerState.selectedEndDateMillis
                        if (startDateMillis != null && endDateMillis != null) {
                            val startDate = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            val endDate = Instant.ofEpochMilli(endDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel?.setDateFilter(startDate, endDate)
                        }
                        showDateRangeDialogInPane = false
                        onFiltersApplied?.invoke()
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = medicationColor.onBackgroundColor,
                        contentColor = medicationColor.cardColor
                    )
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDateRangeDialogInPane = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = medicationColor.onBackgroundColor,
                        contentColor = medicationColor.cardColor
                    )
                ) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, title = null, headline = null, showModeToggle = true)
        }
    }

    Column(
        modifier = modifier.padding(top = 16.dp), // Add some top padding for the pane
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Range Filter Section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(id = R.string.med_history_filter_by_date_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            OutlinedButton(
                onClick = { showDateRangeDialogInPane = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = currentFilter?.let {
                        val start = it.first?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "..."
                        val end = it.second?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) ?: "..."
                        "$start - $end"
                    } ?: stringResource(id = R.string.med_history_filter_select_range_button_label),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (currentFilter != null) {
                OutlinedButton(
                    onClick = {
                        viewModel?.setDateFilter(null, null)
                        onFiltersApplied?.invoke()
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Text(stringResource(id = R.string.med_history_filter_clear_button))
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 4.dp))

        // Sort Order Section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.Start) {
            Text(
                stringResource(id = R.string.med_history_sort_order_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            OutlinedButton(
                onClick = {
                    viewModel?.setSortOrder(!sortAscending)
                    onFiltersApplied?.invoke()
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

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel != null) {
            val currentFilterState = currentFilter // Local val for use
            val startDate = currentFilterState?.first

            val datePickerState = rememberDatePickerState(
                // key = currentFilterState, // Removed: DatePickerState does not have a direct 'key' parameter like this. Re-initialization is handled by `initialSelectedDateMillis` changing.
                initialSelectedDateMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                initialDisplayedMonthMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis(),
                yearRange = (LocalDate.now().year - 5)..(LocalDate.now().year + 5),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                    override fun isSelectableYear(year: Int): Boolean = true
                }
            )

            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = medicationColor.onBackgroundColor.copy(alpha = 0.3f),
                    selectedDayContentColor = medicationColor.cardColor,
                    // dayInSelectionRangeContainerColor = medicationColor.backgroundColor.copy(alpha = 0.4f), // REMOVED
                    // dayInSelectionRangeContentColor = medicationColor.textColor, // REMOVED
                    todayDateBorderColor = medicationColor.onBackgroundColor.copy(alpha = 0.7f),
                    todayContentColor = medicationColor.onBackgroundColor
                    // Example of other optional color settings:
                    // subheadContentColor = medicationColor.textColor,
                    // navigationContentColor = medicationColor.textColor
                )
            )
        }
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
