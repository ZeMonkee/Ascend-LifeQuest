package com.example.ascendlifequest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: QuestStateEntity)

    @Query("SELECT isDone FROM quest_states WHERE userId = :userId AND questId = :questId LIMIT 1")
    suspend fun getState(userId: String, questId: Int): Boolean?

    @Query("SELECT COUNT(*) FROM quest_states WHERE userId = :userId AND isDone = 1 AND questId IN (:questIds)")
    suspend fun countCompletedForUser(userId: String, questIds: List<Int>): Int

    @Query("DELETE FROM quest_states WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
