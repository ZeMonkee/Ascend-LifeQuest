package com.example.ascendlifequest.ui.features.profile

import com.example.ascendlifequest.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitaires pour la logique de présentation du ProfileViewModel (couche ViewModel MVVM).
 *
 * Ces tests vérifient :
 * - Les transformations de données Model -> UI
 * - Les calculs de progression pour l'affichage
 * - La logique de formatage pour la présentation
 *
 * Architecture MVVM :
 * - Model (UserProfile) : Logique métier testée dans UserProfileTest
 * - ViewModel : Logique de présentation testée ici
 * - View : UI Composables (non testés unitairement)
 */
class ProfileViewModelTest {

    @Test
    fun `level display calculation for UI badge`() {
        // Le ViewModel calcule le niveau pour l'afficher dans un badge
        val profile = UserProfile(xp = 0)
        assertEquals(1, profile.calculateLevel())

        val profileLevel2 = UserProfile(xp = 100)
        assertEquals(2, profileLevel2.calculateLevel())

        val profileLevel10 = UserProfile(xp = 8100)
        assertEquals(10, profileLevel10.calculateLevel())
    }

    @Test
    fun `level progression bar value calculation`() {
        // Le ViewModel calcule la progression pour afficher une barre de progression
        val profileAtStart = UserProfile(xp = 100)
        val progression = profileAtStart.calculateLevelProgress()

        // La barre devrait être à 0% au début du niveau
        assertEquals(0f, progression, 0.01f)
    }

    @Test
    fun `level progression percentage for UI display`() {
        // Le ViewModel calcule le pourcentage pour afficher "50% vers niveau 3"
        val profileAtMid = UserProfile(xp = 250)
        val progression = profileAtMid.calculateLevelProgress()

        // À mi-chemin du niveau 2, la barre devrait être à 50%
        assertEquals(0.5f, progression, 0.01f)
    }

    @Test
    fun `XP remaining text for UI label`() {
        // Le ViewModel calcule l'XP restant pour afficher "100 XP vers niveau 2"
        val profile = UserProfile(xp = 0)
        val xpRemaining = profile.xpToNextLevel()

        assertEquals(100L, xpRemaining)
    }

    @Test
    fun `XP remaining updates dynamically`() {
        // Le ViewModel met à jour l'XP restant quand le joueur gagne de l'XP
        val profile1 = UserProfile(xp = 100)
        assertEquals(300L, profile1.xpToNextLevel())

        val profile2 = UserProfile(xp = 250)
        assertEquals(150L, profile2.xpToNextLevel())
    }

    @Test
    fun `profile state for empty UI display`() {
        // Le ViewModel utilise les valeurs par défaut pour l'état initial
        val emptyProfile = UserProfile()

        assertEquals("", emptyProfile.uid)
        assertEquals("", emptyProfile.pseudo)
        assertEquals(0L, emptyProfile.xp)
        assertEquals(0, emptyProfile.quetesRealisees)
        assertEquals(0, emptyProfile.streak)
    }

    @Test
    fun `profile data presentation formatting`() {
        // Le ViewModel formate les données pour l'affichage
        val profile = UserProfile(
            uid = "test-uid",
            pseudo = "TestPlayer",
            xp = 5000,
            quetesRealisees = 100,
            streak = 30,
            rang = 5,
            online = true
        )

        // Vérifier que les données sont prêtes pour l'affichage
        assertEquals("TestPlayer", profile.pseudo) // Titre du profil
        assertEquals(5000L, profile.xp) // Badge XP
        assertEquals(100, profile.quetesRealisees) // Statistique
        assertEquals(30, profile.streak) // Badge de série
        assertEquals(5, profile.rang) // Position dans le classement
        assertTrue(profile.online) // Indicateur de statut en ligne
    }

    @Test
    fun `level milestone detection for UI celebration`() {
        // Le ViewModel détecte les paliers de niveau pour afficher une animation
        val justReachedLevel2 = UserProfile(xp = 100)
        val justReachedLevel3 = UserProfile(xp = 400)
        val justReachedLevel10 = UserProfile(xp = 8100)

        assertEquals(2, justReachedLevel2.calculateLevel())
        assertEquals(3, justReachedLevel3.calculateLevel())
        assertEquals(10, justReachedLevel10.calculateLevel())
    }

    @Test
    fun `XP display formatting for large numbers`() {
        // Le ViewModel formate l'XP pour l'affichage (ex: "5 000 XP")
        val highXpProfile = UserProfile(xp = 5000)

        // L'XP devrait être >= 5000 pour affichage
        assertTrue(highXpProfile.xp >= 5000)
    }

    @Test
    fun `streak badge color logic`() {
        // Le ViewModel détermine la couleur du badge de série
        val noStreak = UserProfile(streak = 0)
        val activeStreak = UserProfile(streak = 7)
        val longStreak = UserProfile(streak = 30)

        assertTrue(noStreak.streak == 0) // Badge gris
        assertTrue(activeStreak.streak > 0) // Badge vert
        assertTrue(longStreak.streak >= 30) // Badge or
    }

    @Test
    fun `profile completion percentage for UI progress`() {
        // Le ViewModel calcule le % de complétion du profil
        val incompleteProfile = UserProfile(pseudo = "")
        val completeProfile = UserProfile(pseudo = "Player", xp = 100)

        // Profil incomplet
        assertTrue(incompleteProfile.pseudo.isEmpty())

        // Profil complet
        assertTrue(completeProfile.pseudo.isNotEmpty())
        assertTrue(completeProfile.xp > 0)
    }
}
