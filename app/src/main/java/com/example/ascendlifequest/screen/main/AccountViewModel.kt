package com.example.ascendlifequest.screen.main

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class AccountUiState {
    object Idle : AccountUiState()
    object Loading : AccountUiState()
    data class Success(val message: String) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
    data class ReauthRequired(val action: String, val pendingValue: String) : AccountUiState()
    data class Loaded(val email: String?, val photoUrl: String?) : AccountUiState()
}

class AccountViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState

    fun loadCurrentUser() {
        val user = auth.currentUser
        _uiState.value = AccountUiState.Loaded(user?.email, user?.photoUrl?.toString())
    }

    fun updateEmail(newEmail: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            _uiState.value = AccountUiState.Error("Email invalide")
            return
        }
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AccountUiState.Error("Utilisateur non authentifié")
            return
        }

        // Vérifier que l'utilisateur a bien un provider Email/Password lié ; sinon l'update peut être impossible
        val hasPasswordProvider = user.providerData.any { it?.providerId == "password" }
        if (!hasPasswordProvider) {
            _uiState.value = AccountUiState.Error("Impossible de modifier l'e‑mail : votre compte est lié via un fournisseur externe (Google/Facebook). Pour changer l'email, liez d'abord une méthode Email/Password ou modifiez l'adresse depuis votre fournisseur.")
            return
        }

        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    _uiState.value = AccountUiState.Success(
                        "Un e-mail de vérification vient d’être envoyé à $newEmail.\n" +
                                "Clique sur le lien pour confirmer le changement."
                    )
                }
                .addOnFailureListener { ex ->
                    // Si re-auth nécessaire, propager l'erreur spécifique
                    if (ex is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                        _uiState.value = AccountUiState.ReauthRequired("email", newEmail)
                    } else if (ex is FirebaseAuthException) {
                        // Cas où l'opération est interdite côté projet (provider désactivé) ou autre erreur Firebase
                        val code = ex.errorCode
                        if (code.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) || ex.message?.contains("Please verify the new email", ignoreCase = true) == true) {
                            _uiState.value = AccountUiState.Error("Opération interdite : activez le provider Email/Password dans la console Firebase ou vérifiez le nouvel e‑mail. Détail: ${ex.message}")
                        } else {
                            _uiState.value = AccountUiState.Error(ex.message ?: "Erreur lors de la mise à jour de l'email")
                        }
                    } else {
                        _uiState.value = AccountUiState.Error(ex.message ?: "Erreur lors de la mise à jour de l'email")
                    }
                }
        }
    }

    fun updatePassword(newPassword: String) {
        if (newPassword.length < 6) {
            _uiState.value = AccountUiState.Error("Le mot de passe doit contenir au moins 6 caractères")
            return
        }
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AccountUiState.Error("Utilisateur non authentifié")
            return
        }

        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    _uiState.value = AccountUiState.Success("Mot de passe mis à jour")
                }
                .addOnFailureListener { ex ->
                    if (ex is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                        _uiState.value = AccountUiState.ReauthRequired("password", newPassword)
                    } else {
                        _uiState.value = AccountUiState.Error(ex.message ?: "Erreur lors de la mise à jour du mot de passe")
                    }
                }
        }
    }

    suspend fun reauthenticate(password: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        val user = auth.currentUser
        if (user == null) {
            cont.resume(Result.failure(Exception("Utilisateur non authentifié")))
            return@suspendCancellableCoroutine
        }
        val email = user.email
        if (email == null) {
            cont.resume(Result.failure(Exception("Email introuvable pour la ré-authentification")))
            return@suspendCancellableCoroutine
        }
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnSuccessListener { cont.resume(Result.success(Unit)) }
            .addOnFailureListener { ex -> cont.resume(Result.failure(ex)) }
    }

    fun signOut() {
        auth.signOut()
    }
}
