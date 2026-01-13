package com.example.ascendlifequest.ui.features.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.example.ascendlifequest.data.auth.AuthRepository
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

class AccountViewModel(
    private val authRepository: AuthRepository = com.example.ascendlifequest.data.auth.AuthRepositoryImpl(com.example.ascendlifequest.data.remote.AuthService())
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState
    private var lastKnownEmail: String? = null

    fun loadCurrentUser() {
        val user = authRepository.getCurrentUser()
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

        val user = authRepository.getCurrentUser()
        if (user != null) {
            // reload not available on repository; fallback to loadCurrentUser
            loadCurrentUser()
        } else {
            if (lastKnownEmail != null) {
                _uiState.value = AccountUiState.Loaded(lastKnownEmail, null)
            }
        }
    }

    fun updateEmail(newEmail: String) {
        val user = authRepository.getCurrentUser()

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

        // Keep legacy Firebase behaviour because repository currently does not expose verifyBeforeUpdateEmail
        _uiState.value = AccountUiState.Loading
        viewModelScope.launch {
            try {
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        val currentEmail = authRepository.getCurrentUser()?.email ?: lastKnownEmail
                        _uiState.value = AccountUiState.Success(
                            "Un e-mail de vérification a été envoyé à $newEmail.\n\n" +
                            "⚠️ IMPORTANT : Vous allez être déconnecté. Vérifiez votre boîte mail ($newEmail), " +
                            "cliquez sur le lien de vérification, puis reconnectez-vous avec votre nouveau email.",
                            currentEmail
                        )
                    }
                    .addOnFailureListener { ex ->
                        val currentEmail = authRepository.getCurrentUser()?.email
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
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error(e.localizedMessage ?: "Erreur lors de la mise à jour de l'email", authRepository.getCurrentUser()?.email)
            }
        }
    }

    fun updatePassword(newPassword: String) {
        val user = authRepository.getCurrentUser()

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
            try {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        val currentEmail = authRepository.getCurrentUser()?.email
                        _uiState.value = AccountUiState.Success(
                            "Mot de passe mis à jour avec succès.\n\n" +
                            "⚠️ IMPORTANT : Vous allez être déconnecté pour des raisons de sécurité. " +
                            "Reconnectez-vous avec votre nouveau mot de passe.",
                            currentEmail
                        )
                    }
                    .addOnFailureListener { ex ->
                        val currentEmail = authRepository.getCurrentUser()?.email
                        if (ex is FirebaseAuthRecentLoginRequiredException) {
                            _uiState.value = AccountUiState.ReauthRequired("password", newPassword)
                        } else {
                            _uiState.value = AccountUiState.Error(
                                ex.message ?: "Erreur lors de la mise à jour du mot de passe",
                                currentEmail
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = AccountUiState.Error(e.localizedMessage ?: "Erreur lors de la mise à jour du mot de passe", authRepository.getCurrentUser()?.email)
            }
        }
    }

    suspend fun reauthenticate(password: String): Result<Unit> = suspendCancellableCoroutine { cont ->
        val user = authRepository.getCurrentUser()
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
        authRepository.signOut()
    }
}
