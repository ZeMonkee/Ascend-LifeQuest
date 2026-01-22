package com.example.ascendlifequest.data.model

import java.util.Date

/**
 * Represents a user account with profile and progress information.
 *
 * @property accountId Unique account identifier
 * @property pseudo User display name
 * @property photoUrl Resource ID for user avatar
 * @property xp Total experience points earned
 * @property online Current online status
 * @property quetesRealisees Number of quests completed
 * @property streak Current daily streak count
 * @property dateDeCreation Account creation date
 * @property rang User rank in the leaderboard
 */
data class User(
        val accountId: Int,
        val pseudo: String,
        val photoUrl: Int,
        val xp: Int = 0,
        val online: Boolean = false,
        val quetesRealisees: Int = 0,
        val streak: Int = 0,
        val dateDeCreation: Date = Date(),
        val rang: Int = 1
)
