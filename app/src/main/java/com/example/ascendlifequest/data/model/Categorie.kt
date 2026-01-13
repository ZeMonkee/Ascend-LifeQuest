package com.example.ascendlifequest.data.model

import androidx.compose.ui.graphics.Color

data class Categorie(
    val id: Int,
    val nom: String,
    val logo: Int,
    val couleur: Color
) {
    fun getIcon(): Int = logo
    fun getColor(): Color = couleur
    fun getColorValue(): Int = couleur.alpha.times(255).toInt().shl(24) or
            couleur.red.times(255).toInt().shl(16) or
            couleur.green.times(255).toInt().shl(8) or
            couleur.blue.times(255).toInt()
}
