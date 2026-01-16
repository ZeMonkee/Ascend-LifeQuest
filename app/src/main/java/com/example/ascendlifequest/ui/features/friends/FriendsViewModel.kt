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
    data class Success(val friends: List<UserProfile>) : FriendsUiState()
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

    private val _isAddingFriend = MutableStateFlow(false)
    val isAddingFriend: StateFlow<Boolean> = _isAddingFriend.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private var searchJob: Job? = null

    init {
        _currentUserId.value = authRepository.getCurrentUserId()
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = FriendsUiState.Loading

            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                _uiState.value = FriendsUiState.Error("Utilisateur non connecté")
                return@launch
            }

            _currentUserId.value = userId

            val result = friendRepository.getFriends(userId)
            result.fold(
                onSuccess = { friends ->
                    _uiState.value = FriendsUiState.Success(friends)
                    Log.d(TAG, "Amis chargés: ${friends.size}")
                },
                onFailure = { error ->
                    _uiState.value = FriendsUiState.Error(error.message ?: "Erreur inconnue")
                    Log.e(TAG, "Erreur chargement amis", error)
                }
            )
        }
    }

    fun openAddFriendDialog() {
        _showAddFriendDialog.value = true
        _searchQuery.value = ""
        _searchState.value = SearchUiState.Idle
    }

    fun closeAddFriendDialog() {
        _showAddFriendDialog.value = false
        _searchQuery.value = ""
        _searchState.value = SearchUiState.Idle
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

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

    fun addFriend(friend: UserProfile) {
        viewModelScope.launch {
            _isAddingFriend.value = true

            val userId = _currentUserId.value
            if (userId.isEmpty()) {
                _isAddingFriend.value = false
                return@launch
            }

            val result = friendRepository.addFriend(userId, friend.uid)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Ami ajouté: ${friend.pseudo}")
                    closeAddFriendDialog()
                    loadFriends()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur ajout ami", error)
                }
            )

            _isAddingFriend.value = false
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
                    loadFriends()
                },
                onFailure = { error ->
                    Log.e(TAG, "Erreur suppression ami", error)
                }
            )
        }
    }
}

