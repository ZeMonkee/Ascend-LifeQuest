package com.example.ascendlifequest.ui.features.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.theme.AppThemeType
import com.example.ascendlifequest.ui.theme.CustomThemeData
import com.example.ascendlifequest.ui.theme.ThemeColors
import com.example.ascendlifequest.ui.theme.ThemeViewModel

/**
 * Écran de sélection de thème avec support des thèmes personnalisés
 */
@Composable
fun ThemeScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val currentCustomThemeId by themeViewModel.currentCustomThemeId.collectAsState()
    val isCustomTheme by themeViewModel.isCustomTheme.collectAsState()
    val colors by themeViewModel.colors.collectAsState()
    val customThemes by themeViewModel.customThemes.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<CustomThemeData?>(null) }

    // Dialogue de suppression
    showDeleteDialog?.let { themeToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer ce thème ?", color = colors.mainText) },
            text = {
                Text(
                    "Voulez-vous vraiment supprimer le thème \"${themeToDelete.name}\" ?",
                    color = colors.minusText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        themeViewModel.deleteCustomTheme(themeToDelete.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Supprimer", color = colors.sport)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Annuler", color = colors.minusText)
                }
            },
            containerColor = colors.darkBackground
        )
    }

    AppBottomNavBar(navController, BottomNavItem.Parametres) { innerPadding ->
        AppBackground {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header avec bouton retour
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = colors.mainText
                            )
                        }

                        Text(
                            text = "THÈMES",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.mainText,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // Liste des thèmes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Thèmes prédéfinis",
                                color = colors.mainText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(AppThemeType.entries) { themeType ->
                            val themeColors = ThemeColors.getColors(themeType)
                            val isSelected = !isCustomTheme && currentTheme == themeType

                            ThemeItem(
                                name = themeType.displayName,
                                themeColors = themeColors,
                                isSelected = isSelected,
                                currentColors = colors,
                                isCustom = false,
                                onClick = { themeViewModel.setTheme(themeType) },
                                onLongClick = { }
                            )
                        }

                        // Thèmes personnalisés
                        if (customThemes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Mes thèmes",
                                    color = colors.mainText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(customThemes) { customTheme ->
                                val themeColors = customTheme.toThemeColors()
                                val isSelected = isCustomTheme && currentCustomThemeId == customTheme.id

                                ThemeItem(
                                    name = customTheme.name,
                                    themeColors = themeColors,
                                    isSelected = isSelected,
                                    currentColors = colors,
                                    isCustom = true,
                                    onClick = { themeViewModel.setCustomTheme(customTheme) },
                                    onLongClick = { showDeleteDialog = customTheme }
                                )
                            }
                        }

                        // Espace pour le FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                // Bouton flottant pour créer un thème
                FloatingActionButton(
                    onClick = { navController.navigate("theme_creator") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = colors.lightAccent,
                    contentColor = colors.mainText,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Créer un thème",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeItem(
    name: String,
    themeColors: ThemeColors,
    isSelected: Boolean,
    currentColors: ThemeColors,
    isCustom: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (isCustom) onLongClick else null
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = currentColors.lightAccent,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = currentColors.darkBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aperçu des couleurs du thème
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(themeColors.backgroundGradient)
                    .border(2.dp, themeColors.lightAccent, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(themeColors.lightAccent)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Nom du thème et couleurs des catégories
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = currentColors.mainText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCustom) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    currentColors.lightAccent.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Perso",
                                color = currentColors.lightAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Aperçu des couleurs de catégories
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ColorDot(themeColors.sport)
                    ColorDot(themeColors.cuisine)
                    ColorDot(themeColors.jeuxVideo)
                    ColorDot(themeColors.lecture)
                }
            }

            // Indicateur de sélection
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(currentColors.lightAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sélectionné",
                        tint = currentColors.mainText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(2.dp, currentColors.minusText, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
    )
}
