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
        private val profileRepository: ProfileRepository =
                ProfileRepositoryImpl(AuthRepositoryImpl(AuthService()))
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    init {
        loadProfile()
    }

    /** Charge le profil de l'utilisateur connecté */
    /**
     * Charge le profil d'un utilisateur
     */
    fun loadProfile(targetUserId: String? = null) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            try {
                // Vérifier si l'utilisateur est connecté
                if (!authRepository.isUserLoggedIn()) {
                    _uiState.value = ProfileUiState.NotLoggedIn
                    return@launch
                }

                val currentUserId = authRepository.getCurrentUserId()
                val userIdToLoad = targetUserId ?: currentUserId

                if (userIdToLoad.isEmpty()) {
                    _uiState.value = ProfileUiState.NotLoggedIn
                    return@launch
                }

                // Récupérer le profil
                val profileResult = profileRepository.getProfileById(userIdToLoad)

                profileResult.fold(
                        onSuccess = { profile ->
                            if (profile != null) {
                                // Récupérer le rang
                                val rankResult = profileRepository.getUserRank(userIdToLoad)
                                val rank = rankResult.getOrNull() ?: profile.rang

                                _uiState.value = ProfileUiState.Success(profile, rank)
                                Log.d(TAG, "Profil chargé avec succès: ${profile.pseudo}")
                            } else {
                                // Si c'est l'utilisateur actuel et qu'il n'a pas de profil, on en
                                // crée un
                                if (userIdToLoad == currentUserId) {
                                    val user = authRepository.getCurrentUser()
                                    val email = user?.email ?: "user@unknown.com"

                                    val createResult =
                                            profileRepository.createProfileForNewUser(
                                                    currentUserId,
                                                    email
                                            )
                                    createResult.fold(
                                            onSuccess = { newProfile ->
                                                _uiState.value =
                                                        ProfileUiState.Success(newProfile, 1)
                                                Log.d(
                                                        TAG,
                                                        "Nouveau profil créé: ${newProfile.pseudo}"
                                                )
                                            },
                                            onFailure = { error ->
                                                _uiState.value =
                                                        ProfileUiState.Error(
                                                                error.message
                                                                        ?: "Erreur lors de la création du profil"
                                                        )
                                            }
                                    )
                                } else {
                                    _uiState.value =
                                            ProfileUiState.Error("Profil utilisateur introuvable")
                                }
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Erreur lors du chargement du profil", error)
                            _uiState.value =
                                    ProfileUiState.Error(
                                            error.message ?: "Erreur lors du chargement du profil"
                                    )
                        }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception lors du chargement du profil", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "Erreur inattendue")
            }
        }
    }

}
