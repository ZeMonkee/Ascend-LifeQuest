package com.example.ascendlifequest.data.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration.Companion.minutes

class QuestRepository {
    private val db = FirebaseFirestore.getInstance()
    private val questCollection = db.collection("quest")
    private val categorieCollection = db.collection("categories")

    suspend fun getCategories(): List<Categorie> {
        return try {
            val snapshot = categorieCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val colorInt = doc.getLong("color")?.toInt() ?: 0
                Categorie(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    nom = doc.getString("nom") ?: "",
                    logo = doc.getLong("icon")?.toInt() ?: 0,
                    couleur = Color(colorInt) // ✅ conversion Int → Color
                )
            }
        } catch (e: Exception) {
            Log.e("QuestRepository", "Erreur lors du chargement des catégories", e)
            emptyList()
        }
    }


    suspend fun getQuests(): List<Quest> {
        return try {
            val snapshot = questCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val minutes = doc.getLong("tempsNecessaire")?.toInt() ?: 0
                Quest(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    categorie = doc.getLong("categorie")?.toInt() ?: 0,
                    nom = doc.getString("nom") ?: "",
                    description = doc.getString("description") ?: "",
                    preferenceRequis = doc.getLong("preferenceRequis")?.toInt() ?: 0,
                    xpRapporte = doc.getLong("xpRapporte")?.toInt() ?: 0,
                    tempsNecessaire = minutes.minutes,
                    dependantMeteo = doc.getBoolean("dependantMeteo") ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("QuestRepository", "Erreur lors du chargement des quêtes", e)
            emptyList()
        }
    }
}


