package com.example.ascendlifequest.ui.features.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.FriendRepository
import com.example.ascendlifequest.data.repository.FriendRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FriendsUiState {
    object Loading : FriendsUiState()
    data class Success(
        val friends: List<UserProfile>,
        val pendingRequests: List<UserProfile>
    ) : FriendsUiState()
    data class Error(val message: String) : FriendsUiState()
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val users: List<UserProfile>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class FriendsViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(AuthService()),
    private val friendRepository: FriendRepository = FriendRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val TAG = "FriendsViewModel"
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow<FriendsUiState>(FriendsUiState.Loading)
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddFriendDialog = MutableStateFlow(false)
    val showAddFriendDialog: StateFlow<Boolean> = _showAddFriendDialog.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow<UserProfile?>(null)
    val showDeleteConfirmDialog: StateFlow<UserProfile?> = _showDeleteConfirmDialog.asStateFlow()

    private val _showPendingRequestsDialog = MutableStateFlow(false)
    val showPendingRequestsDialog: StateFlow<Boolean> = _showPendingRequestsDialog.asStateFlow()

    private val _pendingRequestsCount = MutableStateFlow(0)
    val pendingRequestsCount: StateFlow<Int> = _pendingRequestsCount.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isAddingFriend = MutableStateFlow(false)
    val isAddingFriend: StateFlow<Boolean> = _isAddingFriend.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _requestSentMessage = MutableStateFlow<String?>(null)
    val requestSentMessage: StateFlow<String?> = _requestSentMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        _currentUserId.value = authRepository.getCurrentUserId()
        // Ne pas charger automatiquement, laisser le LaunchedEffect le faire
    }

    fun loadFriendsAndRequests() {
        viewModelScope.launch {
            Log.d(TAG, "=== Début loadFriendsAndRequests ===")

            // Si déjà en cours de chargement, on ne fait rien
            if (_isRefreshing.value) {
                Log.d(TAG, "Déjà en cours de chargement, ignoré")
                return@launch
            }

            _isRefreshing.value = true

            // Ne mettre en Loading que si on n'a pas déjà des données
            val currentState = _uiState.value
            if (currentState !is FriendsUiState.Success) {
                _uiState.value = FriendsUiState.Loading
            }

            val userId = authRepository.getCurrentUserId()
            Log.d(TAG, "UserId actuel: $userId")

            if (userId.isEmpty()) {
                _uiState.value = FriendsUiState.Error("Utilisateur non connecté")
                _isRefreshing.value = false
                return@launch
            }

            _currentUserId.value = userId

            try {
                // Charger les amis
                Log.d(TAG, "Chargement des amis...")
                val friendsResult = friendRepository.getFriends(userId)
                val friends = friendsResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Amis chargés: ${friends.size}")

                // Charger les demandes en attente
                Log.d(TAG, "Chargement des demandes en attente...")
                val pendingResult = friendRepository.getPendingFriendRequests(userId)
                val pendingRequests = pendingResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Demandes en attente chargées: ${pendingRequests.size}")

                // Mettre à jour le compteur de demandes
                _pendingRequestsCount.value = pendingRequests.size
                Log.d(TAG, "Compteur mis à jour: ${_pendingRequestsCount.value}")

                _uiState.value = FriendsUiState.Success(
                    friends = friends,
                    pendingRequests = pendingRequests
                )
                Log.d(TAG, "✅ État mis à jour avec ${friends.size} amis et ${pendingRequests.size} demandes")
            } catch (e: Exception) {
                _uiState.value = FriendsUiState.Error(e.message ?: "Erreur inconnue")
                Log.e(TAG, "❌ Erreur chargement", e)
            } finally {
                _isRefreshing.value = false
                Log.d(TAG, "=== Fin loadFriendsAndRequests ===")
            }
        }
    }

    fun openAddFriendDialog() {
        _showAddFriendDialog.value = true
        _searchQuery.value = ""
        _searchState.value = SearchUiState.Idle
        _requestSentMessage.value = null
    }

    fun closeAddFriendDialog() {
        _showAddFriendDialog.value = false
        _searchQuery.value = ""
        _searchState.value = SearchUiState.Idle
        _requestSentMessage.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _requestSentMessage.value = null

        // Debounce la recherche
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchUsers(query)
        }
    }

    private suspend fun searchUsers(query: String) {
        _searchState.value = SearchUiState.Loading

        val userId = _currentUserId.value
        if (userId.isEmpty()) {
            _searchState.value = SearchUiState.Error("Utilisateur non connecté")
            return
        }

        val result = friendRepository.searchUsersByPseudo(query, userId)
        result.fold(
            onSuccess = { users ->
                _searchState.value = SearchUiState.Success(users)
                Log.d(TAG, "Recherche '$query': ${users.size} résultats")
            },
            onFailure = { error ->
                _searchState.value = SearchUiState.Error(error.message ?: "Erreur de recherche")
                Log.e(TAG, "Erreur recherche", error)
            }
        )
    }

    /**
     * Envoie une demande d'ami (au lieu d'ajouter directement)
     */
    fun sendFriendRequest(friend: UserProfile) {
        viewModelScope.launch {
            _isAddingFriend.value = true

            val userId = _currentUserId.value
            if (userId.isEmpty()) {
                _isAddingFriend.value = false
                return@launch
            }

            val result = friendRepository.sendFriendRequest(userId, friend.uid)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Demande d'ami envoyée à: ${friend.pseudo}")
                    _requestSentMessage.value = "Demande envoyée à ${friend.pseudo} !"
                    // Ne pas fermer le dialogue pour permettre d'ajouter d'autres amis
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur envoi demande", error)
                    _requestSentMessage.value = error.message ?: "Erreur lors de l'envoi"
                }
            )

            _isAddingFriend.value = false
        }
    }

    /**
     * Accepte une demande d'ami
     */
    fun acceptFriendRequest(friend: UserProfile) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            val result = friendRepository.acceptFriendRequest(userId, friend.uid)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Demande acceptée de: ${friend.pseudo}")
                    loadFriendsAndRequests()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur acceptation demande", error)
                }
            )
        }
    }

    /**
     * Refuse une demande d'ami
     */
    fun declineFriendRequest(friend: UserProfile) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) return@launch

            val result = friendRepository.declineFriendRequest(userId, friend.uid)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Demande refusée de: ${friend.pseudo}")
                    loadFriendsAndRequests()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur refus demande", error)
                }
            )
        }
    }

    fun showDeleteConfirmation(friend: UserProfile) {
        _showDeleteConfirmDialog.value = friend
    }

    fun hideDeleteConfirmation() {
        _showDeleteConfirmDialog.value = null
    }

    fun removeFriend(friend: UserProfile) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId.isEmpty()) {
                return@launch
            }

            val result = friendRepository.removeFriend(userId, friend.uid)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Ami supprimé: ${friend.pseudo}")
                    hideDeleteConfirmation()
                    loadFriendsAndRequests()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur suppression ami", error)
                }
            )
        }
    }

    fun clearRequestSentMessage() {
        _requestSentMessage.value = null
    }

    fun openPendingRequestsDialog() {
        // Recharger les données avant d'ouvrir le dialogue
        viewModelScope.launch {
            Log.d(TAG, "Ouverture du dialogue des demandes - rechargement des données")

            val userId = _currentUserId.value
            if (userId.isNotEmpty()) {
                val pendingResult = friendRepository.getPendingFriendRequests(userId)
                val pendingRequests = pendingResult.getOrNull() ?: emptyList()

                _pendingRequestsCount.value = pendingRequests.size

                // Mettre à jour l'état avec les nouvelles demandes
                val currentState = _uiState.value
                if (currentState is FriendsUiState.Success) {
                    _uiState.value = currentState.copy(pendingRequests = pendingRequests)
                }

                Log.d(TAG, "Demandes rechargées: ${pendingRequests.size}")
            }

            _showPendingRequestsDialog.value = true
        }
    }

    fun closePendingRequestsDialog() {
        _showPendingRequestsDialog.value = false
    }
}

