package com.example.ascendlifequest.model

data class Quest(
    val id: Int,
    val categorie: Int,
    val nom: String,
    val description: String,
    val preferenceRequis: Int,
    val xpRapporte: Int,
    val tempsNecessaire: kotlin.time.Duration,
    val dependantMeteo: Boolean,
    val valider: Boolean = false
) {
    fun getTempsNecessaireMinutes(): Long = tempsNecessaire.inWholeMinutes
}
