package com.example.ascendlifequest.screen.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.*
import com.example.ascendlifequest.components.main.QuestCategory
import com.example.ascendlifequest.helpers.QuestHelper
import com.example.ascendlifequest.model.Categorie
import com.example.ascendlifequest.model.Quest
import com.example.ascendlifequest.repository.QuestRepository
import com.example.ascendlifequest.repository.generateQuestForCategory
import com.example.ascendlifequest.service.AuthService
import com.example.ascendlifequest.ui.theme.AppColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuestScreen(navController: NavHostController) {
    val context = LocalContext.current
    val repository = remember { QuestRepository(context) }
    val authService = remember { AuthService(context) }
    val userId = authService.getUserId()

    var categories by remember { mutableStateOf<List<Categorie>>(emptyList()) }
    var quests by remember { mutableStateOf<List<Quest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var generationProgress by remember { mutableIntStateOf(0) }
    var questCounter by remember { mutableIntStateOf(QuestHelper.getQuestCounter(context)) }
    var completedQuestsCount by remember { mutableIntStateOf(0) }
    var showMaxQuestsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val maxQuests = QuestHelper.getMaxQuests()

    fun refreshCompletedCount() {
        val questIds = quests.map { it.id }
        completedQuestsCount = QuestHelper.getCompletedQuestsCount(context, userId, questIds)
    }

    fun refreshData() {
        scope.launch {
            val loadedCategories = repository.getCategories()
            val loadedQuests = repository.getQuests()

            categories = loadedCategories.map { cat ->
                val restoredColor = Color(cat.couleur.value)
                cat.copy(couleur = restoredColor)
            }
            quests = loadedQuests
            questCounter = QuestHelper.getQuestCounter(context)

            // Mettre à jour le compteur de quêtes terminées
            val questIds = loadedQuests.map { it.id }
            completedQuestsCount = QuestHelper.getCompletedQuestsCount(context, userId, questIds)
        }
    }

    // 🔥 Génération automatique des quêtes au lancement
    suspend fun generateInitialQuests() {
        val currentCounter = QuestHelper.getQuestCounter(context)
        if (currentCounter >= maxQuests) {
            isLoading = false
            return
        }

        isGenerating = true
        generationProgress = currentCounter

        // Charger les catégories d'abord
        val loadedCategories = repository.getCategories().map { cat ->
            val restoredColor = Color(cat.couleur.value)
            cat.copy(couleur = restoredColor)
        }
        categories = loadedCategories

        while (QuestHelper.getQuestCounter(context) < maxQuests) {
            val randomCategory = loadedCategories.randomOrNull() ?: break

            try {
                val newQuest = generateQuestForCategory(context, randomCategory)
                if (newQuest != null) {
                    QuestHelper.incrementQuestCounter(context)
                    generationProgress = QuestHelper.getQuestCounter(context)
                    Log.d("QuestScreen", "✅ Quête générée (${generationProgress}/$maxQuests) : ${newQuest.nom}")

                    // Rafraîchir les quêtes après chaque génération
                    val loadedQuests = repository.getQuests()
                    quests = loadedQuests
                } else {
                    Log.w("QuestScreen", "⚠️ Échec génération, retry dans 10s...")
                    delay(10000) // Attendre avant de réessayer
                }
            } catch (e: Exception) {
                Log.e("QuestScreen", "❌ Erreur génération, retry dans 10s...", e)
                delay(10000) // Attendre plus longtemps en cas d'erreur
            }
        }

        questCounter = QuestHelper.getQuestCounter(context)
        isGenerating = false
        isLoading = false
    }

    LaunchedEffect(Unit) {
        generateInitialQuests()
        refreshData()
    }

    // 🔥 Dialog pour afficher le max atteint
    if (showMaxQuestsDialog) {
        AlertDialog(
            onDismissRequest = { showMaxQuestsDialog = false },
            title = {
                Text(
                    text = "Limite atteinte",
                    color = AppColor.MainTextColor
                )
            },
            text = {
                Text(
                    text = "Nombre maximum de quêtes atteint ($maxQuests/$maxQuests).\n\nVidez la base de données pour générer de nouvelles quêtes.",
                    color = AppColor.MinusTextColor
                )
            },
            confirmButton = {
                Button(
                    onClick = { showMaxQuestsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColor.LightBlueColor,
                        contentColor = AppColor.MainTextColor
                    )
                ) {
                    Text("OK")
                }
            },
            containerColor = AppColor.DarkBlueColor
        )
    }

    AppBottomNavBar(navController, BottomNavItem.Quetes) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppHeader(title = "QUÊTES")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Barre de progression des quêtes terminées
                    val totalQuests = quests.size.coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { completedQuestsCount.toFloat() / totalQuests },
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColor.LectureColor,
                        trackColor = AppColor.MinusTextColor.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedQuestsCount/${quests.size} quêtes terminées",
                        fontSize = 14.sp,
                        color = AppColor.MinusTextColor
                    )

                    // 🔘 BOUTON CRÉER UNE QUÊTE
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (!QuestHelper.canGenerateMoreQuests(context)) {
                                showMaxQuestsDialog = true
                            } else {
                                scope.launch {
                                    val randomCategory = categories.randomOrNull()
                                    if (randomCategory != null) {
                                        val newQuest = generateQuestForCategory(context, randomCategory)
                                        if (newQuest != null) {
                                            QuestHelper.incrementQuestCounter(context)
                                            questCounter = QuestHelper.getQuestCounter(context)
                                            Log.d("QuestScreen", "✅ Quête générée : ${newQuest.nom}")
                                            refreshData()
                                        } else {
                                            Log.e("QuestScreen", "❌ Échec génération quête")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColor.LightBlueColor,
                            contentColor = AppColor.MainTextColor,
                            disabledContainerColor = AppColor.LightBlueColor.copy(alpha = 0.5f),
                            disabledContentColor = AppColor.MainTextColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Créer une quête")
                    }

                    // BOUTON POUR VIDER LA BASE DE DONNÉES
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                repository.clearAllQuests()
                                QuestHelper.resetQuestCounter(context)
                                QuestHelper.clearQuest(context, userId)
                                questCounter = 0
                                completedQuestsCount = 0
                                refreshData()
                            }
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColor.MinusTextColor
                        )
                    ) {
                        Text("Vider la BDD (Debug)")
                    }
                }

                // 🔥 Loader pendant la génération initiale
                if (isGenerating) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = AppColor.LightBlueColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Génération des quêtes...",
                                fontSize = 18.sp,
                                color = AppColor.MainTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$generationProgress/$maxQuests",
                                fontSize = 24.sp,
                                color = AppColor.LightBlueColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { generationProgress.toFloat() / maxQuests },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp),
                                color = AppColor.LectureColor,
                                trackColor = AppColor.MinusTextColor.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                } else if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColor.LightBlueColor)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(categories) { categorie ->
                            val questsForCategory = quests.filter { it.categorie == categorie.id }

                            if (questsForCategory.isNotEmpty()) {
                                QuestCategory(
                                    categorie = categorie,
                                    quests = questsForCategory,
                                    context = context,
                                    onQuestStateChanged = { _, isDone ->
                                        // Mettre à jour le compteur de quêtes terminées
                                        if (isDone) {
                                            completedQuestsCount++
                                        } else {
                                            completedQuestsCount--
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
