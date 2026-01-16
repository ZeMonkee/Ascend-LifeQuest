package com.example.ascendlifequest.ui.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile, val rank: Int) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object NotLoggedIn : ProfileUiState()
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(AuthService()),
    private val profileRepository: ProfileRepository = ProfileRepositoryImpl(AuthRepositoryImpl(AuthService()))
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Charge le profil de l'utilisateur connecté
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            try {
                // Vérifier si l'utilisateur est connecté
                if (!authRepository.isUserLoggedIn()) {
                    _uiState.value = ProfileUiState.NotLoggedIn
                    return@launch
                }

                val userId = authRepository.getCurrentUserId()
                if (userId.isEmpty()) {
                    _uiState.value = ProfileUiState.NotLoggedIn
                    return@launch
                }

                // Récupérer le profil
                val profileResult = profileRepository.getCurrentUserProfile()

                profileResult.fold(
                    onSuccess = { profile ->
                        if (profile != null) {
                            // Récupérer le rang
                            val rankResult = profileRepository.getUserRank(userId)
                            val rank = rankResult.getOrNull() ?: profile.rang

                            _uiState.value = ProfileUiState.Success(profile, rank)
                            Log.d(TAG, "Profil chargé avec succès: ${profile.pseudo}")
                        } else {
                            // Créer un nouveau profil si non existant
                            val user = authRepository.getCurrentUser()
                            val email = user?.email ?: "user@unknown.com"

                            val createResult = profileRepository.createProfileForNewUser(userId, email)
                            createResult.fold(
                                onSuccess = { newProfile ->
                                    _uiState.value = ProfileUiState.Success(newProfile, 1)
                                    Log.d(TAG, "Nouveau profil créé: ${newProfile.pseudo}")
                                },
                                onFailure = { error ->
                                    _uiState.value = ProfileUiState.Error(
                                        error.message ?: "Erreur lors de la création du profil"
                                    )
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Erreur lors du chargement du profil", error)
                        _uiState.value = ProfileUiState.Error(
                            error.message ?: "Erreur lors du chargement du profil"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception lors du chargement du profil", e)
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Erreur inattendue"
                )
            }
        }
    }

    /**
     * Rafraîchit le profil (pull-to-refresh)
     */
    fun refreshProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadProfile()
            _isRefreshing.value = false
        }
    }

    /**
     * Met à jour le pseudo de l'utilisateur
     */
    fun updatePseudo(newPseudo: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                _uiState.value = ProfileUiState.Error("Utilisateur non connecté")
                return@launch
            }

            val result = profileRepository.updatePseudo(userId, newPseudo)
            result.fold(
                onSuccess = {
                    // Recharger le profil après mise à jour
                    loadProfile()
                },
                onFailure = { error ->
                    _uiState.value = ProfileUiState.Error(
                        error.message ?: "Erreur lors de la mise à jour du pseudo"
                    )
                }
            )
        }
    }

    /**
     * Ajoute de l'XP à l'utilisateur (appelé après complétion de quête)
     */
    fun addXp(xpAmount: Long) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) return@launch

            profileRepository.updateXp(userId, xpAmount)
            loadProfile() // Recharger pour afficher les nouvelles données
        }
    }

    /**
     * Incrémente le nombre de quêtes réalisées
     */
    fun incrementQuestsCompleted() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) return@launch

            profileRepository.incrementQuestsCompleted(userId)
            loadProfile()
        }
    }
}

