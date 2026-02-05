package com.example.ascendlifequest.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {

    @Test
    fun `calculateLevel returns 1 for 0 XP`() {
        val profile = UserProfile(xp = 0)
        assertEquals(1, profile.calculateLevel())
    }

    @Test
    fun `calculateLevel returns 1 for 99 XP`() {
        val profile = UserProfile(xp = 99)
        assertEquals(1, profile.calculateLevel())
    }

    @Test
    fun `calculateLevel returns 2 for 100 XP`() {
        val profile = UserProfile(xp = 100)
        assertEquals(2, profile.calculateLevel())
    }

    @Test
    fun `calculateLevel returns 3 for 400 XP`() {
        val profile = UserProfile(xp = 400)
        assertEquals(3, profile.calculateLevel())
    }

    @Test
    fun `calculateLevel returns 10 for 8100 XP`() {
        // Level 10 requires (10-1)^2 * 100 = 8100 XP minimum
        val profile = UserProfile(xp = 8100)
        assertEquals(10, profile.calculateLevel())
    }

    @Test
    fun `calculateLevel returns 11 for 10000 XP`() {
        // Level 11 requires (11-1)^2 * 100 = 10000 XP minimum
        val profile = UserProfile(xp = 10000)
        assertEquals(11, profile.calculateLevel())
    }

    @Test
    fun `calculateLevelProgress returns 0 at level start`() {
        // At exactly 100 XP (start of level 2)
        val profile = UserProfile(xp = 100)
        assertEquals(0f, profile.calculateLevelProgress(), 0.01f)
    }

    @Test
    fun `calculateLevelProgress returns 0_5 at half level`() {
        // Level 2: 100-400 XP, midpoint is 250 XP
        // Progress = (250 - 100) / (400 - 100) = 150 / 300 = 0.5
        val profile = UserProfile(xp = 250)
        assertEquals(0.5f, profile.calculateLevelProgress(), 0.01f)
    }

    @Test
    fun `calculateLevelProgress is bounded between 0 and 1`() {
        val profile1 = UserProfile(xp = 0)
        val profile2 = UserProfile(xp = 99)

        val progress1 = profile1.calculateLevelProgress()
        val progress2 = profile2.calculateLevelProgress()

        assertEquals(true, progress1 >= 0f && progress1 <= 1f)
        assertEquals(true, progress2 >= 0f && progress2 <= 1f)
    }

    @Test
    fun `xpToNextLevel returns correct value at level start`() {
        // At 100 XP (level 2), next level at 400 XP
        val profile = UserProfile(xp = 100)
        assertEquals(300L, profile.xpToNextLevel())
    }

    @Test
    fun `xpToNextLevel returns correct value mid-level`() {
        // At 250 XP (level 2), next level at 400 XP
        val profile = UserProfile(xp = 250)
        assertEquals(150L, profile.xpToNextLevel())
    }

    @Test
    fun `xpToNextLevel returns correct value for level 1`() {
        // At 0 XP (level 1), next level at 100 XP
        val profile = UserProfile(xp = 0)
        assertEquals(100L, profile.xpToNextLevel())
    }

    @Test
    fun `xpToNextLevel returns correct value for high level`() {
        // At 8100 XP (level 10), next level at 10000 XP
        val profile = UserProfile(xp = 8100)
        assertEquals(1900L, profile.xpToNextLevel())
    }

    @Test
    fun `default constructor creates valid profile`() {
        val profile = UserProfile()

        assertEquals("", profile.id)
        assertEquals("", profile.uid)
        assertEquals("", profile.pseudo)
        assertEquals(0L, profile.xp)
        assertEquals(0, profile.quetesRealisees)
        assertEquals(0, profile.streak)
        assertEquals(0, profile.rang)
        assertEquals(false, profile.online)
    }

    @Test
    fun `constructor with parameters sets values correctly`() {
        val profile = UserProfile(
            id = "test-id",
            uid = "test-uid",
            pseudo = "TestPlayer",
            xp = 5000,
            quetesRealisees = 50,
            streak = 10,
            rang = 5,
            online = true
        )

        assertEquals("test-id", profile.id)
        assertEquals("test-uid", profile.uid)
        assertEquals("TestPlayer", profile.pseudo)
        assertEquals(5000L, profile.xp)
        assertEquals(50, profile.quetesRealisees)
        assertEquals(10, profile.streak)
        assertEquals(5, profile.rang)
        assertEquals(true, profile.online)
    }

    @Test
    fun `level progression is consistent`() {
        // Verify that levels increase at expected XP thresholds
        val xpThresholds = listOf(0L, 100L, 400L, 900L, 1600L, 2500L, 3600L, 4900L, 6400L, 8100L)
        val expectedLevels = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        xpThresholds.forEachIndexed { index, xp ->
            val profile = UserProfile(xp = xp)
            assertEquals(
                "XP $xp should be level ${expectedLevels[index]}",
                expectedLevels[index],
                profile.calculateLevel()
            )
        }
    }
}
