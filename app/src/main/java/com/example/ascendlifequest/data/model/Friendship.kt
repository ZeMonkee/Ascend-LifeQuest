package com.example.ascendlifequest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Représente une relation d'amitié entre deux utilisateurs
 * Stockée dans la collection "friendships" de Firestore
 */
data class Friendship(
    @DocumentId
    val id: String = "",

    /**
     * UID de l'utilisateur qui a envoyé la demande d'ami
     */
    @get:PropertyName("userId")
    @set:PropertyName("userId")
    var userId: String = "",

    /**
     * UID de l'ami
     */
    @get:PropertyName("friendId")
    @set:PropertyName("friendId")
    var friendId: String = "",

    /**
     * Date de création de l'amitié
     */
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp = Timestamp.now(),

    /**
     * Statut de l'amitié: "pending", "accepted", "declined"
     */
    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "accepted"
) {
    constructor() : this("", "", "", Timestamp.now(), "accepted")

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"
    }
}

