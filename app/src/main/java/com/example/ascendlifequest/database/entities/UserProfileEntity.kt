package com.example.ascendlifequest.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ascendlifequest.data.model.UserProfile
import com.google.firebase.Timestamp

/**
 * Entité Room pour stocker les profils utilisateurs localement.
 * Permet le mode hors ligne et le cache du classement.
 */
@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["xp"]), // Pour trier le classement
        Index(value = ["pseudo"])
    ]
)
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val uid: String,
    val pseudo: String,
    val photoUrl: String,
    val xp: Long,
    val quetesRealisees: Int,
    val streak: Int,
    val dateDeCreationSeconds: Long,
    val dateDeCreationNanos: Int,
    val rang: Int,
    val online: Boolean,
    val isCurrentUser: Boolean = false, // Marque le profil de l'utilisateur connecté
    val lastSyncTimestamp: Long = System.currentTimeMillis() // Pour savoir quand les données ont été synchronisées
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            uid = uid,
            pseudo = pseudo,
            photoUrl = photoUrl,
            xp = xp,
            quetesRealisees = quetesRealisees,
            streak = streak,
            dateDeCreation = Timestamp(dateDeCreationSeconds, dateDeCreationNanos),
            rang = rang,
            online = online
        )
    }

    companion object {
        fun fromUserProfile(
            profile: UserProfile,
            isCurrentUser: Boolean = false
        ): UserProfileEntity {
            return UserProfileEntity(
                id = profile.id,
                uid = profile.uid,
                pseudo = profile.pseudo,
                photoUrl = profile.photoUrl,
                xp = profile.xp,
                quetesRealisees = profile.quetesRealisees,
                streak = profile.streak,
                dateDeCreationSeconds = profile.dateDeCreation.seconds,
                dateDeCreationNanos = profile.dateDeCreation.nanoseconds,
                rang = profile.rang,
                online = profile.online,
                isCurrentUser = isCurrentUser,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }
}
