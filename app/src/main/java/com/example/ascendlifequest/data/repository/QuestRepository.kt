package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    /** Récupère la liste des catégories disponibles */
    fun getCategories(): List<Categorie>

    /** Récupère toutes les quêtes depuis Room (local) */
    suspend fun getQuests(): List<Quest>

    /** Flow pour observer les changements de quêtes en temps réel */
    fun getQuestsFlow(): Flow<List<Quest>>

    /** Sauvegarde une quête localement dans Room */
    suspend fun saveQuestLocally(quest: Quest)

    /** Récupère le prochain ID disponible depuis Room */
    suspend fun getNextQuestId(): Int

    /** Vide toutes les quêtes de la base de données */
    suspend fun clearAllQuests()
}
