package com.example.ascendlifequest.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité Room pour stocker les thèmes personnalisés.
 * Chaque utilisateur peut avoir plusieurs thèmes personnalisés.
 */
@Entity(
    tableName = "custom_themes",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["name"])
    ]
)
data class CustomThemeEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // Propriétaire du thème
    val name: String,
    val lightAccent: Long,
    val darkBackground: Long,
    val mainText: Long,
    val minusText: Long,
    val sport: Long,
    val cuisine: Long,
    val jeuxVideo: Long,
    val lecture: Long,
    val gradientStart: Long,
    val gradientEnd: Long,
    val or: Long,
    val argent: Long,
    val bronze: Long,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false
)

/**
 * Entité pour stocker les paramètres de l'application.
 * Singleton - un seul enregistrement par utilisateur.
 */
@Entity(
    tableName = "app_settings",
    indices = [Index(value = ["userId"], unique = true)]
)
data class AppSettingsEntity(
    @PrimaryKey
    val id: String = "settings",
    val userId: String,
    val selectedThemeType: String = "default_dark", // Type du thème prédéfini
    val selectedCustomThemeId: String? = null, // ID du thème personnalisé si utilisé
    val isCustomTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
