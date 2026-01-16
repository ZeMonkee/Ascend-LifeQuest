package com.example.ascendlifequest.util

import android.content.Context
import com.example.ascendlifequest.data.local.PreferencesHelper
import com.example.ascendlifequest.data.model.Categorie
import kotlin.random.Random

/**
 * Sélecteur de catégories basé sur les préférences utilisateur. Les catégories avec un score de
 * préférence plus élevé ont plus de chances d'être sélectionnées.
 */
object CategorySelector {

    /**
     * Sélectionne une catégorie de manière pondérée en fonction des préférences utilisateur.
     *
     * @param context Le contexte Android
     * @param userId L'ID de l'utilisateur
     * @param categories La liste des catégories disponibles
     * @return La catégorie sélectionnée, ou null si la liste est vide
     */
    fun selectWeightedCategory(
            context: Context,
            userId: String,
            categories: List<Categorie>
    ): Categorie? {
        if (categories.isEmpty()) return null

        // Récupérer les préférences pour chaque catégorie
        val preferences = PreferencesHelper.getAllPreferences(context, userId)

        // Créer une liste pondérée : chaque catégorie apparaît autant de fois que son score de
        // préférence
        // Score 1 = 1 entrée, Score 5 = 5 entrées (5x plus de chances)
        val weightedCategories = mutableListOf<Categorie>()

        for (category in categories) {
            val weight = preferences[category.id] ?: 3 // Par défaut 3 si non défini
            repeat(weight) { weightedCategories.add(category) }
        }

        // Sélectionner aléatoirement dans la liste pondérée
        return if (weightedCategories.isNotEmpty()) {
            weightedCategories[Random.nextInt(weightedCategories.size)]
        } else {
            categories.randomOrNull()
        }
    }

    /**
     * Sélectionne plusieurs catégories uniques de manière pondérée. Utile pour générer plusieurs
     * quêtes avec une distribution basée sur les préférences.
     *
     * @param context Le contexte Android
     * @param userId L'ID de l'utilisateur
     * @param categories La liste des catégories disponibles
     * @param count Le nombre de catégories à sélectionner
     * @param allowDuplicates Si true, permet de sélectionner la même catégorie plusieurs fois
     * @return La liste des catégories sélectionnées
     */
    fun selectMultipleWeightedCategories(
            context: Context,
            userId: String,
            categories: List<Categorie>,
            count: Int,
            allowDuplicates: Boolean = true
    ): List<Categorie> {
        if (categories.isEmpty()) return emptyList()

        val preferences = PreferencesHelper.getAllPreferences(context, userId)
        val selectedCategories = mutableListOf<Categorie>()

        // Créer la liste pondérée initiale
        val weightedCategories = mutableListOf<Categorie>()
        for (category in categories) {
            val weight = preferences[category.id] ?: 3
            repeat(weight) { weightedCategories.add(category) }
        }

        val actualCount = if (allowDuplicates) count else minOf(count, categories.size)

        repeat(actualCount) {
            if (weightedCategories.isEmpty()) return@repeat

            val selectedIndex = Random.nextInt(weightedCategories.size)
            val selected = weightedCategories[selectedIndex]
            selectedCategories.add(selected)

            // Si on n'autorise pas les doublons, retirer toutes les instances de cette catégorie
            if (!allowDuplicates) {
                weightedCategories.removeAll { it.id == selected.id }
            }
        }

        return selectedCategories
    }

    /**
     * Calcule les probabilités de sélection pour chaque catégorie basées sur les préférences. Utile
     * pour l'affichage ou le debug.
     *
     * @return Map avec categoryId -> probabilité (en pourcentage)
     */
    fun getSelectionProbabilities(
            context: Context,
            userId: String,
            categories: List<Categorie>
    ): Map<Int, Float> {
        if (categories.isEmpty()) return emptyMap()

        val preferences = PreferencesHelper.getAllPreferences(context, userId)
        val totalWeight = categories.sumOf { preferences[it.id] ?: 3 }

        return categories.associate { category ->
            val weight = preferences[category.id] ?: 3
            category.id to (weight.toFloat() / totalWeight * 100)
        }
    }
}
