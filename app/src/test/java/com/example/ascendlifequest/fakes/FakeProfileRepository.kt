package com.example.ascendlifequest.fakes

import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of ProfileRepository for unit testing.
 * Stores profiles in memory and allows configuring responses.
 */
class FakeProfileRepository : ProfileRepository {

    private val profiles = mutableMapOf<String, UserProfile>()
    private val profileFlows = mutableMapOf<String, MutableStateFlow<UserProfile?>>()

    var getCurrentUserProfileResult: Result<UserProfile?> = Result.success(null)
    var saveProfileResult: Result<Unit> = Result.success(Unit)
    var updateXpResult: Result<Unit> = Result.success(Unit)
    var incrementQuestsResult: Result<Unit> = Result.success(Unit)
    var decrementQuestsResult: Result<Unit> = Result.success(Unit)
    var updateStreakResult: Result<Unit> = Result.success(Unit)
    var updatePseudoResult: Result<Unit> = Result.success(Unit)
    var leaderboardResult: Result<List<UserProfile>> = Result.success(emptyList())

    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return getCurrentUserProfileResult
    }

    override suspend fun getProfileById(userId: String): Result<UserProfile?> {
        return Result.success(profiles[userId])
    }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        if (saveProfileResult.isSuccess) {
            profiles[profile.uid] = profile
        }
        return saveProfileResult
    }

    override suspend fun createProfileForNewUser(userId: String, email: String): Result<UserProfile> {
        val pseudo = email.substringBefore("@")
        val newProfile = UserProfile(
            id = userId,
            uid = userId,
            pseudo = pseudo,
            xp = 0,
            quetesRealisees = 0,
            streak = 0,
            rang = 1
        )
        profiles[userId] = newProfile
        return Result.success(newProfile)
    }

    override suspend fun updateXp(userId: String, xpToAdd: Long): Result<Unit> {
        if (updateXpResult.isSuccess) {
            profiles[userId]?.let {
                profiles[userId] = it.copy().apply { xp = it.xp + xpToAdd }
            }
        }
        return updateXpResult
    }

    override suspend fun incrementQuestsCompleted(userId: String): Result<Unit> {
        if (incrementQuestsResult.isSuccess) {
            profiles[userId]?.let {
                profiles[userId] = it.copy().apply { quetesRealisees = it.quetesRealisees + 1 }
            }
        }
        return incrementQuestsResult
    }

    override suspend fun decrementQuestsCompleted(userId: String): Result<Unit> {
        if (decrementQuestsResult.isSuccess) {
            profiles[userId]?.let {
                profiles[userId] = it.copy().apply { quetesRealisees = (it.quetesRealisees - 1).coerceAtLeast(0) }
            }
        }
        return decrementQuestsResult
    }

    override suspend fun updateStreak(userId: String, newStreak: Int): Result<Unit> {
        if (updateStreakResult.isSuccess) {
            profiles[userId]?.let {
                profiles[userId] = it.copy().apply { streak = newStreak }
            }
        }
        return updateStreakResult
    }

    override fun observeProfile(userId: String): Flow<UserProfile?> {
        if (!profileFlows.containsKey(userId)) {
            profileFlows[userId] = MutableStateFlow(profiles[userId])
        }
        return profileFlows[userId]!!
    }

    override suspend fun getUserRank(userId: String): Result<Int> {
        val sortedProfiles = profiles.values.sortedByDescending { it.xp }
        val index = sortedProfiles.indexOfFirst { it.uid == userId }
        return Result.success(if (index >= 0) index + 1 else 0)
    }

    override suspend fun updatePseudo(userId: String, newPseudo: String): Result<Unit> {
        if (updatePseudoResult.isSuccess) {
            profiles[userId]?.let {
                profiles[userId] = it.copy().apply { pseudo = newPseudo }
            }
        }
        return updatePseudoResult
    }

    override suspend fun getLeaderboard(limit: Int): Result<List<UserProfile>> {
        return if (leaderboardResult.isSuccess) {
            Result.success(profiles.values.sortedByDescending { it.xp }.take(limit))
        } else {
            leaderboardResult
        }
    }
}
