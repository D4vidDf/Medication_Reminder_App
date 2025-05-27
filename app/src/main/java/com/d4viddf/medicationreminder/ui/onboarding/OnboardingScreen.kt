package com.d4viddf.medicationreminder.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
// De-duplicated imports:
import android.Manifest
import android.content.ContextWrapper // For Preview
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
// Removed Color import as it's no longer needed for the first page directly
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
// Import Pages
import com.d4viddf.medicationreminder.ui.onboarding.OnboardingPages.BatteryOptimizationPage
import com.d4viddf.medicationreminder.ui.onboarding.OnboardingPages.ExactAlarmPermissionPage
import com.d4viddf.medicationreminder.ui.onboarding.OnboardingPages.NotificationPermissionPage
import com.d4viddf.medicationreminder.ui.onboarding.OnboardingPages.WelcomePage
// Import a Theme if not already (assuming MedicationReminderTheme from preview example)
import com.d4viddf.medicationreminder.ui.theme.MedicationReminderTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OnboardingScreen(
    activity: android.app.Activity, // Added activity parameter
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pageCount = 4 // Welcome, Notifications, Exact Alarms, Battery Optimization
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val windowSizeClass = calculateWindowSizeClass(activity) // Updated call
    val context = LocalContext.current
    val notificationPermissionGranted by viewModel.notificationPermissionGranted.collectAsState()
    val exactAlarmPermissionGranted by viewModel.exactAlarmPermissionGranted.collectAsState()

    // Initial permission checks
    LaunchedEffect(Unit) {
        viewModel.checkInitialNotificationPermissionStatus(context)
        viewModel.checkInitialExactAlarmPermissionStatus(context)
    }

    // Lifecycle observer for re-checking permissions on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkInitialNotificationPermissionStatus(context)
                viewModel.checkInitialExactAlarmPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.setNotificationPermissionGranted(isGranted)
        }
    )

    // Page count is already 4, as defined in the initial setup.

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(16.dp),
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.setOnboardingFinished()
                            onOnboardingComplete()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < pageCount - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else { // Last page
                                viewModel.setOnboardingFinished()
                                onOnboardingComplete()
                            }
                        },
                        enabled = if (pagerState.currentPage == pageCount - 1) { // Last page (Finish button)
                                      notificationPermissionGranted && exactAlarmPermissionGranted
                                  } else { // "Next" button for previous pages
                                      !(pagerState.currentPage == 1 && !notificationPermissionGranted) &&
                                      !(pagerState.currentPage == 2 && !exactAlarmPermissionGranted)
                                  },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (pagerState.currentPage < pageCount - 1) "Next" else "Finish")
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> NotificationPermissionPage(
                    viewModel = viewModel,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setNotificationPermissionGranted(true)
                        }
                    }
                )
                2 -> ExactAlarmPermissionPage(
                    viewModel = viewModel,
                    onRequestPermissionSettings = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                })
                            } catch (e: Exception) {
                                // Fallback: Open general app settings
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                })
                            }
                        } else {
                            viewModel.setExactAlarmPermissionGranted(true) // Not needed or auto-granted
                        }
                    }
                )
                else -> {
                    // Placeholder for last page (Battery Opt) -> Now BatteryOptimizationPage
                    // val pageColor = Color.Yellow.copy(alpha = 0.3f) // Page 4 (index 3)
                    BatteryOptimizationPage (
                        onRequestSettings = {
                            val packageName = context.packageName
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                intent.data = Uri.parse("package:$packageName")
                                context.startActivity(intent)
                            } catch (e: android.content.ActivityNotFoundException) {
                                try {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.data = Uri.parse("package:$packageName")
                                    context.startActivity(intent)
                                } catch (e2: android.content.ActivityNotFoundException) {
                                    // Optionally, could try Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                                    // or show a Toast message if all fail.
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    // Get activity from LocalContext for preview purposes
    val context = LocalContext.current
    // Attempt to get Activity, more robustly
    val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
        .filterIsInstance<android.app.Activity>()
        .firstOrNull()

    if (activity != null) {
        MedicationReminderTheme { // Assuming a theme wrapper like this exists
            OnboardingScreen(
                activity = activity,
                onOnboardingComplete = {}
                // Note: viewModel might cause issues in preview if not handled with a fake/mock.
                // For now, ensuring it compiles. If OnboardingViewModel is complex,
                // hiltViewModel() will not work correctly in previews without further setup.
            )
        }
    } else {
        Text("Preview not available: Activity context not found.")
    }
}
// Ensure OnboardingPages is also available for preview if needed, or mock its behavior.
// For this specific change, only OnboardingScreenPreview is directly affected by the import.
