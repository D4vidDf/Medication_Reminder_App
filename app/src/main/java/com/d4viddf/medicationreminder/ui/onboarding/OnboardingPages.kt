package com.d4viddf.medicationreminder.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive // Already present, ensure it's used
import androidx.compose.material.icons.filled.Alarm // For ExactAlarmPermissionPage
import androidx.compose.material.icons.outlined.BatterySaver // For BatteryOptimizationPage (changed from filled)
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button // Already present, ensure it's used
// androidx.compose.runtime.Composable is listed once at the top by the tool if de-duplicated.
// Explicitly ensuring only one Composable import if there were multiple distinct ones.
import androidx.compose.runtime.Composable // Ensure this is the only one
import androidx.compose.runtime.collectAsState // Already present
import androidx.compose.runtime.getValue // Already present
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// Remove OnboardingPreferences and LocalContext from here if they were added for previews
// import com.d4viddf.medicationreminder.settings.OnboardingPreferences - REMOVE IF PRESENT
// import androidx.compose.ui.platform.LocalContext - REMOVE IF PRESENT


@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MedicalServices,
            contentDescription = "Medical Services Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to Medication Reminder!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app helps you stay on track with your medication schedule, ensuring you never miss a dose.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun BatteryOptimizationPage(onRequestSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BatterySaver, // Changed from Filled.BatterySaver
            contentDescription = "Battery Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Allow Background Activity",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To ensure reminders work reliably, please allow the app to run in the background and adjust battery settings to 'Unrestricted' or 'No restrictions' for this app. This helps prevent the system from stopping reminders.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestSettings) {
            Text("Open Battery Settings")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "After adjusting settings, please return to the app to complete onboarding.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryOptimizationPagePreview() {
    MaterialTheme {
        BatteryOptimizationPage(onRequestSettings = {})
    }
}

@Composable
fun ExactAlarmPermissionPage(
    viewModel: OnboardingViewModel,
    onRequestPermissionSettings: () -> Unit
) {
    val permissionGranted by viewModel.exactAlarmPermissionGranted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Alarm, // Using a standard alarm icon
            contentDescription = "Alarm Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Enable Exact Alarms",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "For medication reminders to trigger precisely on time, the app needs permission to schedule exact alarms. Please enable this in the system settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (!permissionGranted) {
            Button(onClick = onRequestPermissionSettings) {
                Text("Open Alarm Settings")
            }
        } else {
            Text(
                text = "Thank you! Exact alarm permission is granted.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExactAlarmPermissionPagePreview() {
    MaterialTheme {
        // Removed problematic ViewModel instantiation for preview
        // Replace with a placeholder or a more robust preview setup if possible
        Text("Preview for ExactAlarmPermissionPage (ViewModel dependent)")
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePagePreview() {
    MaterialTheme { // Wrap with MaterialTheme for preview
        WelcomePage()
    }
}

@Composable
fun NotificationPermissionPage(
    viewModel: OnboardingViewModel,
    onRequestPermission: () -> Unit
) {
    val permissionGranted by viewModel.notificationPermissionGranted.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = "Notifications Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Enable Notifications",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To ensure you receive timely medication alerts, please allow notification access. This is crucial for the app's main functionality.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (!permissionGranted) {
            Button(onClick = onRequestPermission) {
                Text("Grant Notification Permission")
            }
        } else {
            Text(
                text = "Thank you! Notifications are enabled.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationPermissionPagePreview() {
    // This preview won't have a real ViewModel, so it will show the initial state.
    // For more complex previews, you might need a fake ViewModel.
    MaterialTheme {
        // Removed problematic ViewModel instantiation for preview
        // Replace with a placeholder or a more robust preview setup if possible
        Text("Preview for NotificationPermissionPage (ViewModel dependent)")
    }
}
