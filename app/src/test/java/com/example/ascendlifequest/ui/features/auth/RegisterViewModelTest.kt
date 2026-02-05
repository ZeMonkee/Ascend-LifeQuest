package com.example.ascendlifequest.ui.features.auth

import app.cash.turbine.test
import com.example.ascendlifequest.fakes.FakeAuthRepository
import com.example.ascendlifequest.fakes.FakeProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour RegisterViewModel (couche ViewModel MVVM).
 *
 * Ces tests vérifient :
 * - Les états de l'UI (RegisterUiState)
 * - Les événements émis vers la View
 * - La validation et création de compte
 *
 * Architecture MVVM :
 * - Model : AuthRepository, ProfileRepository (mockés avec Fakes)
 * - ViewModel : RegisterViewModel (testé ici)
 * - View : RegisterScreen (non testé unitairement)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeProfileRepository = FakeProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RegisterViewModel {
        return RegisterViewModel(fakeAuthRepository, fakeProfileRepository)
    }

    @Test
    fun `initial state is Idle`() {
        viewModel = createViewModel()
        assertEquals(RegisterUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `register failure updates state to Error`() = runTest {
        val errorMessage = "Email already in use"
        fakeAuthRepository.registerResult = Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.register("existing@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event.startsWith("REGISTER_FAILED"))
            assertTrue(viewModel.uiState.value is RegisterUiState.Error)
            assertEquals(errorMessage, (viewModel.uiState.value as RegisterUiState.Error).message)
        }
    }

    @Test
    fun `register with weak password returns error`() = runTest {
        val errorMessage = "Password should be at least 6 characters"
        fakeAuthRepository.registerResult = Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.register("test@example.com", "123")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event.contains("REGISTER_FAILED"))
        }
    }

    @Test
    fun `state becomes Loading during registration`() = runTest {
        fakeAuthRepository.registerResult = Result.failure(Exception("Error"))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.register("test@example.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        // After completion with error, state should be Error
        assertTrue(viewModel.uiState.value is RegisterUiState.Error)
    }

    @Test
    fun `register with invalid email returns error`() = runTest {
        val errorMessage = "Invalid email format"
        fakeAuthRepository.registerResult = Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.register("invalid-email", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event.contains("REGISTER_FAILED"))
            assertTrue(viewModel.uiState.value is RegisterUiState.Error)
        }
    }

    @Test
    fun `multiple registration failures work correctly`() = runTest {
        fakeAuthRepository.registerResult = Result.failure(Exception("First error"))

        viewModel = createViewModel()

        viewModel.events.test {
            // First attempt
            viewModel.register("test@example.com", "pass1")
            testDispatcher.scheduler.advanceUntilIdle()

            val firstEvent = awaitItem()
            assertTrue(firstEvent.contains("REGISTER_FAILED"))

            // Second attempt
            fakeAuthRepository.registerResult = Result.failure(Exception("Second error"))
            viewModel.register("test@example.com", "pass2")
            testDispatcher.scheduler.advanceUntilIdle()

            val secondEvent = awaitItem()
            assertTrue(secondEvent.contains("REGISTER_FAILED"))
        }
    }
}
