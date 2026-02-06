package com.example.ascendlifequest.ui.features.quest

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.data.repository.QuestGeneratorRepositoryImpl
import com.example.ascendlifequest.data.repository.QuestRepositoryImpl
import com.example.ascendlifequest.di.AppViewModelFactory
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.components.WeatherWidget
import com.example.ascendlifequest.ui.components.fetchWeather
import com.example.ascendlifequest.ui.components.requestRealLocation
import com.example.ascendlifequest.ui.features.quest.components.QuestCategory
import com.example.ascendlifequest.ui.theme.themeColors
import com.example.ascendlifequest.util.QuestHelper

/**
 * Main quest screen displaying categorized quests with progress tracking. Handles quest generation,
 * completion, and weather-dependent quest visibility.
 *
 * @param navController Navigation controller for screen transitions
 * @param viewModel ViewModel managing quest state and operations
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuestScreen(
        navController: NavHostController,
        viewModel: QuestViewModel =
                viewModel(
                        factory =
                                AppViewModelFactory(
                                        questRepository = QuestRepositoryImpl(LocalContext.current),
                                        questGeneratorRepository =
                                                QuestGeneratorRepositoryImpl(LocalContext.current)
                                )
                )
) {
    val context = LocalContext.current

    // Lire userId depuis le ViewModel (injection via AppViewModelFactory)
    val userIdState by viewModel.currentUserId.collectAsState()
    val userId = userIdState

    val categories by viewModel.categories.collectAsState()
    val quests by viewModel.quests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationProgress by viewModel.generationProgress.collectAsState()
    // questCounter n'est pas utilisé pour l'instant ; on le laisse dans le ViewModel si besoin.
    val completedQuestsCount by viewModel.completedQuestsCount.collectAsState()
    val showMaxQuestsDialog by viewModel.showMaxQuestsDialog.collectAsState()

    val maxQuests = QuestHelper.getMaxQuests()
    val colors = themeColors()

    LaunchedEffect(Unit) {
        // Verifier si c'est un nouveau jour et nettoyer les quetes si necessaire
        val wasResetForNewDay = viewModel.checkAndResetForNewDay(context, userId)

        // Verifier si l'utilisateur a change et nettoyer les quetes si necessaire
        val wasCleared = viewModel.checkAndClearQuestsForNewUser(context, userId)

        if (wasResetForNewDay || wasCleared) {
            // Si les quetes ont ete videes, on force la regeneration
            QuestHelper.resetInitialGenerationFlag()
        }

        // Generer les quetes initiales puis charger les donnees
        viewModel.generateInitialQuests(context, userId)
        viewModel.loadData(context, userId)
    }

    // Dialog pour afficher le max atteint
    if (showMaxQuestsDialog) {
        AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(text = "Limite atteinte", color = colors.mainText) },
                text = {
                    Text(
                            text =
                                    "Nombre maximum de quêtes atteint ($maxQuests/$maxQuests).\n\nVidez la base de données pour générer de nouvelles quêtes.",
                            color = colors.minusText
                    )
                },
                confirmButton = {
                    Button(
                            onClick = { viewModel.dismissDialog() },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = colors.lightAccent,
                                            contentColor = colors.mainText
                                    )
                    ) { Text("OK") }
                },
                containerColor = colors.darkBackground
        )
    }

    AppBottomNavBar(navController, BottomNavItem.Quetes) { innerPadding ->
        AppBackground {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AppHeader(
                        title = "QUÊTES",
                        trailing = {
                            // Widget aplati : largeur courte et hauteur adaptée
                            WeatherWidget(modifier = Modifier.width(80.dp).height(36.dp))
                        }
                )

                // Récupération asynchrone de la météo pour marquer les quêtes dépendantes
                var isWeatherBad by remember { mutableStateOf<Boolean?>(null) }
                var weatherCondition by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    try {
                        val loc = requestRealLocation(context)
                        if (loc != null) {
                            val weather = fetchWeather(loc.latitude, loc.longitude)
                            if (weather != null) {
                                weatherCondition = weather.second
                                val badKeywords =
                                        listOf("Pluie", "Neige", "Averses", "Bruine", "Brouillard")
                                isWeatherBad =
                                        badKeywords.any { k ->
                                            weatherCondition?.contains(k, ignoreCase = true) == true
                                        }
                            }
                        }
                    } catch (_: Exception) {
                        // en cas d'échec, on n'affiche pas les marquages
                        isWeatherBad = null
                    }
                }

                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Barre de progression des quêtes terminées
                    val totalQuests = quests.size.coerceAtLeast(1)
                    LinearProgressIndicator(
                            progress = { completedQuestsCount.toFloat() / totalQuests },
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.lecture,
                            trackColor = colors.minusText.copy(alpha = 0.3f),
                            strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = "$completedQuestsCount/${quests.size} quêtes terminées",
                            fontSize = 14.sp,
                            color = colors.minusText
                    )

                    // BOUTON CRÉER UNE QUÊTE
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                            onClick = { viewModel.generateNewQuest(context, userId) },
                            enabled = !isGenerating,
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = colors.lightAccent,
                                            contentColor = colors.mainText,
                                            disabledContainerColor =
                                                    colors.lightAccent.copy(alpha = 0.5f),
                                            disabledContentColor =
                                                    colors.mainText.copy(alpha = 0.5f)
                                    )
                    ) { Text("Créer une quête") }

                    // BOUTON POUR VIDER LA BASE DE DONNÉES
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                            onClick = { viewModel.clearDatabase(context, userId) },
                            enabled = !isGenerating,
                            colors =
                                    ButtonDefaults.outlinedButtonColors(
                                            contentColor = colors.minusText
                                    )
                    ) { Text("Vider la BDD (Debug)") }
                }

                // Loader pendant la génération initiale
                if (isGenerating) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = colors.lightAccent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    text = "Génération des quêtes...",
                                    fontSize = 18.sp,
                                    color = colors.mainText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    text = "$generationProgress/$maxQuests",
                                    fontSize = 24.sp,
                                    color = colors.lightAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                    progress = { generationProgress.toFloat() / maxQuests },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                                    color = colors.lecture,
                                    trackColor = colors.minusText.copy(alpha = 0.3f),
                                    strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                } else if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.lightAccent)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        items(categories) { categorie ->
                            val questsForCategory = quests.filter { it.categorie == categorie.id }

                            if (questsForCategory.isNotEmpty()) {
                                QuestCategory(
                                        categorie,
                                        questsForCategory,
                                        context,
                                        userId,
                                        isWeatherBad == true
                                ) { _: Int, isDone: Boolean, xpAmount: Int ->
                                    viewModel.updateQuestState(isDone, xpAmount)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
