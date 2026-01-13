package com.example.ascendlifequest.data.repository

import android.util.Log
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.time.Duration.Companion.minutes

// ⚠️ REMPLACEZ PAR UNE NOUVELLE CLÉ (L'ancienne est compromise)
private const val API_KEY = "AIzaSyARvmR5zEArUGycAApsmh-Xx1h0F_3YS1Q"
private const val MODEL = "gemini-2.5-flash"

// 🔥 Fonction pour récupérer le prochain ID disponible dans Firestore
suspend fun getNextQuestId(db: FirebaseFirestore): Int {
    return try {
        // On cherche la quête avec l'ID le plus élevé
        val snapshot = db.collection("quest")
            .orderBy("id", Query.Direction.DESCENDING) // Trie du plus grand au plus petit
            .limit(1) // On en prend juste un
            .get()
            .await() // Nécessite l'import kotlinx.coroutines.tasks.await

        if (!snapshot.isEmpty) {
            // Si on trouve une quête, on prend son ID et on ajoute 1
            val lastId = snapshot.documents[0].getLong("id")?.toInt() ?: 1000
            lastId + 1
        } else {
            // Si la base est vide, on commence à 1000
            1000
        }
    } catch (e: Exception) {
        Log.e("QuestRepository", "Erreur lors de la récupération de l'ID", e)
        // En cas d'erreur (ex: index manquant), on génère un ID basé sur le temps pour éviter le crash
        (System.currentTimeMillis() / 1000).toInt()
    }
}

suspend fun generateQuestForCategory(category: Categorie): Quest? = withContext(Dispatchers.IO) {
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

        // Initialisation Firestore
        val db = FirebaseFirestore.getInstance()

        // 🔥 RÉCUPÉRATION DE L'ID DYNAMIQUE
        val newId = getNextQuestId(db)

        val quest = Quest(
            id = newId, // On utilise l'ID calculé
            categorie = category.id,
            nom = nomGenere,
            description = descGenere,
            preferenceRequis = 0,
            xpRapporte = xpString.filter { it.isDigit() }.toIntOrNull() ?: 100,
            tempsNecessaire = (tempsString.filter { it.isDigit() }.toIntOrNull() ?: 10).minutes,
            dependantMeteo = meteoString.contains("oui", ignoreCase = true)
        )

        // Firestore Save
        db.collection("quest")
            .document("quest_${quest.id}")
            .set(mapOf(
                "id" to quest.id,
                "categorie" to quest.categorie,
                "nom" to quest.nom,
                "description" to quest.description,
                "preferenceRequis" to quest.preferenceRequis,
                "xpRapporte" to quest.xpRapporte,
                "tempsNecessaire" to quest.tempsNecessaire.inWholeMinutes,
                "dependantMeteo" to quest.dependantMeteo
            ))
            .addOnSuccessListener { Log.d("QuestRepository", "✅ Saved Quest ID: ${quest.id}") }
            .addOnFailureListener { Log.e("QuestRepository", "❌ Firestore failed", it) }

        return@withContext quest

    } catch (e: Exception) {
        Log.e("QuestRepository", "❌ Crash :", e)
        return@withContext null
    }
}