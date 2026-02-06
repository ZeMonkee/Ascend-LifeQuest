package com.example.ascendlifequest.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val devLabel = "DEV"
    val devFullLabel = "Development"

    // PREPROD - Violet/Magenta avec icône de science
    val preprodPrimaryColor = Color(0xFF9C27B0)
    val preprodSecondaryColor = Color(0xFF7B1FA2)
    val preprodGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFBA68C8), Color(0xFF9C27B0), Color(0xFF7B1FA2))
    )
    val preprodIcon = Icons.Filled.Science
    val preprodLabel = "PREPROD"
    val preprodFullLabel = "Pre-Production"
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
        visible = BuildConfig.IS_DEBUG_BUILD && envData != null,
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
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
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
 * Badge d'environnement compact - affiché dans un coin
 * Idéal pour ne pas gêner l'interface utilisateur
 */
@Composable
fun EnvironmentBadgeCompact(
    modifier: Modifier = Modifier
) {
    val envData = getCurrentEnvironmentData()

    AnimatedVisibility(
        visible = BuildConfig.IS_DEBUG_BUILD && envData != null,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(200)),
        modifier = modifier.zIndex(Float.MAX_VALUE)
    ) {
        envData?.let { data ->
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(data.gradient)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.label.take(1), // Première lettre: D ou P
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Widget flottant détaillé avec informations de version et environnement
 * Peut être replié/déplié par l'utilisateur
 */
@Composable
fun DebugInfoPanel(
    modifier: Modifier = Modifier
) {
    val envData = getCurrentEnvironmentData()
    var isExpanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = BuildConfig.IS_DEBUG_BUILD && envData != null,
        enter = fadeIn(tween(300)) + slideInVertically { it },
        exit = fadeOut(tween(200)) + slideOutVertically { it },
        modifier = modifier.zIndex(Float.MAX_VALUE)
    ) {
        envData?.let { data ->
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1E1E)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Header toujours visible
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(data.gradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = data.label,
                                color = data.primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Détails (affichés si expanded)
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            DebugInfoRow("Environment", data.fullLabel, data.primaryColor)
                            DebugInfoRow("Version", BuildConfig.VERSION_NAME, Color.White)
                            DebugInfoRow("Version Code", BuildConfig.VERSION_CODE.toString(), Color.White)
                            DebugInfoRow("Debug Tools", if (BuildConfig.SHOW_DEBUG_TOOLS) "Enabled" else "Disabled",
                                if (BuildConfig.SHOW_DEBUG_TOOLS) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugInfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Widget flottant simple qui affiche les informations de version en mode debug
 */
@Composable
fun DebugVersionOverlay(
    modifier: Modifier = Modifier
) {
    val envData = getCurrentEnvironmentData()

    AnimatedVisibility(
        visible = BuildConfig.IS_DEBUG_BUILD && envData != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        envData?.let { data ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, data.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(data.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${data.label} • v${BuildConfig.VERSION_NAME}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Vérifie si les outils de debug doivent être affichés
 */
fun shouldShowDebugTools(): Boolean {
    return BuildConfig.SHOW_DEBUG_TOOLS
}

/**
 * Vérifie si c'est un build de debug (dev ou preprod)
 */
fun isDebugBuild(): Boolean {
    return BuildConfig.IS_DEBUG_BUILD
}

/**
 * Retourne le nom de l'environnement actuel
 */
fun getEnvironmentName(): String {
    return BuildConfig.ENVIRONMENT_NAME
}
