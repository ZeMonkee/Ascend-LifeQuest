package com.example.ascendlifequest.ui.features.offline

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.entities.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * État du mode hors ligne
 */
sealed class OfflineState {
    object Checking : OfflineState()
    object Online : OfflineState()
    data class Offline(val hasLocalData: Boolean, val lastUserPseudo: String?) : OfflineState()
}

/**
 * ViewModel pour gérer le mode hors ligne
 */
class OfflineModeViewModel(
    private val context: Context
) : ViewModel() {

    private val networkManager = NetworkConnectivityManager.getInstance(context)
    private val database = AppDatabase.getDatabase(context)

    private val _offlineState = MutableStateFlow<OfflineState>(OfflineState.Checking)
    val offlineState: StateFlow<OfflineState> = _offlineState.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var cachedUserProfile: UserProfileEntity? = null

    init {
        checkConnectivity()
        observeConnectivity()
    }

    private fun checkConnectivity() {
        viewModelScope.launch {
            val connected = networkManager.checkCurrentConnectivity()
            _isConnected.value = connected

            if (connected) {
                _offlineState.value = OfflineState.Online
            } else {
                checkLocalData()
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkManager.observeConnectivity().collect { connected ->
                _isConnected.value = connected
                if (connected) {
                    _offlineState.value = OfflineState.Online
                } else if (_offlineState.value is OfflineState.Online) {
                    checkLocalData()
                }
            }
        }
    }

    private suspend fun checkLocalData() {
        val userProfile = database.userProfileDao().getCurrentUserProfile()
        cachedUserProfile = userProfile

        val hasLocalData = userProfile != null
        val pseudo = userProfile?.pseudo

        _offlineState.value = OfflineState.Offline(
            hasLocalData = hasLocalData,
            lastUserPseudo = pseudo
        )
    }

    /**
     * Récupère l'ID de l'utilisateur en cache
     */
    fun getCachedUserId(): String? {
        return cachedUserProfile?.id
    }

    /**
     * Rafraîchit l'état de connectivité
     */
    fun refreshConnectivity() {
        checkConnectivity()
    }
}
