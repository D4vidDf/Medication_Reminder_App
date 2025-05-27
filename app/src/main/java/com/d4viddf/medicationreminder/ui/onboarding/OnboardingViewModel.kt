package com.d4viddf.medicationreminder.ui.onboarding

import androidx.lifecycle.ViewModel
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.settings.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4viddf.medicationreminder.settings.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted: StateFlow<Boolean> = _notificationPermissionGranted.asStateFlow()

    private val _exactAlarmPermissionGranted = MutableStateFlow(false)
    val exactAlarmPermissionGranted: StateFlow<Boolean> = _exactAlarmPermissionGranted.asStateFlow()

    fun setOnboardingFinished() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
        }
    }

    // --- Notification Permission ---
    fun checkInitialNotificationPermissionStatus(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _notificationPermissionGranted.value = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Notification Permissions are automatically granted on SDK < 33
            _notificationPermissionGranted.value = true
        }
    }

    fun setNotificationPermissionGranted(isGranted: Boolean) {
        _notificationPermissionGranted.value = isGranted
    }

    // --- Exact Alarm Permission ---
    fun checkInitialExactAlarmPermissionStatus(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            _exactAlarmPermissionGranted.value = alarmManager.canScheduleExactAlarms()
        } else {
            // For API < 31, this permission is effectively granted or not required in the same way
            _exactAlarmPermissionGranted.value = true
        }
    }

    fun setExactAlarmPermissionGranted(isGranted: Boolean) {
        // This might be called after user returns from settings
        // or directly if permission is not needed for older APIs
        _exactAlarmPermissionGranted.value = isGranted
    }
}
