package com.example.ascendlifequest.data.model

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestTest {

    @Test
    fun `getTempsNecessaireMinutes returns correct minutes for hours`() {
        val quest = Quest(
            id = 1,
            categorie = 1,
            nom = "Test Quest",
            description = "Description",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 2.hours,
            dependantMeteo = false
        )

        assertEquals(120L, quest.getTempsNecessaireMinutes())
    }

    @Test
    fun `getTempsNecessaireMinutes returns correct minutes`() {
        val quest = Quest(
            id = 1,
            categorie = 1,
            nom = "Test Quest",
            description = "Description",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 45.minutes,
            dependantMeteo = false
        )

        assertEquals(45L, quest.getTempsNecessaireMinutes())
    }

    @Test
    fun `quest default valider is false`() {
        val quest = Quest(
            id = 1,
            categorie = 1,
            nom = "Test Quest",
            description = "Description",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 30.minutes,
            dependantMeteo = false
        )

        assertFalse(quest.valider)
    }

    @Test
    fun `quest can be created as validated`() {
        val quest = Quest(
            id = 1,
            categorie = 1,
            nom = "Test Quest",
            description = "Description",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 30.minutes,
            dependantMeteo = false,
            valider = true
        )

        assertTrue(quest.valider)
    }

    @Test
    fun `quest with weather dependency is set correctly`() {
        val outdoorQuest = Quest(
            id = 1,
            categorie = 1,
            nom = "Outdoor Quest",
            description = "Go outside",
            preferenceRequis = 1,
            xpRapporte = 150,
            tempsNecessaire = 60.minutes,
            dependantMeteo = true
        )

        assertTrue(outdoorQuest.dependantMeteo)
    }

    @Test
    fun `quest without weather dependency is set correctly`() {
        val indoorQuest = Quest(
            id = 2,
            categorie = 1,
            nom = "Indoor Quest",
            description = "Stay inside",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 30.minutes,
            dependantMeteo = false
        )

        assertFalse(indoorQuest.dependantMeteo)
    }

    @Test
    fun `quest properties are accessible`() {
        val quest = Quest(
            id = 42,
            categorie = 5,
            nom = "Epic Quest",
            description = "Complete the epic adventure",
            preferenceRequis = 3,
            xpRapporte = 500,
            tempsNecessaire = 90.minutes,
            dependantMeteo = true
        )

        assertEquals(42, quest.id)
        assertEquals(5, quest.categorie)
        assertEquals("Epic Quest", quest.nom)
        assertEquals("Complete the epic adventure", quest.description)
        assertEquals(3, quest.preferenceRequis)
        assertEquals(500, quest.xpRapporte)
        assertEquals(90L, quest.getTempsNecessaireMinutes())
        assertTrue(quest.dependantMeteo)
    }

    @Test
    fun `quest copy with valider change works`() {
        val originalQuest = Quest(
            id = 1,
            categorie = 1,
            nom = "Test",
            description = "Desc",
            preferenceRequis = 1,
            xpRapporte = 100,
            tempsNecessaire = 30.minutes,
            dependantMeteo = false,
            valider = false
        )

        val completedQuest = originalQuest.copy(valider = true)

        assertFalse(originalQuest.valider)
        assertTrue(completedQuest.valider)
        assertEquals(originalQuest.id, completedQuest.id)
        assertEquals(originalQuest.nom, completedQuest.nom)
    }

    @Test
    fun `quest XP values are preserved`() {
        val lowXpQuest = Quest(
            id = 1, categorie = 1, nom = "Easy", description = "Easy task",
            preferenceRequis = 1, xpRapporte = 25, tempsNecessaire = 10.minutes, dependantMeteo = false
        )

        val highXpQuest = Quest(
            id = 2, categorie = 1, nom = "Hard", description = "Hard task",
            preferenceRequis = 5, xpRapporte = 1000, tempsNecessaire = 3.hours, dependantMeteo = true
        )

        assertEquals(25, lowXpQuest.xpRapporte)
        assertEquals(1000, highXpQuest.xpRapporte)
    }
}
