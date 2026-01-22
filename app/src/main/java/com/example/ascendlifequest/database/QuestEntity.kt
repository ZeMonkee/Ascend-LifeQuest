package com.example.ascendlifequest.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ascendlifequest.data.model.Quest
import kotlin.time.Duration.Companion.minutes

/**
 * Room entity representing a quest stored in the local database. Provides conversion methods
 * to/from the domain [Quest] model.
 */
@Entity(tableName = "quests")
data class QuestEntity(
        @PrimaryKey val id: Int,
        val categorie: Int,
        val nom: String,
        val description: String,
        val preferenceRequis: Int,
        val xpRapporte: Int,
        val tempsNecessaireMinutes: Long,
        val dependantMeteo: Boolean
) {
    fun toQuest(): Quest {
        return Quest(
                id = id,
                categorie = categorie,
                nom = nom,
                description = description,
                preferenceRequis = preferenceRequis,
                xpRapporte = xpRapporte,
                tempsNecessaire = tempsNecessaireMinutes.minutes,
                dependantMeteo = dependantMeteo
        )
    }

    companion object {
        fun fromQuest(quest: Quest): QuestEntity {
            return QuestEntity(
                    id = quest.id,
                    categorie = quest.categorie,
                    nom = quest.nom,
                    description = quest.description,
                    preferenceRequis = quest.preferenceRequis,
                    xpRapporte = quest.xpRapporte,
                    tempsNecessaireMinutes = quest.tempsNecessaire.inWholeMinutes,
                    dependantMeteo = quest.dependantMeteo
            )
        }
    }
}
