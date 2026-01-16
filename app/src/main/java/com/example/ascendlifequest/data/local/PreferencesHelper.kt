package com.example.ascendlifequest.data.local

import android.content.Context
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.CategoryPreferenceEntity
import kotlinx.coroutines.runBlocking

object PreferencesHelper {
    private const val PREFS_NAME = "user_preferences"

    const val KEY_SPORT = "sport_preference"
    const val KEY_CUISINE = "cuisine_preference"
    const val KEY_JEUX_VIDEO = "jeux_video_preference"
    const val KEY_LECTURE = "lecture_preference"

    // Mapping entre les IDs de catégorie et les clés de préférences
    private val categoryToPreferenceKey = mapOf(
        1 to KEY_SPORT,       // Sport
        2 to KEY_CUISINE,     // Cuisine
        3 to KEY_JEUX_VIDEO,  // Jeux Vidéo
        4 to KEY_LECTURE      // Lecture
    )

    fun savePreference(context: Context, userId: String, key: String, value: Int) {
        val db = AppDatabase.getDatabase(context)
        val categoryId = categoryToPreferenceKey.entries.firstOrNull { it.value == key }?.key ?: -1
        runBlocking {
            db.categoryPreferenceDao().upsert(CategoryPreferenceEntity(userId = userId, categoryId = categoryId, preference = value))
        }
    }

    fun getPreference(context: Context, userId: String, key: String, defaultValue: Int): Int {
        val db = AppDatabase.getDatabase(context)
        val categoryId = categoryToPreferenceKey.entries.firstOrNull { it.value == key }?.key
        if (categoryId != null) {
            val roomVal = runBlocking { db.categoryPreferenceDao().getPreferenceForCategory(userId, categoryId) }
            return roomVal ?: defaultValue
        }
        return defaultValue
    }

    /**
     * Récupère toutes les préférences utilisateur sous forme de Map (categoryId -> preference)
     */
    fun getAllPreferences(context: Context, userId: String): Map<Int, Int> {
        val db = AppDatabase.getDatabase(context)
        val list = runBlocking { db.categoryPreferenceDao().getAllPreferencesForUser(userId) }
        if (list.isEmpty()) {
            // Default values
            return mapOf(
                1 to 3,
                2 to 3,
                3 to 3,
                4 to 3
            )
        }
        return list.associate { it.categoryId to it.preference }
    }

    fun clearPreferences(context: Context, userId: String) {
        val db = AppDatabase.getDatabase(context)
        runBlocking { db.categoryPreferenceDao().clearPreferencesForUser(userId) }
    }
}