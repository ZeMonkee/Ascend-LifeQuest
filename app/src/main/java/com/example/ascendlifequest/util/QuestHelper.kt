package com.example.ascendlifequest.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.QuestStateEntity
import kotlinx.coroutines.runBlocking

object QuestHelper {

    private const val PREFS_NAME = "quest_preferences"
    private const val QUEST_COUNTER_KEY = "quest_counter"
    private const val MAX_QUESTS = 5

    private fun getGlobalPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Compteur global (reste en SharedPreferences pour simplicité)
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
        val db = AppDatabase.getDatabase(context)
        runBlocking {
            db.questStateDao().upsert(QuestStateEntity(userId = userId, questId = questId, isDone = isValid))
        }
    }

    fun getQuestState(context: Context, userId: String, questId: Int): Boolean {
        val db = AppDatabase.getDatabase(context)
        return runBlocking {
            db.questStateDao().getState(userId, questId) ?: false
        }
    }

    fun getCompletedQuestsCount(context: Context, userId: String, questIds: List<Int>): Int {
        val db = AppDatabase.getDatabase(context)
        return runBlocking {
            db.questStateDao().countCompletedForUser(userId, questIds)
        }
    }

    fun clearQuest(context: Context, userId: String) {
        val db = AppDatabase.getDatabase(context)
        runBlocking {
            db.questStateDao().clearForUser(userId)
        }
    }
}