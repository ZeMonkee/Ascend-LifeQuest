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

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: FirebaseUser) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(authRepository)
) : ViewModel() {

    companion object {
        private const val TAG = "RegisterViewModel"
    }

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

                // Créer le profil dans Firestore
                try {
                    val profileResult = profileRepository.createProfileForNewUser(user.uid, email)
                    if (profileResult.isSuccess) {
                        Log.d(TAG, "Profil créé avec succès pour: ${user.uid}")
                    } else {
                        Log.e(TAG, "Erreur lors de la création du profil: ${profileResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception lors de la création du profil", e)
                }

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

