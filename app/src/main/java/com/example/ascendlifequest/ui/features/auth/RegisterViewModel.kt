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

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: FirebaseUser) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = authRepository.registerWithEmailPassword(email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _uiState.value = RegisterUiState.Success(user)
                _events.emit("REGISTER_SUCCESS")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Erreur inscription"
                _uiState.value = RegisterUiState.Error(err)
                _events.emit("REGISTER_FAILED: $err")
            }
        }
    }
}

