package com.example.ascendlifequest.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _events.emit("SIGNED_OUT")
            } catch (e: Exception) {
                _events.emit("SIGNOUT_FAILED: ${e.localizedMessage ?: "Erreur"}")
            }
        }
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()
}

