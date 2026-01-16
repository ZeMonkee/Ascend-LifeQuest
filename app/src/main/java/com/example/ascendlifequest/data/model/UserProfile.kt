package com.example.ascendlifequest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Représente le profil utilisateur stocké dans la collection "profile" de Firestore
 */
data class UserProfile(
    @DocumentId
    val id: String = "",

    /**
     * UID Firebase - identifiant unique de l'utilisateur
     * Stocké comme champ pour faciliter les recherches
     */
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = "",

    @get:PropertyName("pseudo")
    @set:PropertyName("pseudo")
    var pseudo: String = "",

    @get:PropertyName("photoUrl")
    @set:PropertyName("photoUrl")
    var photoUrl: String = "",

    @get:PropertyName("xp")
    @set:PropertyName("xp")
    var xp: Long = 0,

    @get:PropertyName("quetesRealisees")
    @set:PropertyName("quetesRealisees")
    var quetesRealisees: Int = 0,

    @get:PropertyName("streak")
    @set:PropertyName("streak")
    var streak: Int = 0,

    @get:PropertyName("dateDeCreation")
    @set:PropertyName("dateDeCreation")
    var dateDeCreation: Timestamp = Timestamp.now(),

    @get:PropertyName("rang")
    @set:PropertyName("rang")
    var rang: Int = 0,

    @get:PropertyName("online")
    @set:PropertyName("online")
    var online: Boolean = false
) {
    // Constructeur vide requis par Firestore
    constructor() : this("", "", "", "", 0, 0, 0, Timestamp.now(), 0, false)

    /**
     * Calcule le niveau basé sur l'XP
     * Formule : niveau = racine carrée de (xp / 100)
     */
    fun calculateLevel(): Int {
        return (kotlin.math.sqrt(xp.toDouble() / 100)).toInt() + 1
    }

    /**
     * Calcule la progression vers le prochain niveau (0.0 à 1.0)
     */
    fun calculateLevelProgress(): Float {
        val currentLevel = calculateLevel()
        val xpForCurrentLevel = ((currentLevel - 1) * (currentLevel - 1)) * 100L
        val xpForNextLevel = (currentLevel * currentLevel) * 100L
        val xpInCurrentLevel = xp - xpForCurrentLevel
        val xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel
        return (xpInCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * XP restante pour le prochain niveau
     */
    fun xpToNextLevel(): Long {
        val currentLevel = calculateLevel()
        val xpForNextLevel = (currentLevel * currentLevel) * 100L
        return xpForNextLevel - xp
    }
}

