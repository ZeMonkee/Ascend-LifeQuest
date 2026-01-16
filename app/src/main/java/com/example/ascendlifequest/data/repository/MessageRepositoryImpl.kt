package com.example.ascendlifequest.data.repository

import android.util.Log
import com.example.ascendlifequest.data.model.Conversation
import com.example.ascendlifequest.data.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MessageRepository {

    companion object {
        private const val TAG = "MessageRepository"
        private const val COLLECTION_CONVERSATIONS = "conversations"
        private const val COLLECTION_MESSAGES = "messages"
    }

    private val conversationsCollection = firestore.collection(COLLECTION_CONVERSATIONS)
    private val messagesCollection = firestore.collection(COLLECTION_MESSAGES)

    override suspend fun getOrCreateConversation(userId1: String, userId2: String): Result<Conversation> {
        return try {
            // Créer un ID unique pour la conversation (toujours le même ordre pour les deux utilisateurs)
            val sortedIds = listOf(userId1, userId2).sorted()
            val conversationId = "${sortedIds[0]}_${sortedIds[1]}"

            // Vérifier si la conversation existe
            val existingDoc = conversationsCollection.document(conversationId).get().await()

            if (existingDoc.exists()) {
                val conversation = existingDoc.toObject(Conversation::class.java)
                    ?: return Result.failure(Exception("Erreur de désérialisation"))
                Log.d(TAG, "Conversation existante trouvée: $conversationId")
                return Result.success(conversation.copy(id = conversationId))
            }

            // Créer une nouvelle conversation
            val newConversation = Conversation(
                id = conversationId,
                participants = sortedIds,
                lastMessage = "",
                lastMessageSenderId = "",
                lastMessageTimestamp = Timestamp.now(),
                createdAt = Timestamp.now(),
                unreadCount = mapOf(userId1 to 0, userId2 to 0)
            )

            conversationsCollection.document(conversationId).set(newConversation).await()
            Log.d(TAG, "Nouvelle conversation créée: $conversationId")
            Result.success(newConversation)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création/récupération de conversation", e)
            Result.failure(e)
        }
    }

    override suspend fun getConversationById(conversationId: String): Result<Conversation?> {
        return try {
            val doc = conversationsCollection.document(conversationId).get().await()
            if (doc.exists()) {
                val conversation = doc.toObject(Conversation::class.java)
                Result.success(conversation?.copy(id = conversationId))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération de conversation", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserConversations(userId: String): Result<List<Conversation>> {
        return try {
            val conversations = conversationsCollection
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val result = conversations.documents.mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.copy(id = doc.id)
            }

            Log.d(TAG, "Récupéré ${result.size} conversations pour $userId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des conversations", e)
            Result.failure(e)
        }
    }

    override fun observeUserConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listenerRegistration = conversationsCollection
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'observation des conversations", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                    }
                    trySend(conversations)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String
    ): Result<Message> {
        return try {
            val timestamp = Timestamp.now()

            // Créer le message
            val message = Message(
                conversationId = conversationId,
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                timestamp = timestamp,
                isRead = false,
                type = "text"
            )

            // Ajouter le message
            val messageRef = messagesCollection.add(message).await()
            val savedMessage = message.copy(id = messageRef.id)

            // Mettre à jour la conversation
            val conversationRef = conversationsCollection.document(conversationId)

            firestore.runTransaction { transaction ->
                val conversationDoc = transaction.get(conversationRef)
                @Suppress("UNCHECKED_CAST")
                val currentUnreadCount = conversationDoc.get("unreadCount") as? Map<String, Long> ?: emptyMap()

                // Incrémenter le compteur de non-lus pour le destinataire
                val newUnreadCount = currentUnreadCount.toMutableMap()
                val currentCount = (newUnreadCount[receiverId] ?: 0L)
                newUnreadCount[receiverId] = currentCount + 1L

                transaction.update(conversationRef, mapOf(
                    "lastMessage" to content,
                    "lastMessageSenderId" to senderId,
                    "lastMessageTimestamp" to timestamp,
                    "unreadCount" to newUnreadCount
                ))
            }.await()

            Log.d(TAG, "Message envoyé dans $conversationId")
            Result.success(savedMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'envoi du message", e)
            Result.failure(e)
        }
    }

    override suspend fun getMessages(conversationId: String, limit: Int): Result<List<Message>> {
        return try {
            val messages = messagesCollection
                .whereEqualTo("conversationId", conversationId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val result = messages.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            }

            Log.d(TAG, "Récupéré ${result.size} messages pour $conversationId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des messages", e)
            Result.failure(e)
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listenerRegistration = messagesCollection
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'observation des messages", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    }
                    trySend(messages)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            // Marquer tous les messages non lus comme lus
            val unreadMessages = messagesCollection
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            // Réinitialiser le compteur de non-lus dans la conversation
            conversationsCollection.document(conversationId)
                .update("unreadCount.$userId", 0)
                .await()

            Log.d(TAG, "Messages marqués comme lus pour $userId dans $conversationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du marquage des messages comme lus", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            // Supprimer tous les messages de la conversation
            val messages = messagesCollection
                .whereEqualTo("conversationId", conversationId)
                .get()
                .await()

            val batch = firestore.batch()
            messages.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Supprimer la conversation
            batch.delete(conversationsCollection.document(conversationId))

            batch.commit().await()

            Log.d(TAG, "Conversation supprimée: $conversationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de la conversation", e)
            Result.failure(e)
        }
    }
}

