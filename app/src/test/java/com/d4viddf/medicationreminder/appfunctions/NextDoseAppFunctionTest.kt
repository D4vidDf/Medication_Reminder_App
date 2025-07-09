package com.d4viddf.medicationreminder.appfunctions

import android.content.Context
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import com.d4viddf.medicationreminder.data.MedicationScheduleRepository
import com.d4viddf.medicationreminder.data.NextDoseInfo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.NoSuchElementException

@ExperimentalCoroutinesApi
class NextDoseAppFunctionTest {

    private lateinit var context: Context
    private lateinit var mockRepository: MedicationScheduleRepository
    private lateinit var nextDoseAppFunction: NextDoseAppFunction
    private lateinit var mockAppFunctionContext: AppFunctionContext

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        mockRepository = mockk()
        mockAppFunctionContext = mockk(relaxed = true) // Relaxed mock for AppFunctionContext

        // Instantiate NextDoseAppFunction with mocked context
        nextDoseAppFunction = NextDoseAppFunction(context)

        // Replace the internally created repository with the mock using reflection
        // This is a common pattern for testing classes with internal dependencies.
        // Ensure this matches how the repository is actually named and accessed in NextDoseAppFunction.
        val repositoryField = nextDoseAppFunction.javaClass.getDeclaredField("medicationScheduleRepository")
        repositoryField.isAccessible = true
        repositoryField.set(nextDoseAppFunction, mockRepository)
    }

    @Test
    fun `execute with valid medication name returns success`() = runTest {
        val medicationName = "aspirin"
        val expectedDoseInfo = NextDoseInfo("Aspirin", "10:00 AM", "1 tablet")
        coEvery { mockRepository.getNextDose(medicationName) } returns expectedDoseInfo

        val requestFunctionInputs = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            // .setFunctionName("CheckNextDose") // Not directly part of request object, but good for context
            .setFunctionInputs(requestFunctionInputs)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue("Response should be Success", response is ExecuteAppFunctionResponse.Success)
        val successResponse = response as ExecuteAppFunctionResponse.Success
        val responseData = successResponse.response
        assertNotNull("Response data should not be null", responseData)
        assertEquals(expectedDoseInfo.nextDoseTime, responseData.getString("nextDoseTime"))
        assertEquals(expectedDoseInfo.medicationName, responseData.getString("medicationName"))
        assertEquals(expectedDoseInfo.doseAmount, responseData.getString("doseAmount"))
    }

    @Test
    fun `execute with unknown medication name returns error`() = runTest {
        val medicationName = "unknownMed"
        coEvery { mockRepository.getNextDose(medicationName) } returns null

        val requestFunctionInputs = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionInputs(requestFunctionInputs)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue("Response should be Error", response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertNotNull("Throwable should not be null", errorResponse.throwable)
        assertTrue("Throwable should be NoSuchElementException", errorResponse.throwable is NoSuchElementException)
        assertEquals("No schedule found for medication: $medicationName", errorResponse.throwable?.message)
    }

    @Test
    fun `execute with missing medication name parameter returns error`() = runTest {
        // Request data without the "medicationName" parameter
        val requestFunctionInputs = AppFunctionData.Builder().build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionInputs(requestFunctionInputs)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue("Response should be Error", response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertNotNull("Throwable should not be null", errorResponse.throwable)
        assertTrue("Throwable should be IllegalArgumentException", errorResponse.throwable is IllegalArgumentException)
        assertEquals("medicationName parameter is missing", errorResponse.throwable?.message)
    }

    @Test
    fun `execute when repository throws exception returns error`() = runTest {
        val medicationName = "aspirin"
        val expectedException = RuntimeException("Database error")
        coEvery { mockRepository.getNextDose(medicationName) } throws expectedException

        val requestFunctionInputs = AppFunctionData.Builder()
            .putString("medicationName", medicationName)
            .build()
        val request = ExecuteAppFunctionRequest.newBuilder()
            .setFunctionInputs(requestFunctionInputs)
            .build()

        val response = nextDoseAppFunction.execute(mockAppFunctionContext, request)

        assertTrue("Response should be Error", response is ExecuteAppFunctionResponse.Error)
        val errorResponse = response as ExecuteAppFunctionResponse.Error
        assertEquals("Throwables should be the same", expectedException, errorResponse.throwable)
    }
}
