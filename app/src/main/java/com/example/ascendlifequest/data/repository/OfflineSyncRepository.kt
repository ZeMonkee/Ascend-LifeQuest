package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.data.model.Message
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.entities.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer la synchronisation des données entre Firebase et Room.
 * Implémente une stratégie "offline-first" pour les données.
 */
class OfflineSyncRepository(private val context: Context) {

    companion object {
        private const val TAG = "OfflineSyncRepository"
    }

    private val database = AppDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val networkManager = NetworkConnectivityManager.getInstance(context)

    private val userProfileDao = database.userProfileDao()
    private val friendshipDao = database.friendshipDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    // ===== USER PROFILE =====

    /**
     * Sauvegarde le profil utilisateur localement
     */
    suspend fun saveUserProfileLocally(profile: UserProfile, isCurrentUser: Boolean = false) {
        withContext(Dispatchers.IO) {
            val entity = UserProfileEntity.fromUserProfile(profile, isCurrentUser)
            userProfileDao.insertProfile(entity)
            Log.d(TAG, "Profil sauvegardé localement: ${profile.pseudo}")
        }
    }

    /**
     * Récupère le profil de l'utilisateur actuel (depuis le cache)
     */
    suspend fun getCurrentUserProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            userProfileDao.getCurrentUserProfile()?.toUserProfile()
        }
    }

    /**
     * Observe le profil de l'utilisateur actuel
     */
    fun observeCurrentUserProfile(): Flow<UserProfile?> {
        return userProfileDao.observeCurrentUserProfile().map { it?.toUserProfile() }
    }

    // ===== LEADERBOARD =====

    /**
     * Sauvegarde le classement localement
     */
    suspend fun saveLeaderboardLocally(profiles: List<UserProfile>) {
        withContext(Dispatchers.IO) {
            val currentUserId = userProfileDao.getCurrentUserProfile()?.id
            val entities = profiles.map { profile ->
                UserProfileEntity.fromUserProfile(
                    profile,
                    isCurrentUser = profile.id == currentUserId
                )
            }
            userProfileDao.insertProfiles(entities)
            Log.d(TAG, "Classement sauvegardé: ${profiles.size} profils")
        }
    }

    /**
     * Récupère le classement depuis le cache
     */
    suspend fun getLeaderboardFromCache(limit: Int = 100): List<UserProfile> {
        return withContext(Dispatchers.IO) {
            userProfileDao.getLeaderboard(limit).map { it.toUserProfile() }
        }
    }

    /**
     * Observe le classement (depuis le cache)
     */
    fun observeLeaderboard(limit: Int = 100): Flow<List<UserProfile>> {
        return userProfileDao.observeLeaderboard(limit).map { entities ->
            entities.map { it.toUserProfile() }
        }
    }

    // ===== FRIENDS =====

    /**
     * Sauvegarde les amis localement
     */
    suspend fun saveFriendsLocally(friends: List<UserProfile>, currentUserId: String) {
        withContext(Dispatchers.IO) {
            // Sauvegarder les profils des amis
            val entities = friends.map { UserProfileEntity.fromUserProfile(it) }
            userProfileDao.insertProfiles(entities)

            // Sauvegarder les relations d'amitié
            friends.forEach { friend ->
                val friendshipEntity = FriendshipEntity(
                    id = "${currentUserId}_${friend.id}",
                    userId = currentUserId,
                    friendId = friend.id,
                    createdAtSeconds = System.currentTimeMillis() / 1000,
                    createdAtNanos = 0,
                    status = "accepted"
                )
                friendshipDao.insertFriendship(friendshipEntity)
            }
            Log.d(TAG, "Amis sauvegardés: ${friends.size}")
        }
    }

    /**
     * Récupère les amis depuis le cache
     */
    suspend fun getFriendsFromCache(userId: String): List<UserProfile> {
        return withContext(Dispatchers.IO) {
            val friendships = friendshipDao.getAcceptedFriendships(userId)
            val friendIds = friendships.map {
                if (it.userId == userId) it.friendId else it.userId
            }
            friendIds.mapNotNull { friendId ->
                userProfileDao.getProfileById(friendId)?.toUserProfile()
            }
        }
    }

    // ===== MESSAGES =====

    /**
     * Sauvegarde les messages localement
     */
    suspend fun saveMessagesLocally(messages: List<Message>) {
        withContext(Dispatchers.IO) {
            val entities = messages.map { MessageEntity.fromMessage(it) }
            messageDao.insertMessages(entities)
            Log.d(TAG, "Messages sauvegardés: ${messages.size}")
        }
    }

    /**
     * Récupère les messages d'une conversation depuis le cache
     */
    suspend fun getMessagesFromCache(conversationId: String): List<Message> {
        return withContext(Dispatchers.IO) {
            messageDao.getMessagesForConversation(conversationId).map { it.toMessage() }
        }
    }

    /**
     * Observe les messages d'une conversation
     */
    fun observeMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.observeMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toMessage() }
        }
    }

    /**
     * Sauvegarde un message envoyé hors ligne (pour synchronisation ultérieure)
     */
    suspend fun saveOfflineMessage(message: Message) {
        withContext(Dispatchers.IO) {
            val entity = MessageEntity.fromMessage(
                message,
                isSentLocally = true,
                syncStatus = MessageEntity.SYNC_STATUS_PENDING
            )
            messageDao.insertMessage(entity)
            Log.d(TAG, "Message hors ligne sauvegardé: ${message.id}")
        }
    }

    /**
     * Récupère les messages en attente de synchronisation
     */
    suspend fun getPendingMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            messageDao.getPendingMessages().map { it.toMessage() }
        }
    }

    /**
     * Marque un message comme synchronisé
     */
    suspend fun markMessageAsSynced(messageId: String) {
        withContext(Dispatchers.IO) {
            messageDao.updateSyncStatus(messageId, MessageEntity.SYNC_STATUS_SYNCED)
        }
    }

    // ===== SYNC =====

    /**
     * Synchronise les messages en attente avec Firebase
     */
    suspend fun syncPendingMessages() {
        if (!networkManager.checkCurrentConnectivity()) {
            Log.d(TAG, "Pas de connexion, synchronisation reportée")
            return
        }

        withContext(Dispatchers.IO) {
            val pendingMessages = getPendingMessages()
            Log.d(TAG, "Synchronisation de ${pendingMessages.size} messages en attente")

            pendingMessages.forEach { message ->
                try {
                    val messageData = hashMapOf(
                        "conversationId" to message.conversationId,
                        "senderId" to message.senderId,
                        "receiverId" to message.receiverId,
                        "content" to message.content,
                        "timestamp" to message.timestamp,
                        "isRead" to message.isRead,
                        "type" to message.type
                    )

                    firestore.collection("messages")
                        .document(message.id)
                        .set(messageData)
                        .await()

                    markMessageAsSynced(message.id)
                    Log.d(TAG, "Message synchronisé: ${message.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur synchronisation message ${message.id}", e)
                    messageDao.updateSyncStatus(message.id, MessageEntity.SYNC_STATUS_FAILED)
                }
            }
        }
    }

    /**
     * Vérifie si l'appareil est connecté
     */
    fun isOnline(): Boolean {
        return networkManager.checkCurrentConnectivity()
    }

    /**
     * Observe l'état de la connectivité
     */
    fun observeConnectivity(): Flow<Boolean> {
        return networkManager.observeConnectivity()
    }

    /**
     * Nettoie les données locales (lors de la déconnexion)
     */
    suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            userProfileDao.deleteAll()
            friendshipDao.deleteAll()
            conversationDao.deleteAll()
            messageDao.deleteAll()
            Log.d(TAG, "Données locales effacées")
        }
    }
}
