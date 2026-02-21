package com.example.ascendlifequest.database.dao

import androidx.room.*
import com.example.ascendlifequest.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestampSeconds ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestampSeconds ASC")
    fun observeMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE syncStatus = :status")
    suspend fun getMessagesBySyncStatus(status: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE isSentLocally = 1 AND syncStatus = 0")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Query("UPDATE messages SET syncStatus = :status WHERE id = :messageId")
    suspend fun updateSyncStatus(messageId: String, status: Int)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND isRead = 0 AND senderId != :currentUserId")
    suspend fun getUnreadCount(conversationId: String, currentUserId: String): Int

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :currentUserId")
    suspend fun markConversationAsRead(conversationId: String, currentUserId: String)
}
