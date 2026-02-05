package com.example.ascendlifequest.data.repository

import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest

// Configuration Ollama
// Pour serveur distant via tunnel SSH : ssh -L 11434:localhost:11434 p2300557@iutbg-skynet.iutbourg.univ-lyon1.fr
// Puis utilisez "http://10.0.2.2:11434" (émulateur) ou "http://127.0.0.1:11434" (appareil physique avec adb reverse)
const val OLLAMA_BASE_URL = "http://10.0.2.2:11434"
const val OLLAMA_MODEL = "llama3.3:latest"  // Modèle Llama 3.3

interface QuestGeneratorRepository {
    suspend fun getNextQuestIdFromRoom(): Int
    suspend fun generateQuestForCategory(category: Categorie, userPreference: Int = 3): Quest?
}