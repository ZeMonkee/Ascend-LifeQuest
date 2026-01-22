package com.example.ascendlifequest.data.model

/**
 * Represents user authentication credentials.
 *
 * @property id Unique account identifier
 * @property email User email address
 * @property password Encrypted password hash
 */
data class Account(val id: Int, val email: String, val password: String)
