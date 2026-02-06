package com.example.ascendlifequest.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * CompositionLocal pour accéder aux couleurs du thème
 */
val LocalThemeColors = compositionLocalOf { ThemeColors.defaultDark }

/**
 * ViewModel gérant le thème de l'application avec support des thèmes personnalisés
 */
class ThemeViewModel(context: Context) : ViewModel() {

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME = "current_theme"
        private const val KEY_IS_CUSTOM = "is_custom_theme"
        private const val KEY_CUSTOM_THEMES = "custom_themes"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private val _currentTheme = MutableStateFlow<AppThemeType?>(null)
    val currentTheme: StateFlow<AppThemeType?> = _currentTheme.asStateFlow()

    private val _currentCustomThemeId = MutableStateFlow<String?>(null)
    val currentCustomThemeId: StateFlow<String?> = _currentCustomThemeId.asStateFlow()

    private val _isCustomTheme = MutableStateFlow(false)
    val isCustomTheme: StateFlow<Boolean> = _isCustomTheme.asStateFlow()

    private val _colors = MutableStateFlow(ThemeColors.defaultDark)
    val colors: StateFlow<ThemeColors> = _colors.asStateFlow()

    private val _customThemes = MutableStateFlow<List<CustomThemeData>>(emptyList())
    val customThemes: StateFlow<List<CustomThemeData>> = _customThemes.asStateFlow()

    init {
        loadTheme()
    }

    private fun loadTheme() {
        // Charger les thèmes personnalisés
        val customThemesJson = prefs.getString(KEY_CUSTOM_THEMES, null)
        if (customThemesJson != null) {
            try {
                _customThemes.value = json.decodeFromString<List<CustomThemeData>>(customThemesJson)
            } catch (e: Exception) {
                _customThemes.value = emptyList()
            }
        }

        // Charger le thème actuel
        val isCustom = prefs.getBoolean(KEY_IS_CUSTOM, false)
        _isCustomTheme.value = isCustom

        if (isCustom) {
            val customThemeId = prefs.getString(KEY_THEME, null)
            _currentCustomThemeId.value = customThemeId
            val customTheme = _customThemes.value.find { it.id == customThemeId }
            if (customTheme != null) {
                _colors.value = customTheme.toThemeColors()
            } else {
                // Thème personnalisé non trouvé, retour au défaut
                _isCustomTheme.value = false
                _currentTheme.value = AppThemeType.DEFAULT_DARK
                _colors.value = ThemeColors.defaultDark
            }
        } else {
            val themeKey = prefs.getString(KEY_THEME, AppThemeType.DEFAULT_DARK.key)
                ?: AppThemeType.DEFAULT_DARK.key
            _currentTheme.value = AppThemeType.fromKey(themeKey)
            _colors.value = ThemeColors.getColors(_currentTheme.value!!)
        }
    }

    /**
     * Change le thème vers un thème prédéfini
     */
    fun setTheme(theme: AppThemeType) {
        _currentTheme.value = theme
        _currentCustomThemeId.value = null
        _colors.value = ThemeColors.getColors(theme)
        _isCustomTheme.value = false

        prefs.edit {
            putString(KEY_THEME, theme.key)
            putBoolean(KEY_IS_CUSTOM, false)
        }
    }

    /**
     * Change le thème vers un thème personnalisé
     */
    fun setCustomTheme(customTheme: CustomThemeData) {
        _currentCustomThemeId.value = customTheme.id
        _currentTheme.value = null
        _colors.value = customTheme.toThemeColors()
        _isCustomTheme.value = true

        prefs.edit {
            putString(KEY_THEME, customTheme.id)
            putBoolean(KEY_IS_CUSTOM, true)
        }
    }

    /**
     * Ajoute un nouveau thème personnalisé
     */
    fun addCustomTheme(customTheme: CustomThemeData) {
        _customThemes.value = _customThemes.value + customTheme
        saveCustomThemes()
    }

    /**
     * Supprime un thème personnalisé
     */
    fun deleteCustomTheme(themeId: String) {
        _customThemes.value = _customThemes.value.filter { it.id != themeId }

        // Si le thème supprimé était actif, revenir au thème par défaut
        if (_currentCustomThemeId.value == themeId) {
            setTheme(AppThemeType.DEFAULT_DARK)
        }

        saveCustomThemes()
    }

    private fun saveCustomThemes() {
        try {
            val jsonString = json.encodeToString(_customThemes.value)
            prefs.edit { putString(KEY_CUSTOM_THEMES, jsonString) }
        } catch (e: Exception) {
            // Ignorer les erreurs de sauvegarde
        }
    }

    /**
     * Réinitialise au thème par défaut
     */
    fun resetToDefault() {
        setTheme(AppThemeType.DEFAULT_DARK)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                return ThemeViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Composable qui fournit le thème à toute l'application
 */
@Composable
fun AppThemeProvider(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit
) {
    val colors by themeViewModel.colors.collectAsState()

    CompositionLocalProvider(LocalThemeColors provides colors) {
        content()
    }
}

/**
 * Extension pour accéder facilement aux couleurs du thème
 */
@Composable
fun themeColors(): ThemeColors {
    return LocalThemeColors.current
}
