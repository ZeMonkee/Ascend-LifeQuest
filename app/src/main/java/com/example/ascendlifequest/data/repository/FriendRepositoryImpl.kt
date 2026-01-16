package com.example.ascendlifequest.data.repository

import android.util.Log
import com.example.ascendlifequest.data.model.Friendship
import com.example.ascendlifequest.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FriendRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FriendRepository {

    companion object {
        private const val TAG = "FriendRepository"
        private const val COLLECTION_PROFILE = "profile"
        private const val COLLECTION_FRIENDSHIPS = "friendships"
    }

    private val profileCollection = firestore.collection(COLLECTION_PROFILE)
    private val friendshipsCollection = firestore.collection(COLLECTION_FRIENDSHIPS)

    override suspend fun searchUsersByPseudo(query: String, currentUserId: String): Result<List<UserProfile>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val queryLower = query.lowercase().trim()

            // Recherche par préfixe sur le pseudo
            val results = profileCollection
                .orderBy("pseudo")
                .startAt(queryLower)
                .endAt(queryLower + "\uf8ff")
                .limit(10)
                .get()
                .await()

            val users = results.documents.mapNotNull { doc ->
                doc.toObject(UserProfile::class.java)?.takeIf {
                    it.uid != currentUserId &&
                    it.pseudo.lowercase().contains(queryLower)
                }
            }

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
            val friendRequestData = hashMapOf(
                "userId" to currentUserId,
                "friendId" to friendId,
                "createdAt" to Timestamp.now(),
                "status" to Friendship.STATUS_PENDING
            )

            Log.d(TAG, "Création document: $docId avec data: $friendRequestData")

            friendshipsCollection
                .document(docId)
                .set(friendRequestData)
                .await()

            Log.d(TAG, "✅ Demande d'ami envoyée avec succès de $currentUserId à $friendId (docId: $docId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'envoi de la demande d'ami", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptFriendRequest(currentUserId: String, friendId: String): Result<Unit> {
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
            val friendship2 = Friendship(
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

    override suspend fun declineFriendRequest(currentUserId: String, friendId: String): Result<Unit> {
        return try {
            // La demande a été envoyée par friendId vers currentUserId
            val requestDocId = "${friendId}_${currentUserId}"

            // Supprimer la demande
            friendshipsCollection.document(requestDocId).delete().await()

            Log.d(TAG, "Demande d'ami refusée: $currentUserId a refusé $friendId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du refus de la demande d'ami", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingFriendRequests(userId: String): Result<List<UserProfile>> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "Recherche des demandes d'amis en attente pour userId: $userId")

            // Approche simplifiée : récupérer toutes les demandes où friendId = userId
            // puis filtrer par status en mémoire (évite le besoin d'un index composite)
            val requests = friendshipsCollection
                .whereEqualTo("friendId", userId)
                .get()
                .await()

            Log.d(TAG, "Documents trouvés avec friendId=$userId: ${requests.documents.size}")

            // Filtrer les demandes en attente
            val pendingRequests = requests.documents.mapNotNull { doc ->
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
            val profiles = mutableListOf<UserProfile>()
            pendingRequests.chunked(10).forEach { chunk ->
                Log.d(TAG, "Recherche profils pour: $chunk")
                val profileDocs = profileCollection
                    .whereIn("uid", chunk)
                    .get()
                    .await()

                Log.d(TAG, "Profils trouvés: ${profileDocs.documents.size}")

                profileDocs.documents.forEach { doc ->
                    val profile = doc.toObject(UserProfile::class.java)
                    Log.d(TAG, "  Profil: ${profile?.pseudo} (uid: ${profile?.uid})")
                    if (profile != null) {
                        profiles.add(profile)
                    }
                }
            }

            Log.d(TAG, "Total profils récupérés: ${profiles.size}")
            Log.d(TAG, "========================================")
            Result.success(profiles)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la récupération des demandes d'amis: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observePendingFriendRequests(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val listenerRegistration = friendshipsCollection
            .whereEqualTo("friendId", userId)
            .whereEqualTo("status", Friendship.STATUS_PENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'observation des demandes d'amis", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val senderIds = snapshot.documents.mapNotNull { doc ->
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
                                val users = profiles.documents.mapNotNull { doc ->
                                    doc.toObject(UserProfile::class.java)
                                }
                                trySend(users)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Erreur lors de la récupération des profils", e)
                                trySend(emptyList())
                            }
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun hasPendingRequest(currentUserId: String, friendId: String): Boolean {
        return try {
            // Vérifier si une demande existe dans un sens ou l'autre
            val doc1 = friendshipsCollection
                .document("${currentUserId}_${friendId}")
                .get()
                .await()

            if (doc1.exists()) {
                val friendship = doc1.toObject(Friendship::class.java)
                if (friendship?.status == Friendship.STATUS_PENDING) {
                    return true
                }
            }

            val doc2 = friendshipsCollection
                .document("${friendId}_${currentUserId}")
                .get()
                .await()

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
            // Récupérer toutes les amitiés acceptées de l'utilisateur
            val friendships = friendshipsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
                .get()
                .await()

            val friendIds = friendships.documents.mapNotNull { doc ->
                doc.toObject(Friendship::class.java)?.friendId
            }

            if (friendIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Récupérer les profils des amis
            val friends = mutableListOf<UserProfile>()
            friendIds.chunked(10).forEach { chunk ->
                val profiles = profileCollection
                    .whereIn("uid", chunk)
                    .get()
                    .await()

                profiles.documents.mapNotNullTo(friends) { doc ->
                    doc.toObject(UserProfile::class.java)
                }
            }

            // Trier par XP décroissant
            val sortedFriends = friends.sortedByDescending { it.xp }

            Log.d(TAG, "Récupéré ${sortedFriends.size} amis pour $userId")
            Result.success(sortedFriends)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des amis", e)
            Result.failure(e)
        }
    }

    override fun observeFriends(userId: String): Flow<List<UserProfile>> = callbackFlow {
        val listenerRegistration = friendshipsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", Friendship.STATUS_ACCEPTED)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'observation des amis", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val friendIds = snapshot.documents.mapNotNull { doc ->
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
                                val friends = profiles.documents.mapNotNull { doc ->
                                    doc.toObject(UserProfile::class.java)
                                }.sortedByDescending { it.xp }

                                trySend(friends)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Erreur lors de la récupération des profils", e)
                                trySend(emptyList())
                            }
                    }
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun areFriends(userId: String, friendId: String): Boolean {
        return try {
            val doc = friendshipsCollection
                .document("${userId}_${friendId}")
                .get()
                .await()

            doc.exists() && doc.toObject(Friendship::class.java)?.status == Friendship.STATUS_ACCEPTED
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
}

