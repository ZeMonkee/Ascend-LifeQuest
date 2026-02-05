package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.QuestEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

// Configuration Ollama
// Pour serveur distant via tunnel SSH : ssh -L 11434:localhost:11434 p2300557@iutbg-skynet.iutbourg.univ-lyon1.fr
// Puis utilisez "http://10.0.2.2:11434" (émulateur) ou "http://127.0.0.1:11434" (appareil physique avec adb reverse)
// Pour appareil physique sans tunnel : utilisez directement l'IP du serveur si accessible
private const val OLLAMA_BASE_URL = "http://10.0.2.2:11434"
private const val OLLAMA_MODEL = "llama3.3:latest"  // Modèle Llama 3.3

suspend fun getNextQuestIdFromRoom(context: Context): Int {
    return try {
        val questDao = AppDatabase.getDatabase(context).questDao()
        val maxId = questDao.getMaxId() ?: 999
        maxId + 1
    } catch (e: Exception) {
        Log.e("QuestRepository", "Erreur lors de la récupération de l'ID depuis Room", e)
        (System.currentTimeMillis() / 1000).toInt()
    }
}

suspend fun generateQuestForCategory(context: Context, category: Categorie): Quest? = withContext(Dispatchers.IO) {
    try {
        val promptText = """
            Génère une quête pour la catégorie « ${category.nom} ». 
            Format de réponse obligatoire (ne mets rien d'autre que ces 5 lignes) :
            1️⃣ Nom de la quête (ça doit être court et contenir la tache et la quantité si il y en une)
            2️⃣ Description courte
            3️⃣ Temps en minutes (nombre uniquement, ex: 15)
            4️⃣ XP rapportée (nombre uniquement, ex: 100)
            5️⃣ "oui" ou "non" (dépendance météo)
        """.trimIndent()

        // Construction JSON pour Ollama
        val json = JSONObject().apply {
            put("model", OLLAMA_MODEL)
            put("prompt", promptText)
            put("stream", false)  // Désactiver le streaming pour obtenir la réponse complète
        }

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$OLLAMA_BASE_URL/api/generate")
            .post(body)
            .build()

        // Client avec timeout plus long pour Ollama (la génération locale peut prendre du temps)
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val response = client.newCall(request).execute()
        val respBody = response.body?.string() ?: return@withContext null

        Log.d("QuestRepository", "Ollama Response: $respBody")

        val respJson = JSONObject(respBody)

        // Vérification d'erreur Ollama
        if (respJson.has("error")) {
            Log.e("QuestRepository", "❌ Erreur API Ollama : ${respJson.getString("error")}")
            return@withContext null
        }

        // Extraction de la réponse Ollama
        val text = respJson.optString("response", "")
        if (text.isBlank()) {
            Log.e("QuestRepository", "❌ Pas de réponse générée par Ollama.")
            return@withContext null
        }


        val parts = text.trim().split("\n").filter { it.isNotBlank() }

        val nomGenere = parts.getOrNull(0)?.replace("1️⃣", "")?.trim() ?: "Quête ${category.nom}"
        val descGenere = parts.getOrNull(1)?.replace("2️⃣", "")?.trim() ?: "Description indisponible"
        val tempsString = parts.getOrNull(2)?.replace("3️⃣", "") ?: "10"
        val xpString = parts.getOrNull(3)?.replace("4️⃣", "") ?: "100"
        val meteoString = parts.getOrNull(4) ?: "non"

        // 🔥 RÉCUPÉRATION DE L'ID DEPUIS ROOM
        val newId = getNextQuestIdFromRoom(context)

        val quest = Quest(
            id = newId,
            categorie = category.id,
            nom = nomGenere,
            description = descGenere,
            preferenceRequis = 0,
            xpRapporte = xpString.filter { it.isDigit() }.toIntOrNull() ?: 100,
            tempsNecessaire = (tempsString.filter { it.isDigit() }.toIntOrNull() ?: 10).minutes,
            dependantMeteo = meteoString.contains("oui", ignoreCase = true)
        )

        // 🔥 SAUVEGARDE DANS ROOM (local - source principale)
        val questDao = AppDatabase.getDatabase(context).questDao()
        questDao.insertQuest(QuestEntity.fromQuest(quest))
        Log.d("QuestRepository", "✅ Saved Quest in Room ID: ${quest.id}")

        return@withContext quest

    } catch (e: Exception) {
        Log.e("QuestRepository", "❌ Crash :", e)
        return@withContext null
    }
}