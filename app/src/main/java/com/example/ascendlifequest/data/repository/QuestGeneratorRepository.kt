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
import kotlin.time.Duration.Companion.minutes

private const val API_KEY = "AIzaSyAh017-nxA99l6Taxc7hSjHkQTguyL-63Y"
private const val MODEL = "gemini-2.5-flash"

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

        // Construction JSON
        val textPart = JSONObject().put("text", promptText)
        val partsArray = JSONArray().put(textPart)
        val contentObj = JSONObject().put("parts", partsArray)
        val contentsArray = JSONArray().put(contentObj)
        val json = JSONObject().put("contents", contentsArray)

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$API_KEY")
            .post(body)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val respBody = response.body?.string() ?: return@withContext null

        Log.d("QuestRepository", "Gemini Response: $respBody")

        val respJson = JSONObject(respBody)

        if (respJson.has("error")) {
            Log.e("QuestRepository", "❌ Erreur API Gemini : ${respJson.getJSONObject("error")}")
            return@withContext null
        }

        val candidates = respJson.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            Log.e("QuestRepository", "❌ Pas de candidat généré.")
            return@withContext null
        }

        val text = candidates
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

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