package com.example.ascendlifequest.data.repository

import android.content.Context
import android.util.Log
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.database.AppDatabase
import com.example.ascendlifequest.database.QuestEntity
import com.example.ascendlifequest.ui.theme.AppColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuestRepositoryImpl(private val context: Context) : QuestRepository {

    companion object {
        private const val TAG = "QuestRepository"
    }

    // Room Database - initialisation lazy pour éviter les problèmes de contexte
    private val questDao by lazy { AppDatabase.getDatabase(context).questDao() }

    override fun getCategories(): List<Categorie> {
        return listOf(
                Categorie(1, "Sport", R.drawable.icon_sport, AppColor.SportColor),
                Categorie(2, "Cuisine", R.drawable.icon_cuisine, AppColor.CuisineColor),
                Categorie(3, "Jeux Vidéo", R.drawable.icon_jeux_video, AppColor.JeuxVideoColor),
                Categorie(4, "Lecture", R.drawable.icon_lecture, AppColor.LectureColor)
        )
    }

    override suspend fun getQuests(): List<Quest> {
        return try {
            val questEntities = questDao.getAllQuests()
            questEntities.map { it.toQuest() }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du chargement des quêtes depuis Room", e)
            emptyList()
        }
    }

    override fun getQuestsFlow(): Flow<List<Quest>> {
        return questDao.getAllQuestsFlow().map { entities -> entities.map { it.toQuest() } }
    }

    override suspend fun saveQuestLocally(quest: Quest) {
        try {
            questDao.insertQuest(QuestEntity.fromQuest(quest))
            Log.d(TAG, "Quete sauvegardee localement: ${quest.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur sauvegarde locale", e)
        }
    }

    override suspend fun getNextQuestId(): Int {
        return try {
            val maxId = questDao.getMaxId() ?: 999
            maxId + 1
        } catch (e: Exception) {
            Log.e(TAG, "Erreur récupération ID", e)
            (System.currentTimeMillis() / 1000).toInt()
        }
    }

    override suspend fun clearAllQuests() {
        try {
            questDao.deleteAllQuests()
            Log.d(TAG, "Toutes les quetes ont ete supprimees de Room")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression des quetes de Room", e)
        }
    }
}
