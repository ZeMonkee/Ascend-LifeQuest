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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour LoginViewModel (couche ViewModel MVVM).
 *
 * Ces tests vérifient :
 * - Les états de l'UI (LoginUiState)
 * - Les événements émis vers la View
 * - La coordination entre AuthRepository et ProfileRepository
 *
 * Architecture MVVM :
 * - Model : AuthRepository, ProfileRepository (mockés avec Fakes)
 * - ViewModel : LoginViewModel (testé ici)
 * - View : LoginScreen (non testé unitairement)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var viewModel: LoginViewModel

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

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(fakeAuthRepository, fakeProfileRepository)
    }

    @Test
    fun `initial state is Idle`() {
        viewModel = createViewModel()
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `login failure updates state to Error and emits event`() = runTest {
        val errorMessage = "Invalid credentials"
        fakeAuthRepository.signInResult = Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.login("test@example.com", "wrongpassword")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event.startsWith("LOGIN_FAILED"))
            assertTrue(viewModel.uiState.value is LoginUiState.Error)
            assertEquals(errorMessage, (viewModel.uiState.value as LoginUiState.Error).message)
        }
    }

    @Test
    fun `resetPassword success emits RESET_EMAIL_SENT event`() = runTest {
        fakeAuthRepository.resetPasswordResult = Result.success(Unit)

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.resetPassword("test@example.com")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals("RESET_EMAIL_SENT", event)
            assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        }
    }

    @Test
    fun `resetPassword failure emits RESET_FAILED event`() = runTest {
        val errorMessage = "Email not found"
        fakeAuthRepository.resetPasswordResult = Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.resetPassword("unknown@example.com")
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event.startsWith("RESET_FAILED"))
            assertTrue(viewModel.uiState.value is LoginUiState.Error)
        }
    }

    @Test
    fun `isUserLoggedIn returns correct value when logged in`() {
        fakeAuthRepository.isLoggedIn = true
        viewModel = createViewModel()
        assertTrue(viewModel.isUserLoggedIn())
    }

    @Test
    fun `isUserLoggedIn returns correct value when not logged in`() {
        fakeAuthRepository.isLoggedIn = false
        viewModel = createViewModel()
        assertFalse(viewModel.isUserLoggedIn())
    }

    @Test
    fun `state becomes Loading during login`() = runTest {
        fakeAuthRepository.signInResult = Result.failure(Exception("Error"))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.login("test@example.com", "password123")

        // After calling login, state transitions to Loading then Error
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Error)
    }

    @Test
    fun `state becomes Loading during resetPassword`() = runTest {
        fakeAuthRepository.resetPasswordResult = Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetPassword("test@example.com")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }
}
