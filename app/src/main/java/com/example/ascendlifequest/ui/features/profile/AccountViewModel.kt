package com.example.ascendlifequest.ui.features.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class AccountUiState {
    object Idle : AccountUiState()
    object Loading : AccountUiState()
    data class Success(val message: String, val email: String?) : AccountUiState()
    data class Error(val message: String, val email: String?) : AccountUiState()
    data class ReauthRequired(val action: String, val pendingValue: String) : AccountUiState()
    data class Loaded(val email: String?, val photoUrl: String?) : AccountUiState()
}

class AccountViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState
    private var lastKnownEmail: String? = null

    fun loadCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            val emailOrName = user.email ?: user.displayName ?: "Utilisateur anonyme"
            lastKnownEmail = user.email
            _uiState.value = AccountUiState.Loaded(emailOrName, user.photoUrl?.toString())
        } else {
            if (lastKnownEmail != null) {
                _uiState.value = AccountUiState.Loaded(lastKnownEmail, null)
            } else {
                _uiState.value = AccountUiState.Error("Utilisateur non connecté", null)
            }
        }
    }

    fun refreshUser() {
        val currentState = _uiState.value
        if (currentState is AccountUiState.Success || currentState is AccountUiState.Error) {
            return
        }

        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener {
                loadCurrentUser()
            }
        } else {
            if (lastKnownEmail != null) {
                _uiState.value = AccountUiState.Loaded(lastKnownEmail, null)
            }
        }
    }

    fun updateEmail(newEmail: String) {
        val user = auth.currentUser

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            _uiState.value = AccountUiState.Error("Email invalide", user?.email)
            return
        }
        if (user == null) {
            _uiState.value = AccountUiState.Error("Utilisateur non authentifié", null)
            return
        }

        val hasPasswordProvider = user.providerData.any { it?.providerId == "password" }
        if (!hasPasswordProvider) {
            _uiState.value = AccountUiState.Error(
                "Impossible de modifier l'e-mail : votre compte est lié via un fournisseur externe. " +
                "Pour changer l'email, liez d'abord une méthode Email/Password.",
                user.email
            )
            return
        }

        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    val currentEmail = auth.currentUser?.email ?: lastKnownEmail
                    _uiState.value = AccountUiState.Success(
                        "Un e-mail de vérification a été envoyé à $newEmail.\n\n" +
                        "⚠️ IMPORTANT : Vous allez être déconnecté. Vérifiez votre boîte mail ($newEmail), " +
                        "cliquez sur le lien de vérification, puis reconnectez-vous avec votre nouveau email.",
                        currentEmail
                    )
                }
                .addOnFailureListener { ex ->
                    val currentEmail = auth.currentUser?.email
                    if (ex is FirebaseAuthRecentLoginRequiredException) {
                        _uiState.value = AccountUiState.ReauthRequired("email", newEmail)
                    } else if (ex is FirebaseAuthException) {
                        val code = ex.errorCode
                        if (code.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                            ex.message?.contains("Please verify the new email", ignoreCase = true) == true) {
                            _uiState.value = AccountUiState.Error(
                                "Opération interdite : activez le provider Email/Password dans la console Firebase.",
                                currentEmail
                            )
                        } else {
                            _uiState.value = AccountUiState.Error(
                                ex.message ?: "Erreur lors de la mise à jour de l'email",
                                currentEmail
                            )
                        }
                    } else {
                        _uiState.value = AccountUiState.Error(
                            ex.message ?: "Erreur lors de la mise à jour de l'email",
                            currentEmail
                        )
                    }
                }
        }
    }

    fun updatePassword(newPassword: String) {
        val user = auth.currentUser

        if (newPassword.length < 6) {
            _uiState.value = AccountUiState.Error(
                "Le mot de passe doit contenir au moins 6 caractères",
                user?.email
            )
            return
        }
        if (user == null) {
            _uiState.value = AccountUiState.Error("Utilisateur non authentifié", null)
            return
        }

        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    val currentEmail = auth.currentUser?.email
                    _uiState.value = AccountUiState.Success(
                        "Mot de passe mis à jour avec succès.\n\n" +
                        "⚠️ IMPORTANT : Vous allez être déconnecté pour des raisons de sécurité. " +
                        "Reconnectez-vous avec votre nouveau mot de passe.",
                        currentEmail
                    )
                }
                .addOnFailureListener { ex ->
                    val currentEmail = auth.currentUser?.email
                    if (ex is FirebaseAuthRecentLoginRequiredException) {
                        _uiState.value = AccountUiState.ReauthRequired("password", newPassword)
                    } else {
                        _uiState.value = AccountUiState.Error(
                            ex.message ?: "Erreur lors de la mise à jour du mot de passe",
                            currentEmail
                        )
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
