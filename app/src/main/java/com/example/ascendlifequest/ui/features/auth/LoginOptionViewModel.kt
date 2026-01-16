package com.example.ascendlifequest.ui.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.ProfileRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

sealed class LoginOptionUiState {
    object Idle : LoginOptionUiState()
    object Loading : LoginOptionUiState()
    data class Success(val user: FirebaseUser) : LoginOptionUiState()
    data class Error(val message: String) : LoginOptionUiState()
}

class LoginOptionViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(authRepository)
) : ViewModel() {

    companion object {
        private const val TAG = "LoginOptionViewModel"
    }

    private val _uiState = MutableStateFlow<LoginOptionUiState>(LoginOptionUiState.Idle)
    val uiState: StateFlow<LoginOptionUiState> = _uiState

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun handleGoogleSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            _uiState.value = LoginOptionUiState.Loading
            val result = authRepository.handleGoogleSignInResult(data)
            if (result.isSuccess) {
                val user = result.getOrNull()!!

                // Vérifier/créer le profil si nécessaire
                ensureProfileExists(user)

                _uiState.value = LoginOptionUiState.Success(user)
                _events.emit("GOOGLE_LOGIN_SUCCESS")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erreur Google Sign-In"
                _uiState.value = LoginOptionUiState.Error(err)
                _events.emit("GOOGLE_LOGIN_FAILED: $err")
            }
        }
    }

    /**
     * S'assure que le profil existe dans Firestore, le crée si nécessaire
     */
    private suspend fun ensureProfileExists(user: FirebaseUser) {
        try {
            val existingProfile = profileRepository.getProfileById(user.uid).getOrNull()
            if (existingProfile == null) {
                val email = user.email ?: user.displayName ?: "user@unknown.com"
                profileRepository.createProfileForNewUser(user.uid, email)
                Log.d(TAG, "Profil créé pour utilisateur Google: ${user.uid}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification/création du profil", e)
        }
    }

    // Exposer un helper permettant de vérifier si l'utilisateur est connecté (utilisé par la View)
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
