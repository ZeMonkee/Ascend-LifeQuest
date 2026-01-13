package com.example.ascendlifequest.data.auth

import android.content.Intent
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun registerWithEmailPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser>
    suspend fun resetPassword(email: String): Result<Unit>
    fun signOut()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String
    fun getCurrentUser(): FirebaseUser?
}

