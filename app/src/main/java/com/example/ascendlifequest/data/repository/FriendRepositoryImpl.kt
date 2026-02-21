package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.data.model.Friendship
import com.example.ascendlifequest.data.model.Notification
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.entities.FriendshipEntity
import com.example.ascendlifequest.database.entities.UserProfileEntity
import com.example.ascendlifequest.di.AppContextProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FriendRepositoryImpl(
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val context: Context? = null
) : FriendRepository {

    companion object {
        private const val TAG = "FriendRepository"
        private const val COLLECTION_PROFILE = "profile"
        private const val COLLECTION_FRIENDSHIPS = "friendships"
        private const val COLLECTION_NOTIFICATION = "notification"
    }

    private val profileCollection = firestore.collection(COLLECTION_PROFILE)
    private val friendshipsCollection = firestore.collection(COLLECTION_FRIENDSHIPS)
    private val notificationCollection = firestore.collection(COLLECTION_NOTIFICATION)

    // Support hors ligne - utiliser AppContextProvider si context n'est pas fourni
    private val effectiveContext: Context? by lazy {
        context ?: AppContextProvider.getContextOrNull()
    }
    private val database by lazy { effectiveContext?.let { AppDatabase.getDatabase(it) } }
    private val userProfileDao by lazy { database?.userProfileDao() }
    private val friendshipDao by lazy { database?.friendshipDao() }
    private val networkManager by lazy {
        effectiveContext?.let {
            NetworkConnectivityManager.getInstance(
                it
            )
        }
    }

    private fun isOnline(): Boolean = networkManager?.checkCurrentConnectivity() ?: true

    /**
     * Sauvegarde les amis dans le cache local
     */
    private suspend fun saveFriendsToCache(userId: String, friends: List<UserProfile>) {
        if (database == null) return
        withContext(Dispatchers.IO) {
            try {
                // Sauvegarder les profils des amis
                val profileEntities = friends.map { UserProfileEntity.fromUserProfile(it) }
                userProfileDao?.insertProfiles(profileEntities)

                // Sauvegarder les relations d'amitié
                friends.forEach { friend ->
                    val friendshipEntity = FriendshipEntity(
                        id = "${userId}_${friend.id}",
                        userId = userId,
                        friendId = friend.id,
                        createdAtSeconds = System.currentTimeMillis() / 1000,
                        createdAtNanos = 0,
                        status = Friendship.STATUS_ACCEPTED
                    )
                    friendshipDao?.insertFriendship(friendshipEntity)
                }
                Log.d(TAG, "Amis sauvegardés en cache: ${friends.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur sauvegarde cache amis", e)
            }
        }
    }

    /**
     * Récupère les amis depuis le cache local (méthode interne)
     */
    private suspend fun loadFriendsFromLocalCache(userId: String): List<UserProfile> {
        if (database == null) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val friendships = friendshipDao?.getAcceptedFriendships(userId) ?: emptyList()
                val friendIds = friendships.map {
                    if (it.userId == userId) it.friendId else it.userId
                }
                friendIds.mapNotNull { friendId ->
                    userProfileDao?.getProfileById(friendId)?.toUserProfile()
                }.sortedByDescending { it.xp }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lecture cache amis", e)
                emptyList()
            }
        }
    }

    override suspend fun searchUsersByPseudo(
        query: String,
        currentUserId: String
    ): Result<List<UserProfile>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val queryLower = query.lowercase().trim()

            // Récupérer tous les profils et filtrer côté client pour une recherche insensible à la
            // casse
            // Firebase ne supporte pas nativement les recherches case-insensitive
            val results =
                profileCollection
                    .limit(100) // Limiter pour éviter de charger trop de données
                    .get()
                    .await()

            val users =
                results.documents
                    .mapNotNull { doc ->
                        doc.toObject(UserProfile::class.java)?.takeIf {
                            it.uid != currentUserId &&
                                    it.pseudo.lowercase().startsWith(queryLower)
                        }
                    }
                    .take(10) // Limiter les résultats affichés

            Log.d(TAG, "Recherche '$query': ${users.size} résultats")
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la recherche d'utilisateurs", e)
            Result.failure(e)
        }
    }

    override suspend fun sendFriendRequest(currentUserId: String, friendId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Envoi demande d'ami: $currentUserId -> $friendId")

            // Vérifier si déjà amis
            if (areFriends(currentUserId, friendId)) {
                Log.d(TAG, "Déjà amis: $currentUserId et $friendId")
                return Result.failure(Exception("Vous êtes déjà amis"))
            }

            // Vérifier si une demande est déjà en attente
            if (hasPendingRequest(currentUserId, friendId)) {
                Log.d(TAG, "Demande déjà en attente entre $currentUserId et $friendId")
                return Result.failure(Exception("Une demande est déjà en attente"))
            }

            // Créer la demande d'ami
            val docId = "${currentUserId}_${friendId}"
            val friendRequestData =
                hashMapOf(
                    "userId" to currentUserId,
                    "friendId" to friendId,
                    "createdAt" to Timestamp.now(),
                    "status" to Friendship.STATUS_PENDING
                )

            Log.d(TAG, "Création document: $docId avec data: $friendRequestData")

            friendshipsCollection.document(docId).set(friendRequestData).await()

            Log.d(
                TAG,
                "Demande d'ami envoyee avec succes de $currentUserId a $friendId (docId: $docId)"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'envoi de la demande d'ami", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(
        currentUserId: String,
        friendId: String
    ): Result<Unit> {
        return try {
            // La demande a été envoyée par friendId vers currentUserId
            val requestDocId = "${friendId}_${currentUserId}"

            // Vérifier que la demande existe et est en pending
            val requestDoc = friendshipsCollection.document(requestDocId).get().await()
            if (!requestDoc.exists()) {
                return Result.failure(Exception("Demande d'ami introuvable"))
            }

            val friendship = requestDoc.toObject(Friendship::class.java)
            if (friendship?.status != Friendship.STATUS_PENDING) {
                return Result.failure(Exception("Cette demande n'est plus en attente"))
            }

            // Créer les deux documents d'amitié (bidirectionnel avec statut accepted)
            val batch = firestore.batch()

            // Mettre à jour la demande originale en accepted
            val doc1Ref = friendshipsCollection.document(requestDocId)
            batch.update(doc1Ref, "status", Friendship.STATUS_ACCEPTED)

            // Créer le document inverse
            val friendship2 =
                Friendship(
                    userId = currentUserId,
                    friendId = friendId,
                    createdAt = Timestamp.now(),
                    status = Friendship.STATUS_ACCEPTED
                )
            val doc2Ref = friendshipsCollection.document("${currentUserId}_${friendId}")
            batch.set(doc2Ref, friendship2)

            batch.commit().await()

            Log.d(TAG, "Demande d'ami acceptée: $currentUserId a accepté $friendId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'acceptation de la demande d'ami", e)
            Result.failure(e)
        }
    }

    override suspend fun declineFriendRequest(
        currentUserId: String,
        friendId: String,
        currentUserPseudo: String
    ): Result<Unit> {
        return try {
            Log.d(TAG, "declineFriendRequest")
            Log.d(TAG, "currentUserId: $currentUserId")
            Log.d(TAG, "friendId: $friendId")
            Log.d(TAG, "currentUserPseudo: $currentUserPseudo")

            // La demande a été envoyée par friendId vers currentUserId
            val requestDocId = "${friendId}_${currentUserId}"
            Log.d(TAG, "Suppression du document: $requestDocId")

            // Supprimer la demande
            friendshipsCollection.document(requestDocId).delete().await()
            Log.d(TAG, "Document supprimé")

            // Créer une notification pour informer l'utilisateur que sa demande a été refusée
            val notificationData =
                hashMapOf(
                    "userId" to
                            friendId, // L'utilisateur qui reçoit la notification (celui qui
                    // a envoyé la demande)
                    "type" to Notification.TYPE_FRIEND_REQUEST_DECLINED,
                    "message" to "$currentUserPseudo a refusé votre demande d'ami",
                    "fromUserId" to currentUserId,
                    "fromUserPseudo" to currentUserPseudo,
                    "createdAt" to Timestamp.now(),
                    "read" to false
                )

            Log.d(TAG, "Création notification: $notificationData")

            val notifRef = notificationCollection.add(notificationData).await()
            Log.d(TAG, "Notification créée avec ID: ${notifRef.id}")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du refus de la demande d'ami: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingFriendRequests(userId: String): Result<List<UserProfile>> {
        // En mode hors ligne, retourner une liste vide (les demandes sont des données temps réel)
        if (!isOnline()) {
            Log.d(TAG, "Mode hors ligne - pas de demandes d'amis disponibles")
            return Result.success(emptyList())
        }

        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "Recherche des demandes d'amis en attente")
            Log.d(TAG, "UserId (destinataire recherché): $userId")

            // Récupérer toutes les demandes où friendId = userId
            val requests = friendshipsCollection.whereEqualTo("friendId", userId).get().await()

            Log.d(TAG, "Documents trouvés avec friendId=$userId: ${requests.documents.size}")

            // Filtrer les demandes en attente
            val pendingRequests =
                requests.documents.mapNotNull { doc ->
                    val data = doc.data
                    Log.d(TAG, "Document ${doc.id}: $data")

                    val status = data?.get("status") as? String
                    val senderId = data?.get("userId") as? String

                    Log.d(TAG, "  -> status: $status, senderId: $senderId")

                    if (status == Friendship.STATUS_PENDING && senderId != null) {
                        senderId
                    } else {
                        null
                    }
                }

            Log.d(TAG, "Demandes pending trouvées: ${pendingRequests.size}")
            Log.d(TAG, "SenderIds: $pendingRequests")

            if (pendingRequests.isEmpty()) {
                Log.d(TAG, "Aucune demande d'ami en attente")
                Log.d(TAG, "========================================")
                return Result.success(emptyList())
            }

            // Récupérer les profils des demandeurs
            // On utilise l'ID du document (qui est le userId) car le champ 'uid' peut être vide
            val profiles = mutableListOf<UserProfile>()
            pendingRequests.forEach { senderId ->
                Log.d(TAG, "Recherche profil pour senderId: $senderId")
                try {
                    // Récupérer directement par l'ID du document
                    val profileDoc = profileCollection.document(senderId).get().await()
                    if (profileDoc.exists()) {
                        val profile = profileDoc.toObject(UserProfile::class.java)
                        if (profile != null) {
                            // S'assurer que l'uid est défini (utiliser l'ID du document si vide)
                            if (profile.uid.isEmpty()) {
                                profile.uid = senderId
                            }
                            profiles.add(profile)
                            Log.d(TAG, "  - Profil trouve: ${profile.pseudo}")
                        }
                    } else {
                        Log.d(TAG, "  - Profil non trouve pour $senderId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "  - Erreur recuperation profil $senderId", e)
                }
            }

            Log.d(TAG, "Total profils récupérés: ${profiles.size}")
            Log.d(TAG, "========================================")
            Result.success(profiles)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la recuperation des demandes d'amis: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observePendingFriendRequests(userId: String): Flow<List<UserProfile>> =
        callbackFlow {
            val listenerRegistration =
                friendshipsCollection
                    .whereEqualTo("friendId", userId)
                    .whereEqualTo("status", Friendship.STATUS_PENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(
                                TAG,
                                "Erreur lors de l'observation des demandes d'amis",
                                error
                            )
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            val senderIds =
                                snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(Friendship::class.java)?.userId
                                }

                            if (senderIds.isEmpty()) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            // Récupérer les profils des demandeurs
                            senderIds.chunked(10).forEach { chunk ->
                                profileCollection
                                    .whereIn("uid", chunk)
                                    .get()
                                    .addOnSuccessListener { profiles ->
                                        val users =
                                            profiles.documents.mapNotNull { doc
                                                ->
                                                doc.toObject(
                                                    UserProfile::class.java
                                                )
                                            }
                                        trySend(users)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            TAG,
                                            "Erreur lors de la récupération des profils",
                                            e
                                        )
                                        trySend(emptyList())
                                    }
                            }
                        } else {
                            trySend(emptyList())
                        }
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun hasPendingRequest(currentUserId: String, friendId: String): Boolean {
        return try {
            // Vérifier si une demande existe dans un sens ou l'autre
            val doc1 = friendshipsCollection.document("${currentUserId}_${friendId}").get().await()

            if (doc1.exists()) {
                val friendship = doc1.toObject(Friendship::class.java)
                if (friendship?.status == Friendship.STATUS_PENDING) {
                    return true
                }
            }

            val doc2 = friendshipsCollection.document("${friendId}_${currentUserId}").get().await()

            if (doc2.exists()) {
                val friendship = doc2.toObject(Friendship::class.java)
                if (friendship?.status == Friendship.STATUS_PENDING) {
                    return true
                }
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification de demande en attente", e)
            false
        }
    }

    override suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit> {
        return try {
            // Supprimer les deux documents d'amitié
            val batch = firestore.batch()

            val doc1Ref = friendshipsCollection.document("${currentUserId}_${friendId}")
            val doc2Ref = friendshipsCollection.document("${friendId}_${currentUserId}")

            batch.delete(doc1Ref)
            batch.delete(doc2Ref)

            batch.commit().await()

            Log.d(TAG, "Amitié supprimée entre $currentUserId et $friendId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression d'ami", e)
            Result.failure(e)
        }
    }

    override suspend fun getFriends(userId: String): Result<List<UserProfile>> {
        return try {
            // Si hors ligne, utiliser le cache
            if (!isOnline()) {
                Log.d(TAG, "Mode hors ligne - utilisation du cache pour les amis")
                val cachedFriends = loadFriendsFromLocalCache(userId)
                return if (cachedFriends.isNotEmpty()) {
                    Result.success(cachedFriends)
                } else {
                    Result.success(emptyList()) // Pas d'amis en cache, retourner liste vide
                }
            }

            // Récupérer toutes les amitiés acceptées de l'utilisateur
            val friendships =
                friendshipsCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
                    .get()
                    .await()

            val friendIds =
                friendships.documents.mapNotNull { doc ->
                    doc.toObject(Friendship::class.java)?.friendId
                }

            if (friendIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Récupérer les profils des amis
            val friends = mutableListOf<UserProfile>()
            friendIds.chunked(10).forEach { chunk ->
                val profiles = profileCollection.whereIn("uid", chunk).get().await()

                profiles.documents.mapNotNullTo(friends) { doc ->
                    doc.toObject(UserProfile::class.java)
                }
            }

            // Trier par XP décroissant
            val sortedFriends = friends.sortedByDescending { it.xp }

            // Sauvegarder dans le cache
            saveFriendsToCache(userId, sortedFriends)

            Log.d(TAG, "Récupéré ${sortedFriends.size} amis pour $userId")
            Result.success(sortedFriends)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des amis", e)
            // Essayer le cache en cas d'erreur
            val cachedFriends = loadFriendsFromLocalCache(userId)
            if (cachedFriends.isNotEmpty()) {
                Log.d(TAG, "Utilisation du cache suite à une erreur")
                return Result.success(cachedFriends)
            }
            Result.failure(e)
        }
    }

    override fun observeFriends(userId: String): Flow<List<UserProfile>> {
        // Si hors ligne, retourner un flow avec les données du cache
        if (!isOnline()) {
            return flow {
                val cachedFriends = loadFriendsFromLocalCache(userId)
                emit(cachedFriends)
            }
        }

        return callbackFlow {
            val listenerRegistration =
                friendshipsCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Erreur lors de l'observation des amis", error)
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val friendIds =
                                snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(Friendship::class.java)?.friendId
                                }

                            if (friendIds.isEmpty()) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }

                            friendIds.chunked(10).forEach { chunk ->
                                profileCollection
                                    .whereIn("uid", chunk)
                                    .get()
                                    .addOnSuccessListener { profiles ->
                                        val friends =
                                            profiles.documents
                                                .mapNotNull { doc ->
                                                    doc.toObject(
                                                        UserProfile::class.java
                                                    )
                                                }
                                                .sortedByDescending { it.xp }

                                        trySend(friends)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            TAG,
                                            "Erreur lors de la récupération des profils",
                                            e
                                        )
                                        trySend(emptyList())
                                    }
                            }
                        } else {
                            trySend(emptyList())
                        }
                    }

            awaitClose { listenerRegistration.remove() }
        }
    }

    override suspend fun areFriends(userId: String, friendId: String): Boolean {
        return try {
            val doc = friendshipsCollection.document("${userId}_${friendId}").get().await()

            doc.exists() &&
                    doc.toObject(Friendship::class.java)?.status == Friendship.STATUS_ACCEPTED
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification d'amitié", e)
            false
        }
    }

    override suspend fun getProfileById(userId: String): Result<UserProfile?> {
        return try {
            val document = profileCollection.document(userId).get().await()
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du profil", e)
            Result.failure(e)
        }
    }

    override suspend fun getNotifications(userId: String): Result<List<Notification>> {
        return try {
            val notifications =
                notificationCollection.whereEqualTo("userId", userId).get().await()

            val notificationList =
                notifications.documents
                    .mapNotNull { doc -> doc.toObject(Notification::class.java) }
                    .sortedByDescending { it.createdAt }

            Log.d(TAG, "Récupéré ${notificationList.size} notifications pour $userId")
            Result.success(notificationList)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des notifications", e)
            Result.failure(e)
        }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationCollection.document(notificationId).update("read", true).await()

            Log.d(TAG, "Notification $notificationId marquée comme lue")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du marquage de la notification", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationCollection.document(notificationId).delete().await()

            Log.d(TAG, "Notification $notificationId supprimée")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la notification", e)
            Result.failure(e)
        }
    }

    override suspend fun getFriendsFromCache(): Result<List<UserProfile>> {
        return try {
            if (database == null) {
                Log.d(TAG, "Base de données non disponible pour le cache")
                return Result.success(emptyList())
            }

            withContext(Dispatchers.IO) {
                // Récupérer le profil de l'utilisateur actuel depuis le cache
                val currentUserProfile = userProfileDao?.getCurrentUserProfile()

                if (currentUserProfile != null) {
                    val userId = currentUserProfile.id
                    Log.d(TAG, "Chargement des amis depuis le cache pour userId: $userId")

                    // Récupérer les amis depuis le cache
                    val friends = loadFriendsFromLocalCache(userId)
                    Log.d(TAG, "Amis trouvés en cache: ${friends.size}")
                    return@withContext Result.success(friends)
                }

                // Fallback: récupérer tous les profils qui ont une relation d'amitié
                Log.d(TAG, "Profil utilisateur non trouvé, tentative de récupération des amis via friendships")

                val allFriendships = friendshipDao?.getAllAcceptedFriendships() ?: emptyList()
                if (allFriendships.isEmpty()) {
                    Log.d(TAG, "Aucune amitié en cache")
                    return@withContext Result.success(emptyList())
                }

                // Récupérer les IDs uniques des amis
                val friendIds = allFriendships.flatMap { listOf(it.userId, it.friendId) }.distinct()

                val friends = friendIds.mapNotNull { friendId ->
                    userProfileDao?.getProfileById(friendId)?.toUserProfile()
                }.sortedByDescending { it.xp }

                Log.d(TAG, "Amis trouvés via fallback: ${friends.size}")
                Result.success(friends)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des amis depuis le cache", e)
            Result.failure(e)
        }
    }
}
