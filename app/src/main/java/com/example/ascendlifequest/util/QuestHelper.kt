package com.example.ascendlifequest.util

import android.content.Context
import android.content.SharedPreferences

object QuestHelper {

    private const val PREFS_NAME = "quest_preferences"
    private fun getQuestKey(userId: String, questId: Int) = "quest_${userId}_$questId"

    private fun getSharedPreferences(context: Context, userId: String): SharedPreferences {
        val prefsName = "PREFS_NAME_$userId"
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun saveQuestState(context: Context, userId: String, questId: Int, isValid: Boolean) {
        val prefs = getSharedPreferences(context, userId)
        with(prefs.edit()) {
            putBoolean(getQuestKey(userId, questId), isValid)
            apply()
        }
    }

    fun getQuestState(context: Context, userId: String, questId: Int): Boolean {
        val prefs = getSharedPreferences(context, userId)
        return prefs.getBoolean(getQuestKey(userId, questId), false) // false est la valeur par défaut
    }

    fun clearQuest(context: Context, userId: String) {
        val prefs = getSharedPreferences(context, userId)
        prefs.edit().clear().apply() // Efface toutes les données de cet utilisateur
    }
}