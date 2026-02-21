package com.example.ascendlifequest.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.theme.*
import java.util.UUID

@Composable
fun ThemeCreatorScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    val colors by themeViewModel.colors.collectAsState()
    var themeName by remember { mutableStateOf("") }

    // Couleurs éditables
    var lightAccent by remember { mutableStateOf(Color(0xFF5682CB)) }
    var darkBackground by remember { mutableStateOf(Color(0xFF151D31)) }
    var mainText by remember { mutableStateOf(Color(0xFFE8EBF3)) }
    var minusText by remember { mutableStateOf(Color(0xFFBABABA)) }
    var sport by remember { mutableStateOf(Color(0xFFFF4343)) }
    var cuisine by remember { mutableStateOf(Color(0xFFFF8543)) }
    var jeuxVideo by remember { mutableStateOf(Color(0xFFC443FF)) }
    var lecture by remember { mutableStateOf(Color(0xFF42D242)) }
    var gradientStart by remember { mutableStateOf(Color(0xFF1A2238)) }
    var gradientEnd by remember { mutableStateOf(Color(0xFF0D1321)) }

    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var currentPickerColor by remember { mutableStateOf(Color.White) }

    val previewColors = ThemeColors(
        lightAccent = lightAccent,
        darkBackground = darkBackground,
        mainText = mainText,
        minusText = minusText,
        sport = sport,
        cuisine = cuisine,
        jeuxVideo = jeuxVideo,
        lecture = lecture,
        gradientStart = gradientStart,
        gradientEnd = gradientEnd
    )

    // Dialogue de sélection de couleur
    showColorPicker?.let { colorType ->
        ColorPickerDialog(
            title = colorType,
            currentColor = currentPickerColor,
            colors = colors,
            onColorSelected = { selectedColor ->
                when (colorType) {
                    "Couleur d'accent" -> lightAccent = selectedColor
                    "Fond sombre" -> darkBackground = selectedColor
                    "Texte principal" -> mainText = selectedColor
                    "Texte secondaire" -> minusText = selectedColor
                    "Sport" -> sport = selectedColor
                    "Cuisine" -> cuisine = selectedColor
                    "Jeux vidéo" -> jeuxVideo = selectedColor
                    "Lecture" -> lecture = selectedColor
                    "Couleur de départ" -> gradientStart = selectedColor
                    "Couleur de fin" -> gradientEnd = selectedColor
                }
                showColorPicker = null
            },
            onDismiss = { showColorPicker = null }
        )
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = colors.mainText
                    )
                }

                Text(
                    text = "CRÉER UN THÈME",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.mainText,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        if (themeName.isNotBlank()) {
                            val customTheme = CustomThemeData.fromThemeColors(
                                id = UUID.randomUUID().toString(),
                                name = themeName.trim(),
                                colors = previewColors
                            )
                            themeViewModel.addCustomTheme(customTheme)
                            themeViewModel.setCustomTheme(customTheme)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sauvegarder",
                        tint = if (themeName.isNotBlank()) colors.lightAccent else colors.minusText
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nom du thème
                item {
                    OutlinedTextField(
                        value = themeName,
                        onValueChange = { themeName = it },
                        label = { Text("Nom du thème") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.lightAccent,
                            unfocusedBorderColor = colors.minusText,
                            focusedLabelColor = colors.lightAccent,
                            unfocusedLabelColor = colors.minusText,
                            cursorColor = colors.lightAccent,
                            focusedTextColor = colors.mainText,
                            unfocusedTextColor = colors.mainText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Aperçu
                item {
                    Text(
                        text = "Aperçu",
                        color = colors.mainText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ThemePreview(previewColors)
                }

                // Section couleurs principales
                item {
                    SectionTitle("Couleurs principales", colors)
                }

                item {
                    ColorTile("Couleur d'accent", lightAccent, colors) {
                        currentPickerColor = lightAccent
                        showColorPicker = "Couleur d'accent"
                    }
                }
                item {
                    ColorTile("Fond sombre", darkBackground, colors) {
                        currentPickerColor = darkBackground
                        showColorPicker = "Fond sombre"
                    }
                }
                item {
                    ColorTile("Texte principal", mainText, colors) {
                        currentPickerColor = mainText
                        showColorPicker = "Texte principal"
                    }
                }
                item {
                    ColorTile("Texte secondaire", minusText, colors) {
                        currentPickerColor = minusText
                        showColorPicker = "Texte secondaire"
                    }
                }

                // Section gradient
                item {
                    SectionTitle("Gradient de fond", colors)
                }
                item {
                    ColorTile("Couleur de départ", gradientStart, colors) {
                        currentPickerColor = gradientStart
                        showColorPicker = "Couleur de départ"
                    }
                }
                item {
                    ColorTile("Couleur de fin", gradientEnd, colors) {
                        currentPickerColor = gradientEnd
                        showColorPicker = "Couleur de fin"
                    }
                }

                // Section catégories
                item {
                    SectionTitle("Couleurs des catégories", colors)
                }
                item {
                    ColorTile("Sport", sport, colors) {
                        currentPickerColor = sport
                        showColorPicker = "Sport"
                    }
                }
                item {
                    ColorTile("Cuisine", cuisine, colors) {
                        currentPickerColor = cuisine
                        showColorPicker = "Cuisine"
                    }
                }
                item {
                    ColorTile("Jeux vidéo", jeuxVideo, colors) {
                        currentPickerColor = jeuxVideo
                        showColorPicker = "Jeux vidéo"
                    }
                }
                item {
                    ColorTile("Lecture", lecture, colors) {
                        currentPickerColor = lecture
                        showColorPicker = "Lecture"
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, colors: ThemeColors) {
    Text(
        text = title,
        color = colors.mainText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ColorTile(
    label: String,
    color: Color,
    themeColors: ThemeColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = themeColors.darkBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = themeColors.mainText,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .border(2.dp, themeColors.minusText, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun ThemePreview(colors: ThemeColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.backgroundGradient)
            .border(2.dp, colors.lightAccent, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Titre exemple",
                color = colors.mainText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Texte secondaire",
                color = colors.minusText,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Bouton exemple
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.lightAccent)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Bouton",
                    color = colors.mainText,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Couleurs des catégories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryPreviewDot(colors.sport, "Sport", colors.minusText)
                CategoryPreviewDot(colors.cuisine, "Cuisine", colors.minusText)
                CategoryPreviewDot(colors.jeuxVideo, "Jeux", colors.minusText)
                CategoryPreviewDot(colors.lecture, "Lecture", colors.minusText)
            }
        }
    }
}

@Composable
private fun CategoryPreviewDot(color: Color, label: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColorPickerDialog(
    title: String,
    currentColor: Color,
    colors: ThemeColors,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedColors = listOf(
        Color.Red, Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B),
        Color(0xFF5682CB), Color(0xFF151D31), Color(0xFF1A2238),
        Color(0xFF0D1321), Color(0xFFE8EBF3), Color(0xFFBABABA),
        Color(0xFF9B59B6), Color(0xFF27AE60), Color(0xFFE74C3C),
        Color(0xFFE67E22), Color(0xFF00BCD4), Color(0xFF1A1025),
        Color(0xFF0D1F15), Color(0xFF1F0D0D), Color(0xFF1F150D),
        Color(0xFF0D1A1F)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.darkBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    color = colors.mainText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(250.dp)
                ) {
                    items(predefinedColors) { color ->
                        val isSelected = color.value == currentColor.value
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else colors.minusText,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onColorSelected(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sélectionné",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Annuler", color = colors.minusText)
                }
            }
        }
    }
}
