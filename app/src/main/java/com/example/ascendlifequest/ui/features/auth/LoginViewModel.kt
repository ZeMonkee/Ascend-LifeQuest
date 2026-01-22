package com.example.ascendlifequest.ui.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.ProfileRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: FirebaseUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel handling user login with email/password and profile verification.
 *
 * @property authRepository Repository for authentication operations
 * @property profileRepository Repository for profile operations
 */
class LoginViewModel(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository = ProfileRepositoryImpl(authRepository)
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInWithEmailPassword(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!

                // Vérifier/créer le profil si nécessaire
                ensureProfileExists(user)

                _uiState.value = LoginUiState.Success(user)
                _events.emit("LOGIN_SUCCESS")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erreur de connexion"
                _uiState.value = LoginUiState.Error(err)
                _events.emit("LOGIN_FAILED: $err")
            }
        }
    }

    /** S'assure que le profil existe dans Firestore, le crée si nécessaire */
    private suspend fun ensureProfileExists(user: FirebaseUser) {
        try {
            val existingProfile = profileRepository.getProfileById(user.uid).getOrNull()
            if (existingProfile == null) {
                val email = user.email ?: user.displayName ?: "user@unknown.com"
                profileRepository.createProfileForNewUser(user.uid, email)
                Log.d(TAG, "Profil créé pour utilisateur existant: ${user.uid}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification/création du profil", e)
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Idle
                _events.emit("RESET_EMAIL_SENT")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erreur envoi email"
                _uiState.value = LoginUiState.Error(err)
                _events.emit("RESET_FAILED: $err")
            }
        }
    }

    /** Checks if a user is currently logged in. */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
