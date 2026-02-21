package com.example.ascendlifequest.ui.features.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.Notification
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.FriendRepository
import com.example.ascendlifequest.data.repository.FriendRepositoryImpl
import com.example.ascendlifequest.di.AppContextProvider
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
            val pendingRequests: List<UserProfile>,
            val notifications: List<Notification> = emptyList()
    ) : FriendsUiState()
    data class Error(val message: String) : FriendsUiState()
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val users: List<UserProfile>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

/**
 * ViewModel managing friend list, friend requests, and user search. Handles friend operations
 * including adding, removing, and request management.
 *
 * @property authRepository Repository for authentication state
 * @property friendRepository Repository for friend operations
 */
class FriendsViewModel(
        private val authRepository: AuthRepository = AuthRepositoryImpl(AuthService()),
        private val friendRepository: FriendRepository = FriendRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val TAG = "FriendsViewModel"
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    // Vérification de la connectivité réseau
    private val networkManager by lazy {
        AppContextProvider.getContextOrNull()?.let { NetworkConnectivityManager.getInstance(it) }
    }

    private fun isOnline(): Boolean = networkManager?.checkCurrentConnectivity() ?: true

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

    private val _notificationsCount = MutableStateFlow(0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Flag pour éviter les rechargements inutiles
    private var _dataLoaded = false

    // ID de l'utilisateur en cours d'ajout (null si aucun)
    private val _addingFriendId = MutableStateFlow<String?>(null)
    val addingFriendId: StateFlow<String?> = _addingFriendId.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUserPseudo = MutableStateFlow("")

    private val _requestSentMessage = MutableStateFlow<String?>(null)
    val requestSentMessage: StateFlow<String?> = _requestSentMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        _currentUserId.value = authRepository.getCurrentUserId()
        // Ne pas charger automatiquement, laisser le LaunchedEffect le faire
    }

    fun loadFriendsAndRequests(forceReload: Boolean = false) {
        viewModelScope.launch {
            Log.d(TAG, "Début loadFriendsAndRequests (forceReload=$forceReload, dataLoaded=$_dataLoaded)")

            // Si les données sont déjà chargées et pas de force reload, on ne fait rien
            if (_dataLoaded && !forceReload && _uiState.value is FriendsUiState.Success) {
                Log.d(TAG, "Données déjà chargées, pas de rechargement")
                return@launch
            }

            // Si déjà en cours de chargement, on ne fait rien
            if (_isRefreshing.value) {
                Log.d(TAG, "Déjà en cours de chargement, ignoré")
                return@launch
            }

            try {
                _isRefreshing.value = true

                // Ne mettre en Loading que si on n'a pas déjà des données
                val currentState = _uiState.value
                if (currentState !is FriendsUiState.Success) {
                    _uiState.value = FriendsUiState.Loading
                }

                val online = isOnline()
                Log.d(TAG, "Connectivité réseau: $online")

                // Récupérer le userId (peut être vide en mode hors ligne)
                var userId = authRepository.getCurrentUserId()
                Log.d(TAG, "UserId depuis auth: $userId")

                // Charger les amis - le repository gère le cache automatiquement
                Log.d(TAG, "Chargement des amis...")
                val friendsResult = if (userId.isNotEmpty()) {
                    friendRepository.getFriends(userId)
                } else {
                    // Si pas de userId, essayer le cache directement
                    friendRepository.getFriendsFromCache()
                }
                val friends = friendsResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Amis chargés: ${friends.size}")

                // Les demandes et notifications ne sont disponibles qu'en ligne
                var pendingRequests = emptyList<UserProfile>()
                var notifications = emptyList<Notification>()

                if (online && userId.isNotEmpty()) {
                    Log.d(TAG, "Chargement des demandes en attente...")
                    val pendingResult = friendRepository.getPendingFriendRequests(userId)
                    pendingRequests = pendingResult.getOrNull() ?: emptyList()
                    Log.d(TAG, "Demandes en attente: ${pendingRequests.size}")

                    Log.d(TAG, "Chargement des notifications...")
                    val notificationsResult = friendRepository.getNotifications(userId)
                    notifications = notificationsResult.getOrNull() ?: emptyList()
                    Log.d(TAG, "Notifications: ${notifications.size}")

                    // Charger le pseudo de l'utilisateur actuel
                    val profileResult = friendRepository.getProfileById(userId)
                    val profile = profileResult.getOrNull()
                    _currentUserPseudo.value = profile?.pseudo ?: ""
                }

                _currentUserId.value = userId

                // Mettre à jour les compteurs
                _pendingRequestsCount.value = pendingRequests.size + notifications.size
                _notificationsCount.value = notifications.size

                _uiState.value = FriendsUiState.Success(
                    friends = friends,
                    pendingRequests = pendingRequests,
                    notifications = notifications
                )

                _dataLoaded = true
                Log.d(TAG, "État mis à jour avec succès - ${friends.size} amis")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement: ${e.message}", e)
                // En cas d'erreur, essayer le cache
                try {
                    val cachedFriends = friendRepository.getFriendsFromCache().getOrNull() ?: emptyList()
                    Log.d(TAG, "Fallback cache: ${cachedFriends.size} amis")
                    _uiState.value = FriendsUiState.Success(
                        friends = cachedFriends,
                        pendingRequests = emptyList(),
                        notifications = emptyList()
                    )
                    _dataLoaded = true
                } catch (cacheError: Exception) {
                    Log.e(TAG, "Erreur cache aussi: ${cacheError.message}")
                    _uiState.value = FriendsUiState.Success(
                        friends = emptyList(),
                        pendingRequests = emptyList(),
                        notifications = emptyList()
                    )
                }
            } finally {
                _isRefreshing.value = false
                Log.d(TAG, "Fin loadFriendsAndRequests")
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

        searchJob =
                viewModelScope.launch {
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

    /** Envoie une demande d'ami (au lieu d'ajouter directement) */
    fun sendFriendRequest(friend: UserProfile) {
        viewModelScope.launch {
            _addingFriendId.value = friend.uid

            val userId = _currentUserId.value
            if (userId.isEmpty()) {
                _addingFriendId.value = null
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

            _addingFriendId.value = null
        }
    }

    /** Accepte une demande d'ami */
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
                    onFailure = { error -> Log.e(TAG, "Erreur acceptation demande", error) }
            )
        }
    }

    /** Refuse une demande d'ami */
    fun declineFriendRequest(friend: UserProfile) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            var userPseudo = _currentUserPseudo.value

            Log.d(
                    TAG,
                    "declineFriendRequest: userId=$userId, userPseudo=$userPseudo, friendUid=${friend.uid}"
            )

            if (userId.isEmpty()) {
                Log.e(TAG, "declineFriendRequest: userId est vide!")
                return@launch
            }

            // Si le pseudo est vide, on le charge depuis le profil
            if (userPseudo.isEmpty()) {
                Log.d(TAG, "declineFriendRequest: pseudo vide, chargement depuis le profil...")
                val profileResult = friendRepository.getProfileById(userId)
                userPseudo = profileResult.getOrNull()?.pseudo ?: "Utilisateur"
                _currentUserPseudo.value = userPseudo
                Log.d(TAG, "declineFriendRequest: pseudo chargé = $userPseudo")
            }

            val result = friendRepository.declineFriendRequest(userId, friend.uid, userPseudo)
            result.fold(
                    onSuccess = {
                        Log.d(
                                TAG,
                                "Demande refusee de: ${friend.pseudo} - Notification envoyee a ${friend.uid}"
                        )
                        loadFriendsAndRequests()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Erreur refus demande: ${error.message}", error)
                    }
            )
        }
    }

    /** Supprime une notification */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val result = friendRepository.deleteNotification(notificationId)
            result.fold(
                    onSuccess = {
                        Log.d(TAG, "Notification supprimée: $notificationId")
                        loadFriendsAndRequests()
                    },
                    onFailure = { error -> Log.e(TAG, "Erreur suppression notification", error) }
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
                    onFailure = { error -> Log.e(TAG, "Erreur suppression ami", error) }
            )
        }
    }

    fun openPendingRequestsDialog() {
        // Recharger les données avant d'ouvrir le dialogue
        viewModelScope.launch {
            Log.d(TAG, "Ouverture du dialogue des demandes - rechargement des données")

            val userId = _currentUserId.value
            if (userId.isNotEmpty()) {
                val pendingResult = friendRepository.getPendingFriendRequests(userId)
                val pendingRequests = pendingResult.getOrNull() ?: emptyList()

                val notificationsResult = friendRepository.getNotifications(userId)
                val notifications = notificationsResult.getOrNull() ?: emptyList()

                _pendingRequestsCount.value = pendingRequests.size + notifications.size
                _notificationsCount.value = notifications.size

                // Mettre à jour l'état avec les nouvelles demandes et notifications
                val currentState = _uiState.value
                if (currentState is FriendsUiState.Success) {
                    _uiState.value =
                            currentState.copy(
                                    pendingRequests = pendingRequests,
                                    notifications = notifications
                            )
                }

                Log.d(
                        TAG,
                        "Demandes rechargées: ${pendingRequests.size}, Notifications: ${notifications.size}"
                )
            }

            _showPendingRequestsDialog.value = true
        }
    }

    fun closePendingRequestsDialog() {
        _showPendingRequestsDialog.value = false
    }
}
