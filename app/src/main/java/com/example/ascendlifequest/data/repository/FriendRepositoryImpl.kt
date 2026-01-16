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
            // Firebase ne supporte pas les recherches "contains", donc on utilise une recherche par préfixe
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

    override suspend fun addFriend(currentUserId: String, friendId: String): Result<Unit> {
        return try {
            // Vérifier si l'amitié existe déjà
            if (areFriends(currentUserId, friendId)) {
                Log.d(TAG, "Amitié déjà existante entre $currentUserId et $friendId")
                return Result.success(Unit)
            }

            // Créer deux documents d'amitié (bidirectionnel)
            val friendship1 = Friendship(
                userId = currentUserId,
                friendId = friendId,
                createdAt = Timestamp.now(),
                status = Friendship.STATUS_ACCEPTED
            )

            val friendship2 = Friendship(
                userId = friendId,
                friendId = currentUserId,
                createdAt = Timestamp.now(),
                status = Friendship.STATUS_ACCEPTED
            )

            // Utiliser un batch pour écrire les deux documents
            val batch = firestore.batch()

            val doc1Ref = friendshipsCollection.document("${currentUserId}_${friendId}")
            val doc2Ref = friendshipsCollection.document("${friendId}_${currentUserId}")

            batch.set(doc1Ref, friendship1)
            batch.set(doc2Ref, friendship2)

            batch.commit().await()

            Log.d(TAG, "Amitié créée entre $currentUserId et $friendId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout d'ami", e)
            Result.failure(e)
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
            // Récupérer toutes les amitiés de l'utilisateur
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

            // Firebase limite whereIn à 10 éléments, donc on fait des requêtes par lots
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

                    // Récupérer les profils des amis
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

