package com.example.ascendlifequest.data.local

import android.content.Context
import android.content.SharedPreferences

object PreferencesHelper {
    private const val PREFS_NAME = "user_preferences"

    const val KEY_SPORT = "sport_preference"
    const val KEY_CUISINE = "cuisine_preference"
    const val KEY_JEUX_VIDEO = "jeux_video_preference"
    const val KEY_LECTURE = "lecture_preference"

    private fun getSharedPreferences(context: Context, userId: String): SharedPreferences {
        // Créer un nom de préférence unique par utilisateur
        val prefsName = "PREFS_NAME_$userId"
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun savePreference(context: Context, userId: String, key: String, value: Int) {
        val prefs = getSharedPreferences(context, userId)
        with(prefs.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun getPreference(context: Context, userId: String, key: String, defaultValue: Int): Int {
        val prefs = getSharedPreferences(context, userId)
        return prefs.getInt(key, defaultValue)
    }

    fun clearPreferences(context: Context, userId: String) {
        val prefs = getSharedPreferences(context, userId)
        prefs.edit().clear().apply() // Efface les données de cet utilisateur
    }
}