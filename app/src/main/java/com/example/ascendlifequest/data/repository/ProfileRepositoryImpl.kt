package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.entities.UserProfileEntity
import com.example.ascendlifequest.di.AppContextProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileRepositoryImpl(
        private val authRepository: AuthRepository,
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        private val context: Context? = null
) : ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepository"
        private const val COLLECTION_PROFILE = "profile"
    }

    private val profileCollection = firestore.collection(COLLECTION_PROFILE)

    // Support hors ligne - utiliser AppContextProvider si context n'est pas fourni
    private val effectiveContext: Context? by lazy {
        context ?: AppContextProvider.getContextOrNull()
    }
    private val database by lazy { effectiveContext?.let { AppDatabase.getDatabase(it) } }
    private val userProfileDao by lazy { database?.userProfileDao() }
    private val networkManager by lazy { effectiveContext?.let { NetworkConnectivityManager.getInstance(it) } }

    private fun isOnline(): Boolean = networkManager?.checkCurrentConnectivity() ?: true

    /**
     * Sauvegarde un profil dans le cache local
     */
    private suspend fun saveToLocalCache(profile: UserProfile, isCurrentUser: Boolean = false) {
        userProfileDao?.let { dao ->
            withContext(Dispatchers.IO) {
                try {
                    val entity = UserProfileEntity.fromUserProfile(profile, isCurrentUser)
                    dao.insertProfile(entity)
                    Log.d(TAG, "Profil sauvegardé en cache local: ${profile.pseudo}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur sauvegarde cache local", e)
                }
            }
        }
    }

    /**
     * Récupère un profil depuis le cache local
     */
    private suspend fun getFromLocalCache(userId: String): UserProfile? {
        return userProfileDao?.let { dao ->
            withContext(Dispatchers.IO) {
                try {
                    dao.getProfileById(userId)?.toUserProfile()
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lecture cache local", e)
                    null
                }
            }
        }
    }

    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val userId = authRepository.getCurrentUserId()

            // Si hors ligne ou pas d'utilisateur connecté, essayer le cache
            if (userId.isEmpty() || !isOnline()) {
                Log.d(TAG, "Mode hors ligne ou pas d'utilisateur - tentative cache local")
                val cachedProfile = withContext(Dispatchers.IO) {
                    userProfileDao?.getCurrentUserProfile()?.toUserProfile()
                }
                if (cachedProfile != null) {
                    Log.d(TAG, "Profil récupéré depuis le cache: ${cachedProfile.pseudo}")
                    return Result.success(cachedProfile)
                }
                if (userId.isEmpty()) {
                    Log.w(TAG, "Aucun utilisateur connecté et pas de cache")
                    return Result.failure(Exception("Utilisateur non connecté"))
                }
            }

            getProfileById(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du profil", e)
            // Dernière tentative avec le cache
            val cachedProfile = withContext(Dispatchers.IO) {
                userProfileDao?.getCurrentUserProfile()?.toUserProfile()
            }
            if (cachedProfile != null) {
                Log.d(TAG, "Profil récupéré depuis le cache après erreur: ${cachedProfile.pseudo}")
                return Result.success(cachedProfile)
            }
            Result.failure(e)
        }
    }

    override suspend fun getProfileById(userId: String): Result<UserProfile?> {
        return try {
            // Si hors ligne, utiliser le cache
            if (!isOnline()) {
                Log.d(TAG, "Mode hors ligne - utilisation du cache pour $userId")
                val cached = getFromLocalCache(userId)
                return if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Profil non disponible hors ligne"))
                }
            }

            val document = profileCollection.document(userId).get().await()
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)

                // Si le champ uid est vide, le mettre à jour avec l'ID du document
                if (profile != null && profile.uid.isEmpty()) {
                    Log.d(TAG, "Mise à jour du champ uid pour le profil: $userId")
                    profileCollection.document(userId).update("uid", userId).await()
                    profile.uid = userId
                }

                // Sauvegarder dans le cache local
                if (profile != null) {
                    val isCurrentUser = userId == authRepository.getCurrentUserId()
                    saveToLocalCache(profile, isCurrentUser)
                }

                Log.d(TAG, "Profil récupéré: $profile")
                Result.success(profile)
            } else {
                Log.d(TAG, "Aucun profil trouvé pour l'utilisateur: $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du profil pour $userId", e)
            // Essayer le cache local en cas d'erreur
            val cached = getFromLocalCache(userId)
            if (cached != null) {
                Log.d(TAG, "Utilisation du cache local suite à une erreur")
                return Result.success(cached)
            }
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        return try {
            val userId =
                    if (profile.id.isNotEmpty()) profile.id else authRepository.getCurrentUserId()
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

    override suspend fun createProfileForNewUser(
            userId: String,
            email: String
    ): Result<UserProfile> {
        return try {
            // Vérifier si le profil existe déjà
            val existingProfile = getProfileById(userId).getOrNull()
            if (existingProfile != null) {
                Log.d(TAG, "Profil existant trouvé pour: $userId")
                return Result.success(existingProfile)
            }

            // Extraire le pseudo de l'email (partie avant @)
            val pseudo = email.substringBefore("@").take(20)

            val newProfile =
                    UserProfile(
                            id = userId,
                            uid = userId, // UID Firebase pour identification unique
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
            val profileData =
                    hashMapOf(
                            "uid" to userId, // ID Firebase pour recherche unique
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
            firestore
                    .runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentXp = snapshot.getLong("xp") ?: 0L
                        transaction.update(docRef, "xp", currentXp + xpToAdd)
                    }
                    .await()
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
            firestore
                    .runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentQuests = snapshot.getLong("quetesRealisees")?.toInt() ?: 0
                        transaction.update(docRef, "quetesRealisees", currentQuests + 1)
                    }
                    .await()
            Log.d(TAG, "Quêtes complétées incrémentées pour $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'incrémentation des quêtes", e)
            Result.failure(e)
        }
    }

    // Nouvelle méthode : décrémenter le compteur de quêtes réalisées (sans aller en négatif)
    override suspend fun decrementQuestsCompleted(userId: String): Result<Unit> {
        return try {
            val docRef = profileCollection.document(userId)
            firestore
                    .runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentQuests = snapshot.getLong("quetesRealisees")?.toInt() ?: 0
                        val newVal = (currentQuests - 1).coerceAtLeast(0)
                        transaction.update(docRef, "quetesRealisees", newVal)
                    }
                    .await()
            Log.d(TAG, "Quêtes complétées décrémentées pour $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la décrémentation des quêtes", e)
            Result.failure(e)
        }
    }

    override suspend fun updateStreak(userId: String, newStreak: Int): Result<Unit> {
        return try {
            profileCollection.document(userId).update("streak", newStreak).await()
            Log.d(TAG, "Streak mis à jour pour $userId: $newStreak")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du streak", e)
            Result.failure(e)
        }
    }

    override fun observeProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listenerRegistration =
                profileCollection.document(userId).addSnapshotListener { snapshot, error ->
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

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getUserRank(userId: String): Result<Int> {
        return try {
            // Récupérer l'XP de l'utilisateur actuel
            val currentUserProfile =
                    getProfileById(userId).getOrNull()
                            ?: return Result.failure(Exception("Profil non trouvé"))

            val userXp = currentUserProfile.xp

            // Compter combien d'utilisateurs ont plus d'XP
            val usersWithMoreXp = profileCollection.whereGreaterThan("xp", userXp).get().await()

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

            profileCollection.document(userId).update("pseudo", newPseudo.trim()).await()
            Log.d(TAG, "Pseudo mis à jour pour $userId: $newPseudo")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du pseudo", e)
            Result.failure(e)
        }
    }

    /** Met à jour le rang de l'utilisateur dans Firestore */
    private suspend fun updateUserRank(userId: String) {
        try {
            val rank = getUserRank(userId).getOrNull() ?: return
            profileCollection.document(userId).update("rang", rank).await()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du rang", e)
        }
    }

    override suspend fun getLeaderboard(limit: Int): Result<List<UserProfile>> {
        return try {
            // Si hors ligne, utiliser le cache
            if (!isOnline()) {
                Log.d(TAG, "Mode hors ligne - utilisation du cache pour le classement")
                val cached = userProfileDao?.let { dao ->
                    withContext(Dispatchers.IO) {
                        dao.getLeaderboard(limit).map { it.toUserProfile() }
                    }
                } ?: emptyList()

                return if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Classement non disponible hors ligne"))
                }
            }

            val snapshot =
                    profileCollection
                            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(limit.toLong())
                            .get()
                            .await()

            val topUsers = snapshot.toObjects(UserProfile::class.java)

            // Assigner le rang en fonction de la position dans la liste (1-indexed)
            topUsers.forEachIndexed { index, user -> user.rang = index + 1 }

            // Sauvegarder dans le cache local
            userProfileDao?.let { dao ->
                withContext(Dispatchers.IO) {
                    val currentUserId = authRepository.getCurrentUserId()
                    val entities = topUsers.map { profile ->
                        UserProfileEntity.fromUserProfile(
                            profile,
                            isCurrentUser = profile.id == currentUserId
                        )
                    }
                    dao.insertProfiles(entities)
                    Log.d(TAG, "Classement sauvegardé en cache: ${entities.size} profils")
                }
            }

            Log.d(TAG, "Classement récupéré: ${topUsers.size} utilisateurs")
            Result.success(topUsers)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du classement", e)

            // Essayer le cache local en cas d'erreur
            val cached = userProfileDao?.let { dao ->
                withContext(Dispatchers.IO) {
                    dao.getLeaderboard(limit).map { it.toUserProfile() }
                }
            } ?: emptyList()

            if (cached.isNotEmpty()) {
                Log.d(TAG, "Utilisation du cache local suite à une erreur")
                return Result.success(cached)
            }

            Result.failure(e)
        }
    }
}
