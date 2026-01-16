package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Friendship
import com.example.ascendlifequest.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    /**
     * Recherche des utilisateurs par pseudo (autocomplétion)
     */
    suspend fun searchUsersByPseudo(query: String, currentUserId: String): Result<List<UserProfile>>

    /**
     * Ajoute un ami
     */
    suspend fun addFriend(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Supprime un ami
     */
    suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Récupère la liste des amis d'un utilisateur
     */
    suspend fun getFriends(userId: String): Result<List<UserProfile>>

    /**
     * Observe la liste des amis en temps réel
     */
    fun observeFriends(userId: String): Flow<List<UserProfile>>

    /**
     * Vérifie si deux utilisateurs sont amis
     */
    suspend fun areFriends(userId: String, friendId: String): Boolean

    /**
     * Récupère un profil par son ID
     */
    suspend fun getProfileById(userId: String): Result<UserProfile?>
}

