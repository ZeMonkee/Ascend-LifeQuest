package com.example.ascendlifequest.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.ascendlifequest.data.model.Conversation
import com.google.firebase.Timestamp

/**
 * Relation One-to-Many : Un utilisateur a plusieurs quêtes.
 */
data class UserWithQuests(
    @Embedded val user: UserProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val quests: List<QuestWithCategoryEntity>
)

/**
 * Représente une quête avec sa catégorie associée.
 * Permet d'avoir les informations complètes de la quête.
 */
data class QuestWithCategoryEntity(
    @Embedded val quest: com.example.ascendlifequest.database.QuestEntity,
    val categoryName: String? = null,
    val categoryColor: Long? = null
)

/**
 * Relation Many-to-Many : Utilisateurs et leurs amis.
 * Un utilisateur peut avoir plusieurs amis, et être ami avec plusieurs utilisateurs.
 */
data class UserWithFriends(
    @Embedded val user: UserProfileEntity,
    @Relation(
        entity = UserProfileEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FriendshipEntity::class,
            parentColumn = "userId",
            entityColumn = "friendId"
        )
    )
    val friends: List<UserProfileEntity>
)

/**
 * Relation Many-to-Many : Conversation avec ses participants.
 */
data class ConversationWithParticipants(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        entity = UserProfileEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ConversationParticipantEntity::class,
            parentColumn = "conversationId",
            entityColumn = "participantId"
        )
    )
    val participants: List<UserProfileEntity>
) {
    fun toConversation(): Conversation {
        return Conversation(
            id = conversation.id,
            participants = participants.map { it.id },
            lastMessage = conversation.lastMessage,
            lastMessageSenderId = conversation.lastMessageSenderId,
            lastMessageTimestamp = Timestamp(
                conversation.lastMessageTimestampSeconds,
                conversation.lastMessageTimestampNanos
            ),
            createdAt = Timestamp(
                conversation.createdAtSeconds,
                conversation.createdAtNanos
            )
        )
    }
}

/**
 * Relation One-to-Many : Conversation avec ses messages.
 */
data class ConversationWithMessages(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "conversationId"
    )
    val messages: List<MessageEntity>
)

/**
 * Relation complète : Conversation avec participants et messages.
 * Combine toutes les relations pour une vue complète.
 */
data class FullConversation(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        entity = UserProfileEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ConversationParticipantEntity::class,
            parentColumn = "conversationId",
            entityColumn = "participantId"
        )
    )
    val participants: List<UserProfileEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "conversationId"
    )
    val messages: List<MessageEntity>
)

/**
 * Relation : Utilisateur avec ses thèmes personnalisés.
 */
data class UserWithCustomThemes(
    @Embedded val user: UserProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val customThemes: List<CustomThemeEntity>
)

/**
 * Amitié avec les profils des deux utilisateurs.
 */
data class FriendshipWithProfiles(
    @Embedded val friendship: FriendshipEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val sender: UserProfileEntity?,
    @Relation(
        parentColumn = "friendId",
        entityColumn = "id"
    )
    val receiver: UserProfileEntity?
)
