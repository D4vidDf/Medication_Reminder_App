package com.d4viddf.medicationreminder.ui.onboarding

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.d4viddf.medicationreminder.settings.OnboardingPreferences
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var mockPreferences: OnboardingPreferences
    private lateinit var mockContext: Context
    private lateinit var mockAlarmManager: AlarmManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockPreferences = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockAlarmManager = mockk(relaxed = true)

        // Mock context.getSystemService for AlarmManager
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        // Mock Build.VERSION.SDK_INT using mockkStatic
        // This will be overridden in specific tests as needed
        mockkStatic(Build.VERSION::class)

        viewModel = OnboardingViewModel(mockPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Build.VERSION::class) // Clear static mock
        unmockkAll() // Clear all other mocks
    }

    @Test
    fun `setOnboardingFinished updates preferences`() = runTest(testDispatcher) {
        viewModel.setOnboardingFinished()
        coVerify { mockPreferences.setOnboardingCompleted(true) }
    }

    @Test
    fun `setNotificationPermissionGranted updates flow directly`() = runTest(testDispatcher) {
        viewModel.setNotificationPermissionGranted(true)
        assertTrue(viewModel.notificationPermissionGranted.first())

        viewModel.setNotificationPermissionGranted(false)
        assertFalse(viewModel.notificationPermissionGranted.first())
    }

    @Test
    fun `checkInitialNotificationPermissionStatus granted on Tiramisu (API 33)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.TIRAMISU
        every { mockContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_GRANTED
        
        viewModel.checkInitialNotificationPermissionStatus(mockContext)
        
        assertTrue(viewModel.notificationPermissionGranted.first())
        verify { mockContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) }
    }

    @Test
    fun `checkInitialNotificationPermissionStatus denied on Tiramisu (API 33)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.TIRAMISU
        every { mockContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_DENIED
        
        viewModel.checkInitialNotificationPermissionStatus(mockContext)
        
        assertFalse(viewModel.notificationPermissionGranted.first())
        verify { mockContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) }
    }

    @Test
    fun `checkInitialNotificationPermissionStatus always true below Tiramisu (e_g_ API 32)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.S_V2 // API 32
        
        viewModel.checkInitialNotificationPermissionStatus(mockContext)
        
        assertTrue(viewModel.notificationPermissionGranted.first())
        verify(exactly = 0) { mockContext.checkSelfPermission(any()) } // Should not be called
    }

    @Test
    fun `setExactAlarmPermissionGranted updates flow directly`() = runTest(testDispatcher) {
        viewModel.setExactAlarmPermissionGranted(true)
        assertTrue(viewModel.exactAlarmPermissionGranted.first())

        viewModel.setExactAlarmPermissionGranted(false)
        assertFalse(viewModel.exactAlarmPermissionGranted.first())
    }

    @Test
    fun `checkInitialExactAlarmPermissionStatus granted on S (API 31)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.S
        every { mockAlarmManager.canScheduleExactAlarms() } returns true
        
        viewModel.checkInitialExactAlarmPermissionStatus(mockContext)
        
        assertTrue(viewModel.exactAlarmPermissionGranted.first())
        verify { mockAlarmManager.canScheduleExactAlarms() }
    }

    @Test
    fun `checkInitialExactAlarmPermissionStatus denied on S (API 31)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.S
        every { mockAlarmManager.canScheduleExactAlarms() } returns false
        
        viewModel.checkInitialExactAlarmPermissionStatus(mockContext)
        
        assertFalse(viewModel.exactAlarmPermissionGranted.first())
        verify { mockAlarmManager.canScheduleExactAlarms() }
    }

    @Test
    fun `checkInitialExactAlarmPermissionStatus always true below S (e_g_ API 30)`() = runTest(testDispatcher) {
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R // API 30
        
        viewModel.checkInitialExactAlarmPermissionStatus(mockContext)
        
        assertTrue(viewModel.exactAlarmPermissionGranted.first())
        verify(exactly = 0) { mockAlarmManager.canScheduleExactAlarms() } // Should not be called
    }
}
