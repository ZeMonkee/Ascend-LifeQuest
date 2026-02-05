package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.QuestEntity
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class QuestGeneratorRepositoryImpl(private val context: Context) : QuestGeneratorRepository {

    companion object {
        private const val TAG = "QuestGeneratorRepository"
    }

    private val questDao by lazy { AppDatabase.getDatabase(context).questDao() }

    override suspend fun getNextQuestIdFromRoom(): Int {
        return try {
            val maxId = questDao.getMaxId() ?: 999
            maxId + 1
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération de l'ID depuis Room", e)
            (System.currentTimeMillis() / 1000).toInt()
        }
    }

    override suspend fun generateQuestForCategory(category: Categorie, userPreference: Int): Quest? =
            withContext(Dispatchers.IO) {
                try {
                    val preferenceText = when (userPreference) {
                        1 -> "L'utilisateur n'aime pas vraiment cette catégorie. Génère une quête simple et rapide."
                        2 -> "L'utilisateur aime peu cette catégorie. Génère une quête assez simple."
                        3 -> "L'utilisateur a un intérêt neutre pour cette catégorie. Génère une quête standard."
                        4 -> "L'utilisateur aime cette catégorie. Génère une quête intéressante et engageante."
                        5 -> "L'utilisateur adore cette catégorie. Génère une quête passionnante et stimulante."
                        else -> "Génère une quête standard."
                    }

                    val promptText =
                            """
                Génère une quête pour la catégorie « ${category.nom} ». 
                $preferenceText
                Format de réponse obligatoire (ne mets rien d'autre que ces 5 lignes) :
                [1] Nom de la quête (ça doit être court et contenir la tache et la quantité si il y en une)
                [2] Description courte
                [3] Temps en minutes (nombre uniquement, ex: 15)
                [4] XP rapportée (nombre uniquement, ex: 100)
                [5] "oui" ou "non" (dépendance météo)
            """.trimIndent()

                    // Construction JSON pour Ollama
                    val json = JSONObject().apply {
                        put("model", OLLAMA_MODEL)
                        put("prompt", promptText)
                        put("stream", false)  // Désactiver le streaming pour obtenir la réponse complète
                    }

                    val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

                    val request =
                            Request.Builder()
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

                    Log.d(TAG, "Ollama Response: $respBody")

                    val respJson = JSONObject(respBody)

                    // Vérification d'erreur Ollama
                    if (respJson.has("error")) {
                        Log.e(TAG, "Erreur API Ollama : ${respJson.getString("error")}")
                        return@withContext null
                    }

                    // Extraction de la réponse Ollama
                    val text = respJson.optString("response", "")
                    if (text.isBlank()) {
                        Log.e(TAG, "Pas de réponse générée par Ollama.")
                        return@withContext null
                    }


                    val parts = text.trim().split("\n").filter { it.isNotBlank() }

                    val nomGenere =
                            parts.getOrNull(0)?.replace("[1]", "")?.trim()
                                    ?: "Quête ${category.nom}"
                    val descGenere =
                            parts.getOrNull(1)?.replace("[2]", "")?.trim()
                                    ?: "Description indisponible"
                    val tempsString = parts.getOrNull(2)?.replace("[3]", "") ?: "10"
                    val xpString = parts.getOrNull(3)?.replace("[4]", "") ?: "100"
                    val meteoString = parts.getOrNull(4) ?: "non"

                    // RÉCUPÉRATION DE L'ID DEPUIS ROOM
                    val newId = getNextQuestIdFromRoom()

                    val quest =
                            Quest(
                                    id = newId,
                                    categorie = category.id,
                                    nom = nomGenere,
                                    description = descGenere,
                                    preferenceRequis = 0,
                                    xpRapporte = xpString.filter { it.isDigit() }.toIntOrNull()
                                                    ?: 100,
                                    tempsNecessaire =
                                            (tempsString.filter { it.isDigit() }.toIntOrNull()
                                                            ?: 10)
                                                    .minutes,
                                    dependantMeteo = meteoString.contains("oui", ignoreCase = true)
                            )

                    // SAUVEGARDE DANS ROOM (local - source principale)
                    questDao.insertQuest(QuestEntity.fromQuest(quest))
                    Log.d(TAG, "Saved Quest in Room ID: ${quest.id}")

                    return@withContext quest
                } catch (e: Exception) {
                    Log.e(TAG, "Crash :", e)
                    return@withContext null
                }
            }
}
