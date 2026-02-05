package com.example.ascendlifequest.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuestHelperTest {

    @Before
    fun setup() {
        // Reset the initial generation flag before each test
        QuestHelper.resetInitialGenerationFlag()
    }

    @Test
    fun `hasInitialGenerationBeenDone returns false initially`() {
        QuestHelper.resetInitialGenerationFlag()
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())
    }

    @Test
    fun `markInitialGenerationAsDone sets flag to true`() {
        QuestHelper.resetInitialGenerationFlag()
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())

        QuestHelper.markInitialGenerationAsDone()

        assertTrue(QuestHelper.hasInitialGenerationBeenDone())
    }

    @Test
    fun `resetInitialGenerationFlag sets flag to false`() {
        QuestHelper.markInitialGenerationAsDone()
        assertTrue(QuestHelper.hasInitialGenerationBeenDone())

        QuestHelper.resetInitialGenerationFlag()

        assertFalse(QuestHelper.hasInitialGenerationBeenDone())
    }

    @Test
    fun `getMaxQuests returns 5`() {
        assertEquals(5, QuestHelper.getMaxQuests())
    }

    @Test
    fun `initial generation flag persists across multiple checks`() {
        QuestHelper.resetInitialGenerationFlag()

        // Check multiple times - should remain false
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())

        // Mark as done
        QuestHelper.markInitialGenerationAsDone()

        // Check multiple times - should remain true
        assertTrue(QuestHelper.hasInitialGenerationBeenDone())
        assertTrue(QuestHelper.hasInitialGenerationBeenDone())
        assertTrue(QuestHelper.hasInitialGenerationBeenDone())
    }

    @Test
    fun `markInitialGenerationAsDone is idempotent`() {
        QuestHelper.resetInitialGenerationFlag()

        // Call multiple times
        QuestHelper.markInitialGenerationAsDone()
        QuestHelper.markInitialGenerationAsDone()
        QuestHelper.markInitialGenerationAsDone()

        // Should still be true
        assertTrue(QuestHelper.hasInitialGenerationBeenDone())
    }

    @Test
    fun `resetInitialGenerationFlag is idempotent`() {
        QuestHelper.markInitialGenerationAsDone()

        // Call reset multiple times
        QuestHelper.resetInitialGenerationFlag()
        QuestHelper.resetInitialGenerationFlag()
        QuestHelper.resetInitialGenerationFlag()

        // Should still be false
        assertFalse(QuestHelper.hasInitialGenerationBeenDone())
    }
}
