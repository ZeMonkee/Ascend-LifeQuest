package com.example.ascendlifequest.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pref: CategoryPreferenceEntity)

    @Query("SELECT preference FROM category_preferences WHERE userId = :userId AND categoryId = :categoryId LIMIT 1")
    suspend fun getPreferenceForCategory(userId: String, categoryId: Int): Int?

    @Query("SELECT userId, categoryId, preference FROM category_preferences WHERE userId = :userId")
    suspend fun getAllPreferencesForUser(userId: String): List<CategoryPreferenceEntity>

    @Query("DELETE FROM category_preferences WHERE userId = :userId")
    suspend fun clearPreferencesForUser(userId: String)
}
