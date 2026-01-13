package com.example.ascendlifequest.helpers

import android.content.Context
import android.content.SharedPreferences

object QuestHelper {

    private const val PREFS_NAME = "quest_preferences"
    private const val QUEST_COUNTER_KEY = "quest_counter"
    private const val MAX_QUESTS = 5

    private fun getQuestKey(userId: String, questId: Int) = "quest_${userId}_$questId"

    private fun getSharedPreferences(context: Context, userId: String): SharedPreferences {
        val prefsName = "PREFS_NAME_$userId"
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    private fun getGlobalPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 🔥 Compteur de quêtes générées
    fun getQuestCounter(context: Context): Int {
        return getGlobalPreferences(context).getInt(QUEST_COUNTER_KEY, 0)
    }

    fun incrementQuestCounter(context: Context): Int {
        val prefs = getGlobalPreferences(context)
        val currentCount = prefs.getInt(QUEST_COUNTER_KEY, 0)
        val newCount = currentCount + 1
        prefs.edit().putInt(QUEST_COUNTER_KEY, newCount).apply()
        return newCount
    }

    fun resetQuestCounter(context: Context) {
        getGlobalPreferences(context).edit().putInt(QUEST_COUNTER_KEY, 0).apply()
    }

    fun canGenerateMoreQuests(context: Context): Boolean {
        return getQuestCounter(context) < MAX_QUESTS
    }

    fun getMaxQuests(): Int = MAX_QUESTS

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

    // 🔥 Compter les quêtes terminées pour une liste de quêtes
    fun getCompletedQuestsCount(context: Context, userId: String, questIds: List<Int>): Int {
        val prefs = getSharedPreferences(context, userId)
        return questIds.count { questId ->
            prefs.getBoolean(getQuestKey(userId, questId), false)
        }
    }

    fun clearQuest(context: Context, userId: String) {
        val prefs = getSharedPreferences(context, userId)
        prefs.edit().clear().apply() // Efface toutes les données de cet utilisateur
    }
}
