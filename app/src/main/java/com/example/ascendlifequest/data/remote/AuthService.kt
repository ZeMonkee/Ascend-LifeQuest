package com.example.ascendlifequest.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.TimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Service handling Firebase Authentication and Google Sign-In. Provides all authentication-related
 * operations including email/password and Google OAuth authentication methods.
 */
class AuthService() {
    private val TAG = "AuthService"
    private val auth = FirebaseAuth.getInstance()

    // Le webClientId doit correspondre au client_id dans le fichier google-services.json
    // Dans l'objet oauth_client avec client_type 3
    private val webClientId =
            "215110936631-pum9ivl9r5mnklg1cgngpjg6olkqgbf2.apps.googleusercontent.com"

    /** Returns the Intent to launch Google Sign-In from UI. */
    fun getGoogleSignInIntent(context: Context): Intent {
        val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()

        Log.d(TAG, "Création de l'Intent GoogleSignIn avec webClientId: $webClientId")
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    /** Checks if a user is currently signed in. */
    fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        Log.d(TAG, "État de connexion: ${'$'}{user != null}, User: ${'$'}{user?.email}")
        return user != null
    }

    /** Returns the current Firebase user, or null if not signed in. */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /** Signs in with email and password credentials. */
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Connexion par email réussie: ${'$'}{it.email}")
                Result.success(it)
            }
                    ?: Result.failure(Exception("Échec de connexion"))
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de connexion par email", e)
            Result.failure(e)
        }
    }

    /** Registers a new user with email and password. */
    suspend fun registerWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Inscription réussie: ${'$'}{it.email}")
                Result.success(it)
            }
                    ?: Result.failure(Exception("Échec d'inscription"))
        } catch (e: Exception) {
            Log.e(TAG, "Erreur d'inscription", e)
            Result.failure(e)
        }
    }

    /** Signs out the current Firebase user. */
    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "Utilisateur Firebase déconnecté")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la déconnexion Firebase", e)
        }
    }

    /** Processes the Google Sign-In result and authenticates with Firebase. */
    suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Début du traitement du résultat Google Sign-In")

                if (data == null) {
                    Log.e(TAG, "Intent de résultat Google null")
                    return@withContext Result.failure(Exception("Données de connexion invalides"))
                }

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                    withTimeout(20000L) {
                        val account = task.getResult(ApiException::class.java)
                        Log.d(TAG, "Google Sign In réussi, compte: ${'$'}{account.email}")

                        if (account.idToken == null) {
                            Log.e(TAG, "ID Token null, vérifiez la configuration OAuth")
                            return@withTimeout Result.failure(Exception("Token ID Google invalide"))
                        }

                        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                        val authResult = auth.signInWithCredential(credential).await()

                        authResult.user?.let { Result.success(it) }
                                ?: Result.failure(Exception("Échec de connexion Firebase"))
                    }
                } catch (e: ApiException) {
                    val errorDetails = getGoogleSignInErrorDetails(e)
                    Log.e(TAG, "Échec Google sign in: $errorDetails", e)
                    Result.failure(Exception(errorDetails))
                } catch (e: TimeoutException) {
                    Log.e(TAG, "Timeout lors de l'authentification Google", e)
                    Result.failure(Exception("Erreur réseau: vérifiez votre connexion internet"))
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "Timeout lors de l'authentification Google", e)
                    Result.failure(Exception("Erreur réseau: vérifiez votre connexion internet"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception lors de la connexion Google", e)
                Result.failure(e)
            }
        }
    }

    private fun getGoogleSignInErrorDetails(e: ApiException): String {
        return when (e.statusCode) {
            GoogleSignInStatusCodes.NETWORK_ERROR ->
                    "Erreur réseau: vérifiez votre connexion internet"
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Authentification annulée"
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Échec de l'authentification"
            GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Authentification requise"
            GoogleSignInStatusCodes.TIMEOUT -> "Délai d'attente dépassé"
            else -> "Erreur code: ${'$'}{e.statusCode}"
        }
    }

    /** Sends a password reset email to the specified address. */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Email de réinitialisation envoyé à: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de réinitialisation de mot de passe", e)
            Result.failure(e)
        }
    }

    /** Returns the current user's UID, or empty string if not signed in. */
    fun getUserId(): String {
        return try {
            auth.currentUser?.uid ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération de l'ID utilisateur", e)
            ""
        }
    }
}
