package com.example.ascendlifequest.ui.features.leaderboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.FriendRepository
import com.example.ascendlifequest.data.repository.FriendRepositoryImpl
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.ProfileRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    data class Success(val leaderboard: List<UserProfile>, val currentUserRank: Int = 0) :
            LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}

enum class LeaderboardFilter {
    ALL,
    FRIENDS
}

/**
 * ViewModel for leaderboard display with global and friends-only filtering. Ranks users by XP and
 * provides the current user's position.
 *
 * @property profileRepository Repository for profile and leaderboard data
 * @property authRepository Repository for authentication state
 * @property friendRepository Repository for friend list retrieval
 */
class LeaderboardViewModel(
        private val profileRepository: ProfileRepository =
                ProfileRepositoryImpl(AuthRepositoryImpl(AuthService())),
        private val authRepository: AuthRepository = AuthRepositoryImpl(AuthService()),
        private val friendRepository: FriendRepository = FriendRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val TAG = "LeaderboardDesc"
    }

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val _filterMode = MutableStateFlow(LeaderboardFilter.ALL)
    val filterMode: StateFlow<LeaderboardFilter> = _filterMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)

    // Flag pour éviter les rechargements inutiles
    private var _dataLoaded = false
    private var _lastFilterMode: LeaderboardFilter? = null

    init {
        // Ne pas charger automatiquement, laisser le LaunchedEffect le faire
    }

    fun setFilterMode(mode: LeaderboardFilter) {
        if (_filterMode.value != mode) {
            _filterMode.value = mode
            loadLeaderboard(forceReload = true)
        }
    }

    fun loadLeaderboard(forceReload: Boolean = false) {
        viewModelScope.launch {
            // Si les données sont déjà chargées pour ce filtre et pas de force reload, on ne fait rien
            if (_dataLoaded && !forceReload && _lastFilterMode == _filterMode.value && _uiState.value is LeaderboardUiState.Success) {
                Log.d(TAG, "Données déjà chargées pour ${_filterMode.value}, pas de rechargement")
                return@launch
            }

            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true

            // Ne mettre en Loading que si on n'a pas déjà des données
            if (_uiState.value !is LeaderboardUiState.Success) {
                _uiState.value = LeaderboardUiState.Loading
            }

            val userId = authRepository.getCurrentUserId()

            try {
                Log.d(TAG, "Chargement du classement (Mode: ${_filterMode.value})...")

                if (_filterMode.value == LeaderboardFilter.ALL) {
                    // Mode Global: Charger depuis ProfileRepository
                    val result = profileRepository.getLeaderboard(100)
                    result.fold(
                            onSuccess = { users ->
                                // Find current user rank
                                val currentUser = users.find { it.uid == userId }
                                val rank = currentUser?.rang ?: 0
                                _uiState.value = LeaderboardUiState.Success(users, rank)
                                _dataLoaded = true
                                _lastFilterMode = _filterMode.value
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Erreur chargement classement global", error)
                                _uiState.value =
                                        LeaderboardUiState.Error(error.message ?: "Erreur inconnue")
                            }
                    )
                } else {
                    // Mode Amis: Charger les amis + self, trier par XP
                    if (userId.isEmpty()) {
                        _uiState.value = LeaderboardUiState.Error("Utilisateur non connecté")
                        return@launch
                    }

                    val friendsResult = friendRepository.getFriends(userId)
                    val currentUserResult = profileRepository.getProfileById(userId)

                    val friends = friendsResult.getOrNull() ?: emptyList()
                    val currentUser = currentUserResult.getOrNull()

                    val allUsers =
                            (friends + (currentUser ?: return@launch))
                                    .distinctBy {
                                        it.uid
                                    } // Avoid duplicates if self is in friends list (unlikely but
                                    // safe)
                                    .sortedByDescending { it.xp }

                    // Assign local ranks
                    allUsers.forEachIndexed { index, user -> user.rang = index + 1 }

                    val myRank = allUsers.find { it.uid == userId }?.rang ?: 0

                    _uiState.value = LeaderboardUiState.Success(allUsers, myRank)
                    _dataLoaded = true
                    _lastFilterMode = _filterMode.value
                    Log.d(TAG, "Classement amis chargé: ${allUsers.size} utilisateurs")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception chargement classement", e)
                _uiState.value = LeaderboardUiState.Error(e.message ?: "Erreur inconnue")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
