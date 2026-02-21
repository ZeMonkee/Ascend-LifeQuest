package com.example.ascendlifequest.database.dao

import androidx.room.*
import com.example.ascendlifequest.database.entities.UserProfileEntity
import com.example.ascendlifequest.database.entities.UserWithFriends
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    // ===== INSERT/UPDATE =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfileEntity>)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    // ===== DELETE =====

    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteProfileById(userId: String)

    @Query("DELETE FROM user_profiles WHERE isCurrentUser = 0")
    suspend fun deleteAllExceptCurrentUser()

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAll()

    // ===== QUERIES =====

    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    suspend fun getProfileById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE uid = :uid")
    suspend fun getProfileByUid(uid: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY xp DESC LIMIT :limit")
    suspend fun getLeaderboard(limit: Int): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles ORDER BY xp DESC LIMIT :limit")
    fun observeLeaderboard(limit: Int): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE LOWER(pseudo) LIKE LOWER(:query) || '%' LIMIT :limit")
    suspend fun searchByPseudo(query: String, limit: Int = 20): List<UserProfileEntity>

    // ===== RELATIONS =====

    @Transaction
    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    suspend fun getUserWithFriends(userId: String): UserWithFriends?

    // ===== SYNC =====

    @Query("SELECT lastSyncTimestamp FROM user_profiles WHERE id = :userId")
    suspend fun getLastSyncTimestamp(userId: String): Long?

    @Query("UPDATE user_profiles SET lastSyncTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateSyncTimestamp(userId: String, timestamp: Long)
}
