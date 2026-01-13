package com.example.ascendlifequest.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: FirebaseUser) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // One-shot events (navigation/messages)
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.signInWithEmailPassword(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.value = LoginUiState.Success(user)
                _events.emit("LOGIN_SUCCESS")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erreur de connexion"
                _uiState.value = LoginUiState.Error(err)
                _events.emit("LOGIN_FAILED: $err")
            }
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

    // Vérifier rapidement si un utilisateur est déjà connecté (utilisé par la View pour navigation initiale)
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}
