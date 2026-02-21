package com.example.ascendlifequest.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ascendlifequest.data.model.Conversation

/**
 * Entité Room pour stocker les conversations.
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["lastMessageTimestampSeconds"], orders = [Index.Order.DESC])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val lastMessage: String,
    val lastMessageSenderId: String,
    val lastMessageTimestampSeconds: Long,
    val lastMessageTimestampNanos: Int,
    val createdAtSeconds: Long,
    val createdAtNanos: Int,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromConversation(conversation: Conversation): ConversationEntity {
            return ConversationEntity(
                id = conversation.id,
                lastMessage = conversation.lastMessage,
                lastMessageSenderId = conversation.lastMessageSenderId,
                lastMessageTimestampSeconds = conversation.lastMessageTimestamp.seconds,
                lastMessageTimestampNanos = conversation.lastMessageTimestamp.nanoseconds,
                createdAtSeconds = conversation.createdAt.seconds,
                createdAtNanos = conversation.createdAt.nanoseconds
            )
        }
    }
}

/**
 * Table de jointure Many-to-Many entre Conversations et Users.
 */
@Entity(
    tableName = "conversation_participants",
    primaryKeys = ["conversationId", "participantId"],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["participantId"])
    ]
)
data class ConversationParticipantEntity(
    val conversationId: String,
    val participantId: String
)
