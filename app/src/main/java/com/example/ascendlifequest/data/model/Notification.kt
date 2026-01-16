package com.example.ascendlifequest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Représente une notification utilisateur
 * Stockée dans la collection "notification" de Firestore
 */
data class Notification(
    @DocumentId
    val id: String = "",

    /**
     * UID de l'utilisateur qui reçoit la notification
     */
    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    /**
     * Type de notification: "friend_request_declined", "friend_request_accepted", etc.
     */
    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "",

    /**
     * Message de la notification
     */
    @get:PropertyName("message")
    @set:PropertyName("message")
    var message: String = "",

    /**
     * UID de l'utilisateur concerné (celui qui a refusé/accepté)
     */
    @get:PropertyName("fromUserId")
    @set:PropertyName("fromUserId")
    var fromUserId: String = "",

    /**
     * Pseudo de l'utilisateur concerné
     */
    @get:PropertyName("fromUserPseudo")
    @set:PropertyName("fromUserPseudo")
    var fromUserPseudo: String = "",

    /**
     * Date de création de la notification
     */
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp = Timestamp.now(),

    /**
     * Si la notification a été lue
     */
    @get:PropertyName("read")
    @set:PropertyName("read")
    var read: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", Timestamp.now(), false)

    companion object {
        const val TYPE_FRIEND_REQUEST_DECLINED = "friend_request_declined"
        const val TYPE_FRIEND_REQUEST_ACCEPTED = "friend_request_accepted"
    }
}

