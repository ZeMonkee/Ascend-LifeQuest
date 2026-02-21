package com.example.ascendlifequest.database.dao

import androidx.room.*
import com.example.ascendlifequest.database.entities.ConversationEntity
import com.example.ascendlifequest.database.entities.ConversationParticipantEntity
import com.example.ascendlifequest.database.entities.ConversationWithMessages
import com.example.ascendlifequest.database.entities.ConversationWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParticipant(participant: ConversationParticipantEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Query("""
        SELECT c.* FROM conversations c
        INNER JOIN conversation_participants cp ON c.id = cp.conversationId
        WHERE cp.participantId = :userId
        ORDER BY c.lastMessageTimestampSeconds DESC
    """)
    fun observeConversationsForUser(userId: String): Flow<List<ConversationEntity>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationWithParticipants(conversationId: String): ConversationWithParticipants?

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationWithMessages(conversationId: String): ConversationWithMessages?
}
