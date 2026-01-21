package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Récupère le profil de l'utilisateur connecté */
    suspend fun getCurrentUserProfile(): Result<UserProfile?>

    /** Récupère un profil par son ID */
    suspend fun getProfileById(userId: String): Result<UserProfile?>

    /** Crée ou met à jour le profil utilisateur */
    suspend fun saveProfile(profile: UserProfile): Result<Unit>

    /** Crée un nouveau profil pour un utilisateur nouvellement inscrit */
    suspend fun createProfileForNewUser(userId: String, email: String): Result<UserProfile>

    /** Met à jour l'XP de l'utilisateur */
    suspend fun updateXp(userId: String, xpToAdd: Long): Result<Unit>

    /** Incrémente le compteur de quêtes réalisées */
    suspend fun incrementQuestsCompleted(userId: String): Result<Unit>

    /** Met à jour le streak de l'utilisateur */
    suspend fun updateStreak(userId: String, newStreak: Int): Result<Unit>

    /** Observe les changements du profil en temps réel */
    fun observeProfile(userId: String): Flow<UserProfile?>

    /** Récupère le rang de l'utilisateur basé sur son XP */
    suspend fun getUserRank(userId: String): Result<Int>

    /** Met à jour le pseudo de l'utilisateur */
    suspend fun updatePseudo(userId: String, newPseudo: String): Result<Unit>
    /** Récupère le classement des utilisateurs (top X) */
    suspend fun getLeaderboard(limit: Int = 100): Result<List<UserProfile>>
}
