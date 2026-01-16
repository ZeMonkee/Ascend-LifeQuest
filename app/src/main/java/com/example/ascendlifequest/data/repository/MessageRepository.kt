package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Conversation
import com.example.ascendlifequest.data.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    /**
     * Récupère ou crée une conversation entre deux utilisateurs
     */
    suspend fun getOrCreateConversation(userId1: String, userId2: String): Result<Conversation>

    /**
     * Récupère une conversation par son ID
     */
    suspend fun getConversationById(conversationId: String): Result<Conversation?>

    /**
     * Récupère toutes les conversations d'un utilisateur
     */
    suspend fun getUserConversations(userId: String): Result<List<Conversation>>

    /**
     * Observe les conversations d'un utilisateur en temps réel
     */
    fun observeUserConversations(userId: String): Flow<List<Conversation>>

    /**
     * Envoie un message
     */
    suspend fun sendMessage(conversationId: String, senderId: String, receiverId: String, content: String): Result<Message>

    /**
     * Récupère les messages d'une conversation
     */
    suspend fun getMessages(conversationId: String, limit: Int = 50): Result<List<Message>>

    /**
     * Observe les messages d'une conversation en temps réel
     */
    fun observeMessages(conversationId: String): Flow<List<Message>>

    /**
     * Marque les messages comme lus
     */
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit>

    /**
     * Supprime une conversation
     */
    suspend fun deleteConversation(conversationId: String): Result<Unit>
}

