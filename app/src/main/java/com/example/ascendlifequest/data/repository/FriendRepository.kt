package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Friendship
import com.example.ascendlifequest.data.model.Notification
import com.example.ascendlifequest.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FriendRepository {
    /**
     * Recherche des utilisateurs par pseudo (autocomplétion)
     */
    suspend fun searchUsersByPseudo(query: String, currentUserId: String): Result<List<UserProfile>>

    /**
     * Envoie une demande d'ami (statut pending)
     */
    suspend fun sendFriendRequest(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Accepte une demande d'ami
     */
    suspend fun acceptFriendRequest(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Refuse une demande d'ami et envoie une notification
     */
    suspend fun declineFriendRequest(currentUserId: String, friendId: String, currentUserPseudo: String): Result<Unit>

    /**
     * Récupère les demandes d'amis en attente (reçues)
     */
    suspend fun getPendingFriendRequests(userId: String): Result<List<UserProfile>>

    /**
     * Observe les demandes d'amis en attente en temps réel
     */
    fun observePendingFriendRequests(userId: String): Flow<List<UserProfile>>

    /**
     * Vérifie si une demande d'ami a déjà été envoyée
     */
    suspend fun hasPendingRequest(currentUserId: String, friendId: String): Boolean

    /**
     * Supprime un ami
     */
    suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Récupère la liste des amis d'un utilisateur (statut accepted uniquement)
     */
    suspend fun getFriends(userId: String): Result<List<UserProfile>>

    /**
     * Observe la liste des amis en temps réel
     */
    fun observeFriends(userId: String): Flow<List<UserProfile>>

    /**
     * Vérifie si deux utilisateurs sont amis (statut accepted)
     */
    suspend fun areFriends(userId: String, friendId: String): Boolean

    /**
     * Récupère un profil par son ID
     */
    suspend fun getProfileById(userId: String): Result<UserProfile?>

    /**
     * Récupère les notifications d'un utilisateur
     */
    suspend fun getNotifications(userId: String): Result<List<Notification>>

    /**
     * Marque une notification comme lue
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit>

    /**
     * Supprime une notification
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit>

    /**
     * Récupère les amis depuis le cache local (mode hors ligne)
     */
    suspend fun getFriendsFromCache(): Result<List<UserProfile>>
}
