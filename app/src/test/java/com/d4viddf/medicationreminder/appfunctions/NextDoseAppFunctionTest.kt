package com.d4viddf.medicationreminder.appfunctions

import android.content.Context
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import com.d4viddf.medicationreminder.data.MedicationScheduleRepository
import com.d4viddf.medicationreminder.data.NextDoseInfo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.NoSuchElementException

class NextDoseAppFunctionTest {

    private lateinit var context: Context
    private lateinit var mockRepository: MedicationScheduleRepository
    private lateinit var nextDoseAppFunction: NextDoseAppFunction
    private lateinit var mockAppFunctionContext: AppFunctionContext

    @Before
    fun setUp() {
        context = mockk(relaxed = true) // Relaxed mock for Context
        mockRepository = mockk()
        mockAppFunctionContext = mockk()

        // Instantiate NextDoseAppFunction with mocked context, then replace its repository
        nextDoseAppFunction = NextDoseAppFunction(context)
        // Use reflection or a testing constructor if repository is private and not easily replaceable
        // For this example, assuming we can replace it or the NextDoseAppFunction is modified for testing
        val repositoryField = nextDoseAppFunction.javaClass.getDeclaredField("medicationScheduleRepository")
        repositoryField.isAccessible = true
        repositoryField.set(nextDoseAppFunction, mockRepository)
    }

    @Test
    fun `execute with valid medication name returns success`() = runBlocking {
        val medicationName = "aspirin"
        val expectedDoseInfo = NextDoseInfo("Aspirin", "10:00 AM", "1 tablet")
        coEvery { mockRepository.getNextDose(medicationName) } returns expectedDoseInfo

        val requestData = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionName("CheckNextDose")
            .setFunctionInputs(requestData)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue(response is ExecuteAppFunctionResponse.Success)
        val successResponse = response as ExecuteAppFunctionResponse.Success
        assertEquals(expectedDoseInfo.nextDoseTime, successResponse.response.getString("nextDoseTime"))
        assertEquals(expectedDoseInfo.medicationName, successResponse.response.getString("medicationName"))
        assertEquals(expectedDoseInfo.doseAmount, successResponse.response.getString("doseAmount"))
    }

    @Test
    fun `execute with unknown medication name returns error`() = runBlocking {
        val medicationName = "unknownMed"
        coEvery { mockRepository.getNextDose(medicationName) } returns null

        val requestData = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionName("CheckNextDose")
            .setFunctionInputs(requestData)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue(response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertNotNull(errorResponse.throwable)
        assertTrue(errorResponse.throwable is NoSuchElementException)
        assertEquals("No schedule found for medication: $medicationName", errorResponse.throwable?.message)
    }

    @Test
    fun `execute with missing medication name parameter returns error`() = runBlocking {
        val requestData = AppFunctionData.Builder().build() // Missing medicationName
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionName("CheckNextDose")
            .setFunctionInputs(requestData)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue(response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertNotNull(errorResponse.throwable)
        assertTrue(errorResponse.throwable is IllegalArgumentException)
        assertEquals("medicationName parameter is missing", errorResponse.throwable?.message)
    }

    @Test
    fun `execute when repository throws exception returns error`() = runBlocking {
        val medicationName = "aspirin"
        val exception = RuntimeException("Database error")
        coEvery { mockRepository.getNextDose(medicationName) } throws exception

        val requestData = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionName("CheckNextDose")
            .setFunctionInputs(requestData)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue(response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertEquals(exception, errorResponse.throwable)
    }
}
