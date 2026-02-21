package com.example.ascendlifequest.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ascendlifequest.data.model.Message
import com.google.firebase.Timestamp

/**
 * Entité Room pour stocker les messages.
 * Relation Many-to-One avec Conversation.
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["senderId"]),
        Index(value = ["receiverId"]),
        Index(value = ["timestampSeconds"], orders = [Index.Order.ASC]),
        Index(value = ["conversationId", "timestampSeconds"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestampSeconds: Long,
    val timestampNanos: Int,
    val isRead: Boolean,
    val type: String,
    val isSentLocally: Boolean = false, // Pour les messages envoyés hors ligne
    val syncStatus: Int = SYNC_STATUS_SYNCED, // État de synchronisation
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    fun toMessage(): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = Timestamp(timestampSeconds, timestampNanos),
            isRead = isRead,
            type = type
        )
    }

    companion object {
        const val SYNC_STATUS_PENDING = 0
        const val SYNC_STATUS_SYNCED = 1
        const val SYNC_STATUS_FAILED = 2

        fun fromMessage(
            message: Message,
            isSentLocally: Boolean = false,
            syncStatus: Int = SYNC_STATUS_SYNCED
        ): MessageEntity {
            return MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                receiverId = message.receiverId,
                content = message.content,
                timestampSeconds = message.timestamp.seconds,
                timestampNanos = message.timestamp.nanoseconds,
                isRead = message.isRead,
                type = message.type,
                isSentLocally = isSentLocally,
                syncStatus = syncStatus
            )
        }
    }
}
