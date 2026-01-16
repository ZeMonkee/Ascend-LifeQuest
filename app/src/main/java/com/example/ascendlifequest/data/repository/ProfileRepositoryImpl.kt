package com.example.ascendlifequest.data.repository

import android.util.Log
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepository"
        private const val COLLECTION_PROFILE = "profile"
    }

    private val profileCollection = firestore.collection(COLLECTION_PROFILE)

    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                Log.w(TAG, "Aucun utilisateur connecté")
                return Result.failure(Exception("Utilisateur non connecté"))
            }
            getProfileById(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du profil", e)
            Result.failure(e)
        }
    }

    override suspend fun getProfileById(userId: String): Result<UserProfile?> {
        return try {
            val document = profileCollection.document(userId).get().await()
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                Log.d(TAG, "Profil récupéré: $profile")
                Result.success(profile)
            } else {
                Log.d(TAG, "Aucun profil trouvé pour l'utilisateur: $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du profil pour $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            val userId = if (profile.id.isNotEmpty()) profile.id else authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                return Result.failure(Exception("ID utilisateur invalide"))
            }

            profileCollection.document(userId).set(profile).await()
            Log.d(TAG, "Profil sauvegardé pour: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde du profil", e)
            Result.failure(e)
        }
    }

    override suspend fun createProfileForNewUser(userId: String, email: String): Result<UserProfile> {
        return try {
            // Vérifier si le profil existe déjà
            val existingProfile = getProfileById(userId).getOrNull()
            if (existingProfile != null) {
                Log.d(TAG, "Profil existant trouvé pour: $userId")
                return Result.success(existingProfile)
            }

            // Extraire le pseudo de l'email (partie avant @)
            val pseudo = email.substringBefore("@").take(20)

            val newProfile = UserProfile(
                id = userId,
                uid = userId,  // UID Firebase pour identification unique
                pseudo = pseudo,
                photoUrl = "",
                xp = 0,
                quetesRealisees = 0,
                streak = 0,
                dateDeCreation = Timestamp.now(),
                rang = 0,
                online = true
            )

            // Créer un map pour inclure explicitement l'UID comme champ
            val profileData = hashMapOf(
                "uid" to userId,  // ID Firebase pour recherche unique
                "pseudo" to newProfile.pseudo,
                "photoUrl" to newProfile.photoUrl,
                "xp" to newProfile.xp,
                "quetesRealisees" to newProfile.quetesRealisees,
                "streak" to newProfile.streak,
                "dateDeCreation" to newProfile.dateDeCreation,
                "rang" to newProfile.rang,
                "online" to newProfile.online
            )

            profileCollection.document(userId).set(profileData).await()
            Log.d(TAG, "Nouveau profil créé pour: $userId avec UID comme champ")
            Result.success(newProfile)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création du profil pour $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateXp(userId: String, xpToAdd: Long): Result<Unit> {
        return try {
            val docRef = profileCollection.document(userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentXp = snapshot.getLong("xp") ?: 0L
                transaction.update(docRef, "xp", currentXp + xpToAdd)
            }.await()
            Log.d(TAG, "XP mis à jour pour $userId: +$xpToAdd")

            // Mettre à jour le rang après le changement d'XP
            updateUserRank(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'XP", e)
            Result.failure(e)
        }
    }

    override suspend fun incrementQuestsCompleted(userId: String): Result<Unit> {
        return try {
            val docRef = profileCollection.document(userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentQuests = snapshot.getLong("quetesRealisees")?.toInt() ?: 0
                transaction.update(docRef, "quetesRealisees", currentQuests + 1)
            }.await()
            Log.d(TAG, "Quêtes complétées incrémentées pour $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'incrémentation des quêtes", e)
            Result.failure(e)
        }
    }

    override suspend fun updateStreak(userId: String, newStreak: Int): Result<Unit> {
        return try {
            profileCollection.document(userId)
                .update("streak", newStreak)
                .await()
            Log.d(TAG, "Streak mis à jour pour $userId: $newStreak")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du streak", e)
            Result.failure(e)
        }
    }

    override fun observeProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listenerRegistration = profileCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'observation du profil", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun getUserRank(userId: String): Result<Int> {
        return try {
            // Récupérer l'XP de l'utilisateur actuel
            val currentUserProfile = getProfileById(userId).getOrNull()
                ?: return Result.failure(Exception("Profil non trouvé"))

            val userXp = currentUserProfile.xp

            // Compter combien d'utilisateurs ont plus d'XP
            val usersWithMoreXp = profileCollection
                .whereGreaterThan("xp", userXp)
                .get()
                .await()

            val rank = usersWithMoreXp.size() + 1
            Log.d(TAG, "Rang calculé pour $userId: $rank (XP: $userXp)")
            Result.success(rank)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du calcul du rang", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePseudo(userId: String, newPseudo: String): Result<Unit> {
        return try {
            if (newPseudo.isBlank() || newPseudo.length > 20) {
                return Result.failure(Exception("Le pseudo doit contenir entre 1 et 20 caractères"))
            }

            profileCollection.document(userId)
                .update("pseudo", newPseudo.trim())
                .await()
            Log.d(TAG, "Pseudo mis à jour pour $userId: $newPseudo")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du pseudo", e)
            Result.failure(e)
        }
    }

    /**
     * Met à jour le rang de l'utilisateur dans Firestore
     */
    private suspend fun updateUserRank(userId: String) {
        try {
            val rank = getUserRank(userId).getOrNull() ?: return
            profileCollection.document(userId)
                .update("rang", rank)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du rang", e)
        }
    }
}

