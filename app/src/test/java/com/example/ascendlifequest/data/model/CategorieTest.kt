package com.example.ascendlifequest.data.model

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorieTest {

    @Test
    fun `getIcon returns logo resource id`() {
        val categorie = Categorie(
            id = 1,
            nom = "Sport",
            logo = 12345,
            couleur = Color.Blue
        )

        assertEquals(12345, categorie.getIcon())
    }

    @Test
    fun `getColor returns category color`() {
        val expectedColor = Color.Red
        val categorie = Categorie(
            id = 1,
            nom = "Health",
            logo = 100,
            couleur = expectedColor
        )

        assertEquals(expectedColor, categorie.getColor())
    }

    @Test
    fun `getColorValue converts color to int correctly for red`() {
        val redColor = Color(1f, 0f, 0f, 1f) // Pure red
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = redColor
        )

        // Expected: 0xFFFF0000 (fully opaque red)
        val colorValue = categorie.getColorValue()

        // Alpha should be 255
        val alpha = (colorValue shr 24) and 0xFF
        assertEquals(255, alpha)

        // Red should be 255
        val red = (colorValue shr 16) and 0xFF
        assertEquals(255, red)

        // Green should be 0
        val green = (colorValue shr 8) and 0xFF
        assertEquals(0, green)

        // Blue should be 0
        val blue = colorValue and 0xFF
        assertEquals(0, blue)
    }

    @Test
    fun `getColorValue converts color to int correctly for green`() {
        val greenColor = Color(0f, 1f, 0f, 1f) // Pure green
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = greenColor
        )

        val colorValue = categorie.getColorValue()

        val red = (colorValue shr 16) and 0xFF
        val green = (colorValue shr 8) and 0xFF
        val blue = colorValue and 0xFF

        assertEquals(0, red)
        assertEquals(255, green)
        assertEquals(0, blue)
    }

    @Test
    fun `getColorValue converts color to int correctly for blue`() {
        val blueColor = Color(0f, 0f, 1f, 1f) // Pure blue
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = blueColor
        )

        val colorValue = categorie.getColorValue()

        val red = (colorValue shr 16) and 0xFF
        val green = (colorValue shr 8) and 0xFF
        val blue = colorValue and 0xFF

        assertEquals(0, red)
        assertEquals(0, green)
        assertEquals(255, blue)
    }

    @Test
    fun `getColorValue handles semi-transparent color`() {
        val semiTransparent = Color(1f, 0f, 0f, 0.5f) // 50% transparent red
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = semiTransparent
        )

        val colorValue = categorie.getColorValue()

        val alpha = (colorValue shr 24) and 0xFF
        // 0.5 * 255 = 127.5, should round to 127 or 128
        assertEquals(true, alpha in 127..128)
    }

    @Test
    fun `category properties are accessible`() {
        val categorie = Categorie(
            id = 42,
            nom = "Lecture",
            logo = 999,
            couleur = Color.Magenta
        )

        assertEquals(42, categorie.id)
        assertEquals("Lecture", categorie.nom)
        assertEquals(999, categorie.logo)
        assertEquals(Color.Magenta, categorie.couleur)
    }

    @Test
    fun `category copy works correctly`() {
        val original = Categorie(
            id = 1,
            nom = "Original",
            logo = 100,
            couleur = Color.Blue
        )

        val copied = original.copy(nom = "Copied", couleur = Color.Green)

        assertEquals("Original", original.nom)
        assertEquals("Copied", copied.nom)
        assertEquals(original.id, copied.id)
        assertEquals(original.logo, copied.logo)
        assertEquals(Color.Blue, original.couleur)
        assertEquals(Color.Green, copied.couleur)
    }

    @Test
    fun `getColorValue for white color`() {
        val whiteColor = Color(1f, 1f, 1f, 1f)
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = whiteColor
        )

        val colorValue = categorie.getColorValue()

        val alpha = (colorValue shr 24) and 0xFF
        val red = (colorValue shr 16) and 0xFF
        val green = (colorValue shr 8) and 0xFF
        val blue = colorValue and 0xFF

        assertEquals(255, alpha)
        assertEquals(255, red)
        assertEquals(255, green)
        assertEquals(255, blue)
    }

    @Test
    fun `getColorValue for black color`() {
        val blackColor = Color(0f, 0f, 0f, 1f)
        val categorie = Categorie(
            id = 1,
            nom = "Test",
            logo = 100,
            couleur = blackColor
        )

        val colorValue = categorie.getColorValue()

        val alpha = (colorValue shr 24) and 0xFF
        val red = (colorValue shr 16) and 0xFF
        val green = (colorValue shr 8) and 0xFF
        val blue = colorValue and 0xFF

        assertEquals(255, alpha)
        assertEquals(0, red)
        assertEquals(0, green)
        assertEquals(0, blue)
    }
}
