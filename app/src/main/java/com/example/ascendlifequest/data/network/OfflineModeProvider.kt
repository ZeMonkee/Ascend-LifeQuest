package com.example.ascendlifequest.data.network

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.StateFlow

/**
 * État global du mode hors ligne accessible dans toute l'application
 */
data class OfflineMode(
    val isOffline: Boolean = false,
    val hasLocalData: Boolean = false
)

/**
 * CompositionLocal pour accéder à l'état hors ligne dans tout l'arbre de composition
 */
val LocalOfflineMode = compositionLocalOf { OfflineMode() }

/**
 * Provider pour l'état hors ligne global
 */
@Composable
fun OfflineModeProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val networkManager = remember { NetworkConnectivityManager.getInstance(context) }

    // Observer l'état de connectivité
    val isConnected by networkManager.isConnected.collectAsState()

    // Créer l'état du mode hors ligne
    val offlineMode = remember(isConnected) {
        OfflineMode(
            isOffline = !isConnected,
            hasLocalData = true // On suppose qu'on a des données locales si l'utilisateur est connecté
        )
    }

    // Démarrer l'observation de la connectivité
    remember {
        networkManager.startObserving()
        networkManager
    }

    CompositionLocalProvider(LocalOfflineMode provides offlineMode) {
        content()
    }
}

/**
 * Fonction utilitaire pour accéder à l'état hors ligne
 */
@Composable
fun isOfflineMode(): Boolean {
    return LocalOfflineMode.current.isOffline
}
