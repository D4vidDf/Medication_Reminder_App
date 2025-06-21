package com.d4viddf.medicationreminder.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout // Added
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController // Corrected order
import androidx.navigation.NavType
import com.d4viddf.medicationreminder.repository.UserPreferencesRepository
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.d4viddf.medicationreminder.ui.screens.settings.ResponsiveSettingsScaffold
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.hilt.navigation.compose.hiltViewModel // Added for ViewModel instantiation
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Removed
import androidx.compose.ui.res.painterResource // Added import
import androidx.compose.ui.res.stringResource
import com.d4viddf.medicationreminder.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue // For state
import androidx.compose.runtime.mutableStateOf // For state
import androidx.compose.runtime.remember // For state
import androidx.compose.runtime.setValue // For state
import com.d4viddf.medicationreminder.ui.colors.MedicationColor // Added import
import com.d4viddf.medicationreminder.ui.screens.medication.AllSchedulesScreen
import com.d4viddf.medicationreminder.ui.screens.medication.MedicationDetailsScreen
import com.d4viddf.medicationreminder.ui.screens.medication.MedicationGraphScreen
import com.d4viddf.medicationreminder.ui.screens.medication.MedicationHistoryScreen
import com.d4viddf.medicationreminder.ui.screens.medication.MedicationInfoScreen

// Define the routes for navigation

const val MEDICATION_ID_ARG = "medicationId" // Common argument name
const val SHOW_TODAY_ARG = "showToday" // Argument for AllSchedulesScreen

sealed class Screen(val route: String) {
    object Today : Screen("today") // New home screen
    object MedicationLibrary : Screen("medication_library") // Old home screen
    object AddMedication : Screen("addMedication")
    object AddMedicationChoice : Screen("addMedicationChoice")
    object MedicationDetails : Screen("medicationDetails/{$MEDICATION_ID_ARG}?enableSharedTransition={enableSharedTransition}") {
        fun createRoute(id: Int, enableSharedTransition: Boolean = true) = "medicationDetails/$id?enableSharedTransition=$enableSharedTransition"
    }
    object Settings : Screen("settings")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    data object Onboarding : Screen("onboarding_screen")

