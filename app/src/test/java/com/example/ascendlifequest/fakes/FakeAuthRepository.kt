package com.example.ascendlifequest.fakes

import android.content.Intent
import com.example.ascendlifequest.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser

/**
 * Fake implementation of AuthRepository for unit testing.
 * Allows configuring responses for authentication operations.
 */
class FakeAuthRepository : AuthRepository {

    var signInResult: Result<FirebaseUser> = Result.failure(Exception("Not configured"))
    var registerResult: Result<FirebaseUser> = Result.failure(Exception("Not configured"))
    var resetPasswordResult: Result<Unit> = Result.success(Unit)
    var isLoggedIn: Boolean = false

    private var _userId: String = ""
    private var _user: FirebaseUser? = null

    override suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return signInResult
    }

    override suspend fun registerWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return registerResult
    }

    override suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> {
        return Result.failure(Exception("Google Sign-In not supported in tests"))
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return resetPasswordResult
    }

    override fun signOut() {
        isLoggedIn = false
        _userId = ""
        _user = null
    }

    override fun isUserLoggedIn(): Boolean = isLoggedIn

    override fun getCurrentUserId(): String = _userId

    override fun getCurrentUser(): FirebaseUser? = _user
}
