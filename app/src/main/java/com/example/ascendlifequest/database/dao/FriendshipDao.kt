package com.example.ascendlifequest.database.dao

import androidx.room.*
import com.example.ascendlifequest.database.entities.FriendshipEntity
import com.example.ascendlifequest.database.entities.FriendshipWithProfiles
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendshipDao {

    // ===== INSERT/UPDATE =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: FriendshipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendships(friendships: List<FriendshipEntity>)

    @Update
    suspend fun updateFriendship(friendship: FriendshipEntity)

    // ===== DELETE =====

    @Delete
    suspend fun deleteFriendship(friendship: FriendshipEntity)

    @Query("DELETE FROM friendships WHERE id = :friendshipId")
    suspend fun deleteFriendshipById(friendshipId: String)

    @Query("DELETE FROM friendships WHERE userId = :userId OR friendId = :userId")
    suspend fun deleteAllFriendshipsForUser(userId: String)

    @Query("DELETE FROM friendships")
    suspend fun deleteAll()

    // ===== QUERIES =====

    @Query("SELECT * FROM friendships WHERE id = :friendshipId")
    suspend fun getFriendshipById(friendshipId: String): FriendshipEntity?

    @Query("SELECT * FROM friendships WHERE userId = :userId AND friendId = :friendId")
    suspend fun getFriendship(userId: String, friendId: String): FriendshipEntity?

    @Query("SELECT * FROM friendships WHERE (userId = :userId OR friendId = :userId) AND status = 'accepted'")
    suspend fun getAcceptedFriendships(userId: String): List<FriendshipEntity>

    @Query("SELECT * FROM friendships WHERE status = 'accepted'")
    suspend fun getAllAcceptedFriendships(): List<FriendshipEntity>

    @Query("SELECT * FROM friendships WHERE (userId = :userId OR friendId = :userId) AND status = 'accepted'")
    fun observeAcceptedFriendships(userId: String): Flow<List<FriendshipEntity>>

    @Query("SELECT * FROM friendships WHERE friendId = :userId AND status = 'pending'")
    suspend fun getPendingRequests(userId: String): List<FriendshipEntity>

    @Query("SELECT * FROM friendships WHERE friendId = :userId AND status = 'pending'")
    fun observePendingRequests(userId: String): Flow<List<FriendshipEntity>>

    @Query("SELECT * FROM friendships WHERE userId = :userId AND status = 'pending'")
    suspend fun getSentRequests(userId: String): List<FriendshipEntity>

    // ===== RELATIONS =====

    @Transaction
    @Query("SELECT * FROM friendships WHERE (userId = :userId OR friendId = :userId) AND status = 'accepted'")
    suspend fun getFriendshipsWithProfiles(userId: String): List<FriendshipWithProfiles>

    @Transaction
    @Query("SELECT * FROM friendships WHERE friendId = :userId AND status = 'pending'")
    suspend fun getPendingRequestsWithProfiles(userId: String): List<FriendshipWithProfiles>

    // ===== HELPER =====

    @Query("SELECT COUNT(*) FROM friendships WHERE friendId = :userId AND status = 'pending'")
    suspend fun getPendingRequestsCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM friendships WHERE friendId = :userId AND status = 'pending'")
    fun observePendingRequestsCount(userId: String): Flow<Int>
}
