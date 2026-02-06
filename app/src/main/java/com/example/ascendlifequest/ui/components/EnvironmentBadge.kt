package com.example.ascendlifequest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ascendlifequest.BuildConfig

/**
 * Configuration des couleurs et icônes pour chaque environnement
 */
private object EnvironmentConfig {
    // DEV - Orange/Rouge vif avec icône de code
    val devPrimaryColor = Color(0xFFFF5722)
    val devSecondaryColor = Color(0xFFE64A19)
    val devGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF7043), Color(0xFFFF5722), Color(0xFFE64A19))
    )
    val devIcon = Icons.Filled.Code
    const val devLabel = "DEV"
    const val devFullLabel = "Development"

    // PREPROD - Violet/Magenta avec icône de science
    val preprodPrimaryColor = Color(0xFF9C27B0)
    val preprodSecondaryColor = Color(0xFF7B1FA2)
    val preprodGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBA68C8), Color(0xFF9C27B0), Color(0xFF7B1FA2))
    )
    val preprodIcon = Icons.Filled.Science
    const val preprodLabel = "PREPROD"
    const val preprodFullLabel = "Pre-Production"
}

/**
 * Données de configuration pour l'environnement actuel
 */
private data class EnvironmentData(
    val primaryColor: Color,
    val secondaryColor: Color,
    val gradient: Brush,
    val icon: ImageVector,
    val label: String,
    val fullLabel: String
)

@Suppress("KotlinConstantConditions")
private fun getCurrentEnvironmentData(): EnvironmentData? {
    return when (BuildConfig.ENVIRONMENT_NAME) {
        "Development" -> EnvironmentData(
            primaryColor = EnvironmentConfig.devPrimaryColor,
            secondaryColor = EnvironmentConfig.devSecondaryColor,
            gradient = EnvironmentConfig.devGradient,
            icon = EnvironmentConfig.devIcon,
            label = EnvironmentConfig.devLabel,
            fullLabel = EnvironmentConfig.devFullLabel
        )
        "Pre-Production" -> EnvironmentData(
            primaryColor = EnvironmentConfig.preprodPrimaryColor,
            secondaryColor = EnvironmentConfig.preprodSecondaryColor,
            gradient = EnvironmentConfig.preprodGradient,
            icon = EnvironmentConfig.preprodIcon,
            label = EnvironmentConfig.preprodLabel,
            fullLabel = EnvironmentConfig.preprodFullLabel
        )
        else -> null // Production - pas de badge
    }
}

/**
 * Widget principal qui affiche un badge indiquant l'environnement (DEV, PREPROD)
 * Ce widget est automatiquement masqué en production
 *
 * Design moderne avec:
 * - Gradient de couleurs
 * - Icône distinctive
 * - Animation d'entrée/sortie
 * - Position safe (évite la caméra)
 */
@Composable
fun EnvironmentBadge(
    modifier: Modifier = Modifier
) {
    val envData = getCurrentEnvironmentData()

    // Ne pas afficher en production
    AnimatedVisibility(
        visible = envData != null,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it },
        modifier = modifier
            .zIndex(Float.MAX_VALUE)
            .statusBarsPadding()
    ) {
        envData?.let { data ->
            Surface(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(bottomEnd = 16.dp)
                    ),
                shape = RoundedCornerShape(bottomEnd = 16.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(data.gradient)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = data.label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vérifie si les outils de debug doivent être affichés
 * Retourne true uniquement en mode Debug (pas en Release)
 */
@Suppress("KotlinConstantConditions")
fun shouldShowDebugTools(): Boolean {
    return BuildConfig.SHOW_DEBUG_TOOLS
}

