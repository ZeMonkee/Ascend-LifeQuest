package com.example.ascendlifequest.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Types de thèmes prédéfinis disponibles
 */
enum class AppThemeType(val displayName: String, val key: String) {
    DEFAULT_DARK("Défaut (Bleu nuit)", "default_dark"),
    PURPLE("Violet mystique", "purple"),
    GREEN("Vert nature", "green"),
    RED("Rouge passion", "red"),
    ORANGE("Orange chaleureux", "orange"),
    CYAN("Cyan océan", "cyan");

    companion object {
        fun fromKey(key: String): AppThemeType {
            return entries.find { it.key == key } ?: DEFAULT_DARK
        }
    }
}

/**
 * Modèle sérialisable pour un thème personnalisé
 */
@Serializable
data class CustomThemeData(
    val id: String,
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
    val createdAt: Long
) {
    fun toThemeColors(): ThemeColors {
        return ThemeColors(
            lightAccent = Color(lightAccent.toULong()),
            darkBackground = Color(darkBackground.toULong()),
            mainText = Color(mainText.toULong()),
            minusText = Color(minusText.toULong()),
            sport = Color(sport.toULong()),
            cuisine = Color(cuisine.toULong()),
            jeuxVideo = Color(jeuxVideo.toULong()),
            lecture = Color(lecture.toULong()),
            gradientStart = Color(gradientStart.toULong()),
            gradientEnd = Color(gradientEnd.toULong())
        )
    }

    companion object {
        fun fromThemeColors(id: String, name: String, colors: ThemeColors): CustomThemeData {
            return CustomThemeData(
                id = id,
                name = name,
                lightAccent = colors.lightAccent.value.toLong(),
                darkBackground = colors.darkBackground.value.toLong(),
                mainText = colors.mainText.value.toLong(),
                minusText = colors.minusText.value.toLong(),
                sport = colors.sport.value.toLong(),
                cuisine = colors.cuisine.value.toLong(),
                jeuxVideo = colors.jeuxVideo.value.toLong(),
                lecture = colors.lecture.value.toLong(),
                gradientStart = colors.gradientStart.value.toLong(),
                gradientEnd = colors.gradientEnd.value.toLong(),
                createdAt = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Couleurs pour chaque thème
 */
data class ThemeColors(
    val lightAccent: Color,
    val darkBackground: Color,
    val mainText: Color,
    val minusText: Color,
    val sport: Color,
    val cuisine: Color,
    val jeuxVideo: Color,
    val lecture: Color,
    val or: Color = Color(0xFFFFD700),
    val argent: Color = Color(0xFFC0C0C0),
    val bronze: Color = Color(0xFFCD7F32),
    val gradientStart: Color,
    val gradientEnd: Color
) {
    val backgroundGradient: Brush
        get() = Brush.verticalGradient(
            colors = listOf(gradientStart, gradientEnd)
        )

    companion object {
        /**
         * Thème par défaut (Bleu nuit)
         */
        val defaultDark = ThemeColors(
            lightAccent = Color(0xFF5682CB),
            darkBackground = Color(0xFF151D31),
            mainText = Color(0xFFE8EBF3),
            minusText = Color(0xFFBABABA),
            sport = Color(0xFFFF4343),
            cuisine = Color(0xFFFF8543),
            jeuxVideo = Color(0xFFC443FF),
            lecture = Color(0xFF42D242),
            gradientStart = Color(0xFF1A2238),
            gradientEnd = Color(0xFF0D1321)
        )

        /**
         * Thème Violet mystique
         */
        val purple = ThemeColors(
            lightAccent = Color(0xFF9B59B6),
            darkBackground = Color(0xFF1A1025),
            mainText = Color(0xFFE8E0F0),
            minusText = Color(0xFFB8A8C8),
            sport = Color(0xFFFF4343),
            cuisine = Color(0xFFFF8543),
            jeuxVideo = Color(0xFFE066FF),
            lecture = Color(0xFF42D242),
            gradientStart = Color(0xFF2D1B3D),
            gradientEnd = Color(0xFF1A1025)
        )

        /**
         * Thème Vert nature
         */
        val green = ThemeColors(
            lightAccent = Color(0xFF27AE60),
            darkBackground = Color(0xFF0D1F15),
            mainText = Color(0xFFE0F0E8),
            minusText = Color(0xFFA8C8B8),
            sport = Color(0xFFFF4343),
            cuisine = Color(0xFFFF8543),
            jeuxVideo = Color(0xFFC443FF),
            lecture = Color(0xFF5EE85E),
            gradientStart = Color(0xFF1B3D2D),
            gradientEnd = Color(0xFF0D1F15)
        )

        /**
         * Thème Rouge passion
         */
        val red = ThemeColors(
            lightAccent = Color(0xFFE74C3C),
            darkBackground = Color(0xFF1F0D0D),
            mainText = Color(0xFFF0E0E0),
            minusText = Color(0xFFC8A8A8),
            sport = Color(0xFFFF6B6B),
            cuisine = Color(0xFFFF8543),
            jeuxVideo = Color(0xFFC443FF),
            lecture = Color(0xFF42D242),
            gradientStart = Color(0xFF3D1B1B),
            gradientEnd = Color(0xFF1F0D0D)
        )

        /**
         * Thème Orange chaleureux
         */
        val orange = ThemeColors(
            lightAccent = Color(0xFFE67E22),
            darkBackground = Color(0xFF1F150D),
            mainText = Color(0xFFF0E8E0),
            minusText = Color(0xFFC8B8A8),
            sport = Color(0xFFFF4343),
            cuisine = Color(0xFFFFAA66),
            jeuxVideo = Color(0xFFC443FF),
            lecture = Color(0xFF42D242),
            gradientStart = Color(0xFF3D2D1B),
            gradientEnd = Color(0xFF1F150D)
        )

        /**
         * Thème Cyan océan
         */
        val cyan = ThemeColors(
            lightAccent = Color(0xFF00BCD4),
            darkBackground = Color(0xFF0D1A1F),
            mainText = Color(0xFFE0F0F5),
            minusText = Color(0xFFA8C8D0),
            sport = Color(0xFFFF4343),
            cuisine = Color(0xFFFF8543),
            jeuxVideo = Color(0xFFC443FF),
            lecture = Color(0xFF42D242),
            gradientStart = Color(0xFF1B3D3D),
            gradientEnd = Color(0xFF0D1A1F)
        )

        /**
         * Récupère les couleurs pour un type de thème
         */
        fun getColors(type: AppThemeType): ThemeColors {
            return when (type) {
                AppThemeType.DEFAULT_DARK -> defaultDark
                AppThemeType.PURPLE -> purple
                AppThemeType.GREEN -> green
                AppThemeType.RED -> red
                AppThemeType.ORANGE -> orange
                AppThemeType.CYAN -> cyan
            }
        }
    }
}