    object AllSchedules : Screen("all_schedules_screen/{$MEDICATION_ID_ARG}/{colorName}?$SHOW_TODAY_ARG={$SHOW_TODAY_ARG}") {
        fun createRoute(medicationId: Int, colorName: String, showToday: Boolean = false) = "all_schedules_screen/$medicationId/$colorName?$SHOW_TODAY_ARG=$showToday"
    }
    object MedicationHistory : Screen("medication_history_screen/{$MEDICATION_ID_ARG}/{colorName}") {
        fun createRoute(medicationId: Int, colorName: String, selectedDate: String? = null, selectedMonth: String? = null): String {
            var route = "medication_history_screen/$medicationId/$colorName"
            val queryParams = mutableListOf<String>()
            selectedDate?.let { queryParams.add("selectedDate=$it") }
            selectedMonth?.let { queryParams.add("selectedMonth=$it") }
            if (queryParams.isNotEmpty()) {
                route += "?" + queryParams.joinToString("&")
            }
            return route
        }
    }
    object MedicationGraph : Screen("medication_graph_screen/{$MEDICATION_ID_ARG}/{colorName}") {
        fun createRoute(medicationId: Int, colorName: String) = "medication_graph_screen/$medicationId/$colorName"
    }
    object MedicationInfo : Screen("medication_info_screen/{$MEDICATION_ID_ARG}/{colorName}") {
        fun createRoute(medicationId: Int, colorName: String) = "medication_info_screen/$medicationId/$colorName"
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier, // Add this line
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    isMainScaffold: Boolean, // Added parameter
    userPreferencesRepository: UserPreferencesRepository, // Still needed for OnboardingScreen
    startDestinationRoute: String // Add this new parameter
) {
    SharedTransitionLayout { // `this` is SharedTransitionScope
        val currentSharedTransitionScope = this // Capture SharedTransitionScope

        // val onboardingCompleted by userPreferencesRepository.onboardingCompletedFlow.collectAsState(initial = false) // REMOVE
        // val startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route // REMOVE

        NavHost(
            navController = navController,
            startDestination = startDestinationRoute, // USE THE PARAMETER HERE
            modifier = modifier.then(if (isMainScaffold) Modifier.fillMaxSize() else Modifier) // Apply incoming modifier and then conditional padding
        ) {
            composable(Screen.Onboarding.route) { // Added route for OnboardingScreen
                OnboardingScreen(
                    navController = navController,
                    userPreferencesRepository = userPreferencesRepository // Pass it here
                )
            }
            // Define TodayScreen (New Home)
            composable(Screen.Today.route) {
                TodayScreen(
                    navController = navController,
                    widthSizeClass = widthSizeClass
                )
            }
            composable(Screen.MedicationLibrary.route) { // Was Screen.Home.route
                // `this` is an AnimatedVisibilityScope
                HomeScreen( // This is the original HomeScreen, now for the medication library
                    navController = navController,
                    onMedicationClick = { medicationId ->
                        navController.navigate(Screen.MedicationDetails.createRoute(medicationId, enableSharedTransition = widthSizeClass == WindowWidthSizeClass.Compact))
                    },
                    widthSizeClass = widthSizeClass,
                    sharedTransitionScope = currentSharedTransitionScope,
                    animatedVisibilityScope = this
                )
            }
            composable(Screen.AddMedicationChoice.route) { // New entry
                AddMedicationChoiceScreen(
                    onSearchMedication = { navController.navigate(Screen.AddMedication.route) },
                    onUseCamera = { /* Functionality to be added later */ },
                    onClose = { navController.popBackStack() } // Add this line
                )
            }
            composable(Screen.AddMedication.route) {
                // `this` is an AnimatedVisibilityScope
                AddMedicationScreen(
                    // onNavigateBack = { navController.popBackStack() }, // Remove this
                    navController = navController, // Add this
                    widthSizeClass = widthSizeClass // Pass the widthSizeClass
                    // No animatedVisibilityScope passed
                )
            }
            composable(
                Screen.MedicationDetails.route,
                arguments = listOf(navArgument("enableSharedTransition") { type = NavType.BoolType; defaultValue = true })
            ) { backStackEntry ->
                // `this` is an AnimatedVisibilityScope
                val medicationId = backStackEntry.arguments?.getString(MEDICATION_ID_ARG)?.toIntOrNull()
                val enableSharedTransition = backStackEntry.arguments?.getBoolean("enableSharedTransition") ?: true
                if (medicationId != null) {
                    MedicationDetailsScreen(
                        medicationId = medicationId,
                        navController = navController, // Added this line
                        onNavigateBack = {
                            navController.previousBackStackEntry?.savedStateHandle?.set("medicationDetailClosed", true)
                            navController.popBackStack()
                        },
                        sharedTransitionScope = currentSharedTransitionScope, // Pass captured scope
                        animatedVisibilityScope = this, // Pass scope
                        isHostedInPane = false,
                        widthSizeClass = widthSizeClass, // Add this line
                        // Navigation callbacks for new screens from MedicationDetailScreen
                        onNavigateToAllSchedules = { medId, colorName ->
                            navController.navigate(
                                Screen.AllSchedules.createRoute(medId, colorName, true)
                            )
                        }, // Pass showToday = true from details screen

                        onNavigateToMedicationHistory = { medId, colorName ->
                            navController.navigate(
                                Screen.MedicationHistory.createRoute(medId, colorName)
                            )
                        },
                        onNavigateToMedicationGraph = { medId, colorName ->
                            navController.navigate(
                                Screen.MedicationGraph.createRoute(medId, colorName)
                            )
                        },
                        onNavigateToMedicationInfo = { medId, colorName ->
                            navController.navigate(
                                Screen.MedicationInfo.createRoute(medId, colorName)
                            )
                        }
                    )
                }
            }
            composable(Screen.Settings.route) {
                var currentSettingsTitleResId by remember { mutableStateOf(R.string.settings_screen_title) }
                var currentSettingsBackAction by remember { mutableStateOf<() -> Unit>({ navController.popBackStack() }) } // Explicit lambda wrapping

                if (widthSizeClass == WindowWidthSizeClass.Compact) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(id = currentSettingsTitleResId)) },
                                navigationIcon = {
                                    IconButton(onClick = { currentSettingsBackAction.invoke() }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.rounded_arrow_back_ios_24),
                                            contentDescription = stringResource(id = R.string.back) // Generic back description
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        ResponsiveSettingsScaffold(
                            widthSizeClass = widthSizeClass,
                            navController = navController,
                            updateTopBarActions = { titleResId, backAction ->
                                currentSettingsTitleResId = titleResId
                                currentSettingsBackAction = backAction
                            },
                            contentPadding = innerPadding, // Pass Scaffold's innerPadding
                            modifier = Modifier.fillMaxSize() // Responsive scaffold should fill the content area
                        )
                    }
                } else {
                    // Tablet layout: ResponsiveSettingsScaffold manages its own TopAppBar.
                    // updateTopBarActions and contentPadding are not strictly needed here as it manages its own Scaffold/TopAppBar.
                    // However, to keep the call signature consistent or if some root modifier is needed:
                    ResponsiveSettingsScaffold(
                        widthSizeClass = widthSizeClass,
                        navController = navController,
                        updateTopBarActions = { _, _ -> /* No-op for tablet as it handles its own top bar */ },
                        // contentPadding can be default or specific if ResponsiveSettingsScaffold needs it
                        modifier = Modifier.fillMaxSize() // Ensure it fills the designated area
                    )
                }
            }
            composable(Screen.Calendar.route) {
                // `this` is an AnimatedVisibilityScope
                CalendarScreen(
                    navController = navController,
                    widthSizeClass = widthSizeClass, // Added widthSizeClass
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMedicationDetail = { medicationId ->
                        navController.navigate(Screen.MedicationDetails.createRoute(medicationId, enableSharedTransition = false))
                    }
                    // No sharedTransitionScope or animatedVisibilityScope passed
                )
            }
            composable(Screen.Profile.route) {
                // `this` is an AnimatedVisibilityScope
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                    // No animatedVisibilityScope passed
                )
            }

            // Routes for the new screens
            composable(
                Screen.AllSchedules.route,
                arguments = listOf(
                    navArgument(MEDICATION_ID_ARG) { type = NavType.IntType },
                    navArgument("colorName") { type = NavType.StringType },
                    navArgument(SHOW_TODAY_ARG) { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getInt(MEDICATION_ID_ARG) ?: -1
                val colorName = backStackEntry.arguments?.getString("colorName")
                val showToday = backStackEntry.arguments?.getBoolean(SHOW_TODAY_ARG) ?: false
                AllSchedulesScreen(
                    medicationId = medicationId,
                    showToday = showToday, // Pass showToday argument
                    onNavigateBack = { navController.popBackStack() },
                    colorName = colorName ?: MedicationColor.LIGHT_ORANGE.name
                )
            }

            composable(
                Screen.MedicationHistory.route,
                arguments = listOf(
                    navArgument(MEDICATION_ID_ARG) { type = NavType.IntType },
                    navArgument("colorName") { type = NavType.StringType },
                    navArgument("selectedDate") { type = NavType.StringType; nullable = true },
                    navArgument("selectedMonth") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getInt(MEDICATION_ID_ARG) ?: -1
                val colorName = backStackEntry.arguments?.getString("colorName")
                val selectedDate = backStackEntry.arguments?.getString("selectedDate")
                val selectedMonth = backStackEntry.arguments?.getString("selectedMonth") // Extract selectedMonth
                MedicationHistoryScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() },
                    colorName = colorName ?: MedicationColor.LIGHT_ORANGE.name,
                    selectedDate = selectedDate, // Pass the extracted selectedDate
                    selectedMonth = selectedMonth // Pass selectedMonth
                )
            }

            composable(
                Screen.MedicationGraph.route,
                arguments = listOf(
                    navArgument(MEDICATION_ID_ARG) { type = NavType.IntType },
                    navArgument("colorName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getInt(MEDICATION_ID_ARG) ?: -1
                val colorName = backStackEntry.arguments?.getString("colorName")
                val medicationGraphViewModel: com.d4viddf.medicationreminder.viewmodel.MedicationGraphViewModel = hiltViewModel()
                MedicationGraphScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() },
                    colorName = colorName ?: MedicationColor.LIGHT_ORANGE.name,
                    viewModel = medicationGraphViewModel,
                    widthSizeClass = widthSizeClass, // Add this line
                    onNavigateToHistoryForDate = { medId, colorStr, date ->
                        navController.navigate(
                            Screen.MedicationHistory.createRoute(
                                medicationId = medId,
                                colorName = colorStr,
                                selectedDate = date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            )
                        )
                    },
                    onNavigateToHistoryForMonth = { medId, colorStr, yearMonth ->
                        navController.navigate(
                            Screen.MedicationHistory.createRoute(
                                medicationId = medId,
                                colorName = colorStr,
                                selectedMonth = yearMonth.toString() // YearMonth.toString() is "YYYY-MM"
                            )
                        )
                    }
                )
            }

            composable(
                Screen.MedicationInfo.route,
                arguments = listOf(
                    navArgument(MEDICATION_ID_ARG) { type = NavType.IntType },
                    navArgument("colorName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val medicationId = backStackEntry.arguments?.getInt(MEDICATION_ID_ARG) ?: -1
                val colorName = backStackEntry.arguments?.getString("colorName")
                MedicationInfoScreen(
                    medicationId = medicationId,
                    onNavigateBack = { navController.popBackStack() },
                    colorName = colorName ?: MedicationColor.LIGHT_ORANGE.name
                )
            }
        }
    }
}
