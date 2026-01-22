package com.example.ascendlifequest.data.model

import kotlin.time.Duration

/**
 * Represents a quest that users can complete to earn experience points.
 *
 * @property id Unique identifier for the quest
 * @property categorie Category ID this quest belongs to
 * @property nom Display name of the quest
 * @property description Detailed description of the quest objectives
 * @property preferenceRequis Required preference level to unlock this quest
 * @property xpRapporte Experience points awarded upon completion
 * @property tempsNecessaire Estimated time to complete the quest
 * @property dependantMeteo Whether quest availability depends on weather conditions
 * @property valider Whether the quest has been completed
 */
data class Quest(
        val id: Int,
        val categorie: Int,
        val nom: String,
        val description: String,
        val preferenceRequis: Int,
        val xpRapporte: Int,
        val tempsNecessaire: Duration,
        val dependantMeteo: Boolean,
        val valider: Boolean = false
) {
    fun getTempsNecessaireMinutes(): Long = tempsNecessaire.inWholeMinutes
}
