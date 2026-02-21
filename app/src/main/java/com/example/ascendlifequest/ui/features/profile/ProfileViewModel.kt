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

/**
 * ViewModel managing user profile loading, creation, and rank retrieval.
 *
 * @property authRepository Repository for authentication state
 * @property profileRepository Repository for profile operations
 */
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

    // Flag pour éviter les rechargements inutiles
    private var _dataLoaded = false
    private var _loadedUserId: String? = null

    init {
        // Ne pas charger automatiquement, laisser le LaunchedEffect le faire
    }

    /** Loads profile for a specific user or the current logged-in user. */
    fun loadProfile(targetUserId: String? = null, forceReload: Boolean = false) {
        viewModelScope.launch {
            val userIdToLoad = targetUserId ?: authRepository.getCurrentUserId()

            // Si les données sont déjà chargées pour cet utilisateur et pas de force reload, on ne fait rien
            if (_dataLoaded && !forceReload && _loadedUserId == userIdToLoad && _uiState.value is ProfileUiState.Success) {
                Log.d(TAG, "Données déjà chargées pour $userIdToLoad, pas de rechargement")
                return@launch
            }

            // Ne mettre en Loading que si on n'a pas déjà des données
            if (_uiState.value !is ProfileUiState.Success) {
                _uiState.value = ProfileUiState.Loading
            }

            try {
                val currentUserId = authRepository.getCurrentUserId()

                // Si pas de userId, essayer de récupérer le profil en cache (mode hors ligne)
                if (userIdToLoad.isEmpty()) {
                    Log.d(TAG, "Mode hors ligne détecté - tentative de chargement depuis le cache")
                    val cachedResult = profileRepository.getCurrentUserProfile()
                    cachedResult.fold(
                        onSuccess = { profile ->
                            if (profile != null) {
                                _uiState.value = ProfileUiState.Success(profile, profile.rang)
                                _dataLoaded = true
                                _loadedUserId = profile.id
                                Log.d(TAG, "Profil chargé depuis le cache: ${profile.pseudo}")
                            } else {
                                _uiState.value = ProfileUiState.NotLoggedIn
                            }
                        },
                        onFailure = {
                            _uiState.value = ProfileUiState.NotLoggedIn
                        }
                    )
                    return@launch
                }

                // Récupérer le profil (le repository gère le cache automatiquement)
                val profileResult = profileRepository.getProfileById(userIdToLoad)

                profileResult.fold(
                        onSuccess = { profile ->
                            if (profile != null) {
                                val rankResult = profileRepository.getUserRank(userIdToLoad)
                                val rank = rankResult.getOrNull() ?: profile.rang

                                _uiState.value = ProfileUiState.Success(profile, rank)
                                _dataLoaded = true
                                _loadedUserId = userIdToLoad
                                Log.d(TAG, "Profil chargé avec succès: ${profile.pseudo}")
                            } else {
                                // Si c'est l'utilisateur actuel et qu'il n'a pas de profil
                                if (userIdToLoad == currentUserId && authRepository.isUserLoggedIn()) {
                                    val user = authRepository.getCurrentUser()
                                    val email = user?.email ?: "user@unknown.com"

                                    val createResult =
                                            profileRepository.createProfileForNewUser(
                                                    currentUserId,
                                                    email
                                            )
                                    createResult.fold(
                                            onSuccess = { newProfile ->
                                                _uiState.value = ProfileUiState.Success(newProfile, 1)
                                                _dataLoaded = true
                                                _loadedUserId = currentUserId
                                                Log.d(TAG, "Nouveau profil créé: ${newProfile.pseudo}")
                                            },
                                            onFailure = { error ->
                                                _uiState.value = ProfileUiState.Error(
                                                    error.message ?: "Erreur lors de la création du profil"
                                                )
                                            }
                                    )
                                } else {
                                    _uiState.value = ProfileUiState.Error("Profil utilisateur introuvable")
                                }
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
                _uiState.value = ProfileUiState.Error(e.message ?: "Erreur inattendue")
            }
        }
    }
}
