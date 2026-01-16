package com.example.ascendlifequest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Représente une conversation entre deux utilisateurs
 * Stockée dans la collection "conversations" de Firestore
 */
data class Conversation(
    @DocumentId
    val id: String = "",

    /**
     * Liste des UIDs des participants (toujours 2 pour une conversation privée)
     */
    @get:PropertyName("participants")
    @set:PropertyName("participants")
    var participants: List<String> = emptyList(),

    /**
     * Dernier message de la conversation (pour affichage dans la liste)
     */
    @get:PropertyName("lastMessage")
    @set:PropertyName("lastMessage")
    var lastMessage: String = "",

    /**
     * UID de l'expéditeur du dernier message
     */
    @get:PropertyName("lastMessageSenderId")
    @set:PropertyName("lastMessageSenderId")
    var lastMessageSenderId: String = "",

    /**
     * Timestamp du dernier message
     */
    @get:PropertyName("lastMessageTimestamp")
    @set:PropertyName("lastMessageTimestamp")
    var lastMessageTimestamp: Timestamp = Timestamp.now(),

    /**
     * Date de création de la conversation
     */
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp = Timestamp.now(),

    /**
     * Nombre de messages non lus par participant (Map<userId, count>)
     */
    @get:PropertyName("unreadCount")
    @set:PropertyName("unreadCount")
    var unreadCount: Map<String, Int> = emptyMap()
) {
    constructor() : this("", emptyList(), "", "", Timestamp.now(), Timestamp.now(), emptyMap())

    /**
     * Obtient l'UID de l'autre participant
     */
    fun getOtherParticipantId(currentUserId: String): String {
        return participants.firstOrNull { it != currentUserId } ?: ""
    }
}

