package com.example.ascendlifequest.data.auth

import android.content.Intent
import com.example.ascendlifequest.data.remote.AuthService
import com.google.firebase.auth.FirebaseUser

/**
 * Implementation of [AuthRepository] that delegates to [AuthService].
 *
 * @property authService Service handling Firebase authentication operations
 */
class AuthRepositoryImpl(private val authService: AuthService) : AuthRepository {
    override suspend fun signInWithEmailPassword(
            email: String,
            password: String
    ): Result<FirebaseUser> {
        return authService.signInWithEmailPassword(email, password)
    }

    override suspend fun registerWithEmailPassword(
            email: String,
            password: String
    ): Result<FirebaseUser> {
        return authService.registerWithEmailPassword(email, password)
    }

    override suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> {
        return authService.handleGoogleSignInResult(data)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return authService.resetPassword(email)
    }

    override fun signOut() {
        authService.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    override fun getCurrentUserId(): String {
        return authService.getUserId()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return authService.getCurrentUser()
    }
}
