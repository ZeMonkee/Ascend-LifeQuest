package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest

interface QuestGeneratorRepository {
    /** Récupère le prochain ID disponible depuis Room */
    suspend fun getNextQuestIdFromRoom(): Int

    /** Génère une quête pour une catégorie donnée en utilisant l'API Gemini */
    suspend fun generateQuestForCategory(category: Categorie): Quest?
}
