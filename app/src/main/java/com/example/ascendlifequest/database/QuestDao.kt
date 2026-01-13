package com.example.ascendlifequest.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests")
    suspend fun getAllQuests(): List<QuestEntity>

    @Query("SELECT * FROM quests")
    fun getAllQuestsFlow(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: Int): QuestEntity?

    @Query("SELECT * FROM quests WHERE categorie = :categorieId")
    suspend fun getQuestsByCategory(categorieId: Int): List<QuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Delete
    suspend fun deleteQuest(quest: QuestEntity)

    @Query("DELETE FROM quests")
    suspend fun deleteAllQuests()

    @Query("SELECT MAX(id) FROM quests")
    suspend fun getMaxId(): Int?
}
