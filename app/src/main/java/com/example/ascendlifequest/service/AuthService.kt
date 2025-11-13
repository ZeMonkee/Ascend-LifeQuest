package com.example.ascendlifequest.service

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class AuthService(private val context: Context) {
    private val TAG = "AuthService"
    private val auth = FirebaseAuth.getInstance()

    // Le webClientId doit correspondre au client_id dans le fichier google-services.json
    // Dans l'objet oauth_client avec client_type 3
    private val webClientId = "215110936631-pum9ivl9r5mnklg1cgngpjg6olkqgbf2.apps.googleusercontent.com"
    // Configuration pour l'authentification Google
    private val googleSignInClient: GoogleSignInClient by lazy {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            Log.d(TAG, "Initialisation de GoogleSignInClient avec webClientId: $webClientId")
            GoogleSignIn.getClient(context, gso)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création du client Google Sign-In", e)
            throw e
        }
    }

    // Vérifier si un utilisateur est connecté
    fun isUserLoggedIn(): Boolean {
        val user = auth.currentUser
        Log.d(TAG, "État de connexion: ${user != null}, User: ${user?.email}")
        return user != null
    }

    // Obtenir l'utilisateur actuel
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Connexion avec email et mot de passe
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Connexion par email réussie: ${it.email}")
                Result.success(it)
            } ?: Result.failure(Exception("Échec de connexion"))
        } catch (e: Exception) {
            Log.e(TAG, "Erreur de connexion par email", e)
            Result.failure(e)
        }
    }

    // Inscription avec email et mot de passe
    suspend fun registerWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Inscription réussie: ${it.email}")
                Result.success(it)
            } ?: Result.failure(Exception("Échec d'inscription"))
        } catch (e: Exception) {
            Log.e(TAG, "Erreur d'inscription", e)
            Result.failure(e)
        }
    }

    // Déconnexion
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        Log.d(TAG, "Utilisateur déconnecté")
    }

    // Lancer la connexion Google - MÉTHODE CORRIGÉE
    fun signInWithGoogle(activity: ComponentActivity, launcher: ActivityResultLauncher<Intent>) {
        try {
            Log.d(TAG, "Lancement de la connexion Google avec le client existant")
            // Utiliser directement l'instance lazy au lieu d'en créer une nouvelle
            launcher.launch(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du lancement de Google Sign-In", e)
            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    // Traiter le résultat de la connexion Google - MÉTHODE CORRIGÉE
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
                    // Utiliser withTimeout correctement - comme une fonction englobante
                    withTimeout(20000L) {
                        val account = task.getResult(ApiException::class.java)
                        Log.d(TAG, "Google Sign In réussi, compte: ${account.email}")

                        if (account.idToken == null) {
                            Log.e(TAG, "ID Token null, vérifiez la configuration OAuth")
                            return@withTimeout Result.failure(Exception("Token ID Google invalide"))
                        }

                        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                        val authResult = auth.signInWithCredential(credential).await()

                        authResult.user?.let {
                            Result.success(it)
                        } ?: Result.failure(Exception("Échec de connexion Firebase"))
                    }
                } catch (e: ApiException) {
                    val errorDetails = getGoogleSignInErrorDetails(e)
                    Log.e(TAG, "Échec Google sign in: $errorDetails", e)
                    Result.failure(Exception(errorDetails))
                } catch (e: java.util.concurrent.TimeoutException) {
                    Log.e(TAG, "Timeout lors de l'authentification Google", e)
                    Result.failure(Exception("Erreur réseau: vérifiez votre connexion internet"))
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Log.e(TAG, "Timeout lors de l'authentification Google", e)
                    Result.failure(Exception("Erreur réseau: vérifiez votre connexion internet"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception lors de la connexion Google", e)
                Result.failure(e)
            }
        }
    }
    // Fonction utilitaire pour traduire les codes d'erreur Google Sign-In
    private fun getGoogleSignInErrorDetails(e: ApiException): String {
        return when (e.statusCode) {
            GoogleSignInStatusCodes.NETWORK_ERROR -> "Erreur réseau: vérifiez votre connexion internet"
            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Authentification annulée"
            GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Échec de l'authentification"
            GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Authentification requise"
            GoogleSignInStatusCodes.TIMEOUT -> "Délai d'attente dépassé"
            else -> "Erreur code: ${e.statusCode}"
        }
    }
    // Réinitialisation du mot de passe
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

    // Récupérer l'ID utilisateur actuel (uid)
    fun getUserId(): String {
        return try {
            auth.currentUser!!.uid
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération de l'ID utilisateur", e)
        } as String
    }
}
