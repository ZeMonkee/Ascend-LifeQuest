package com.example.ascendlifequest.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.QuestStateEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import androidx.core.content.edit

object QuestHelper {

    private const val PREFS_NAME = "quest_preferences"
    private const val QUEST_COUNTER_KEY = "quest_counter"
    private const val QUEST_USER_ID_KEY = "quest_user_id"
    private const val LAST_GENERATION_DATE_KEY = "last_generation_date"
    private const val MAX_QUESTS = 5
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun getGlobalPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Flag pour la génération initiale (persiste uniquement pour cette session d'app)
    // On utilise un companion object variable pour éviter de persister entre les sessions
    private var initialGenerationDoneThisSession = false

    fun hasInitialGenerationBeenDone(): Boolean {
        return initialGenerationDoneThisSession
    }

    fun markInitialGenerationAsDone() {
        initialGenerationDoneThisSession = true
    }

    fun resetInitialGenerationFlag() {
        initialGenerationDoneThisSession = false
    }

    // Gestion de l'ID utilisateur lié aux quêtes
    fun getQuestUserId(context: Context): String {
        return getGlobalPreferences(context).getString(QUEST_USER_ID_KEY, "") ?: ""
    }

    fun setQuestUserId(context: Context, userId: String) {
        getGlobalPreferences(context).edit().putString(QUEST_USER_ID_KEY, userId).apply()
    }

    /**
     * Vérifie si l'utilisateur actuel est le même que celui qui a généré les quêtes.
     * @return true si l'utilisateur est différent (les quêtes doivent être vidées), false sinon
     */
    fun isUserDifferent(context: Context, currentUserId: String): Boolean {
        val savedUserId = getQuestUserId(context)
        // Si aucun userId n'est sauvegardé, c'est un nouvel utilisateur ou première utilisation
        if (savedUserId.isEmpty()) {
            return false
        }
        return savedUserId != currentUserId
    }

    /** Réinitialise toutes les données des quêtes pour un nouvel utilisateur */
    fun resetForNewUser(context: Context, newUserId: String) {
        resetQuestCounter(context)
        resetInitialGenerationFlag()
        setQuestUserId(context, newUserId)
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
            db.questStateDao()
                    .upsert(QuestStateEntity(userId = userId, questId = questId, isDone = isValid))
        }
    }

    fun getQuestState(context: Context, userId: String, questId: Int): Boolean {
        val db = AppDatabase.getDatabase(context)
        return runBlocking { db.questStateDao().getState(userId, questId) ?: false }
    }

    fun getCompletedQuestsCount(context: Context, userId: String, questIds: List<Int>): Int {
        val db = AppDatabase.getDatabase(context)
        return runBlocking { db.questStateDao().countCompletedForUser(userId, questIds) }
    }

    fun clearQuest(context: Context, userId: String) {
        val db = AppDatabase.getDatabase(context)
        runBlocking { db.questStateDao().clearForUser(userId) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastGenerationDate(context: Context): LocalDate? {
        val dateString = getGlobalPreferences(context).getString(LAST_GENERATION_DATE_KEY, null)
        return dateString?.let {
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLastGenerationDate(context: Context, date: LocalDate) {
        getGlobalPreferences(context)
            .edit {
                putString(LAST_GENERATION_DATE_KEY, date.format(dateFormatter))
                }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isNewDay(context: Context): Boolean {
        val lastDate = getLastGenerationDate(context)
        val today = LocalDate.now()
        return lastDate == null || lastDate.isBefore(today)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun shouldResetQuestsForNewDay(context: Context, userId: String): Boolean {
        if (isNewDay(context)) {
            Log.d("QuestHelper", "Nouveau jour detecte - Reset des quetes necessaire")
            Log.d("QuestHelper", "   Derniere date: ${getLastGenerationDate(context)}")
            Log.d("QuestHelper", "   Aujourd'hui: ${LocalDate.now()}")

            resetQuestCounter(context)
            resetInitialGenerationFlag()
            clearQuest(context, userId)
            setLastGenerationDate(context, LocalDate.now())

            return true
        }
        return false
    }
}
