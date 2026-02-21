package com.example.ascendlifequest.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ascendlifequest.data.model.Friendship
import com.google.firebase.Timestamp

/**
 * Entité Room pour stocker les relations d'amitié.
 * Relation Many-to-Many entre utilisateurs via cette table de jointure.
 */
@Entity(
    tableName = "friendships",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["friendId"]),
        Index(value = ["userId", "friendId"], unique = true),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FriendshipEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val friendId: String,
    val createdAtSeconds: Long,
    val createdAtNanos: Int,
    val status: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    fun toFriendship(): Friendship {
        return Friendship(
            id = id,
            userId = userId,
            friendId = friendId,
            createdAt = Timestamp(createdAtSeconds, createdAtNanos),
            status = status
        )
    }

    companion object {
        fun fromFriendship(friendship: Friendship): FriendshipEntity {
            return FriendshipEntity(
                id = friendship.id,
                userId = friendship.userId,
                friendId = friendship.friendId,
                createdAtSeconds = friendship.createdAt.seconds,
                createdAtNanos = friendship.createdAt.nanoseconds,
                status = friendship.status,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }
}
