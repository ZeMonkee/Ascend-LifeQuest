package com.example.ascendlifequest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Représente un message dans une conversation
 * Stockée dans la collection "messages" de Firestore
 */
data class Message(
    @DocumentId
    val id: String = "",

    /**
     * ID de la conversation à laquelle appartient ce message
     */
    @get:PropertyName("conversationId")
    @set:PropertyName("conversationId")
    var conversationId: String = "",

    /**
     * UID de l'expéditeur
     */
    @get:PropertyName("senderId")
    @set:PropertyName("senderId")
    var senderId: String = "",

    /**
     * UID du destinataire
     */
    @get:PropertyName("receiverId")
    @set:PropertyName("receiverId")
    var receiverId: String = "",

    /**
     * Contenu du message
     */
    @get:PropertyName("content")
    @set:PropertyName("content")
    var content: String = "",

    /**
     * Timestamp d'envoi
     */
    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Timestamp = Timestamp.now(),

    /**
     * Le message a-t-il été lu?
     */
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,

    /**
     * Type de message: "text", "image", etc. (pour extension future)
     */
    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "text"
) {
    constructor() : this("", "", "", "", "", Timestamp.now(), false, "text")
}

