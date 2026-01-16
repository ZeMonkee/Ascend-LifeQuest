package com.example.ascendlifequest.ui.features.quest

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
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.di.AppViewModelFactory
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.quest.components.QuestCategory
import com.example.ascendlifequest.ui.theme.AppColor
import com.example.ascendlifequest.util.QuestHelper

@Composable
fun QuestScreen(
        navController: NavHostController,
        viewModel: QuestViewModel =
                viewModel(
                        factory =
                                AppViewModelFactory(
                                        questRepository = QuestRepository(LocalContext.current)
                                )
                )
) {
        val context = LocalContext.current
        val authService = remember { AuthService() }
        val userId = authService.getUserId()

        val categories by viewModel.categories.collectAsState()
        val quests by viewModel.quests.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val isGenerating by viewModel.isGenerating.collectAsState()
        val generationProgress by viewModel.generationProgress.collectAsState()
        val questCounter by viewModel.questCounter.collectAsState()
        val completedQuestsCount by viewModel.completedQuestsCount.collectAsState()
        val showMaxQuestsDialog by viewModel.showMaxQuestsDialog.collectAsState()

        val maxQuests = QuestHelper.getMaxQuests()

        LaunchedEffect(Unit) {
                viewModel.generateInitialQuests(context, userId)
                viewModel.loadData(context, userId)
        }

        // 🔥 Dialog pour afficher le max atteint
        if (showMaxQuestsDialog) {
                AlertDialog(
                        onDismissRequest = { viewModel.dismissDialog() },
                        title = { Text(text = "Limite atteinte", color = AppColor.MainTextColor) },
                        text = {
                                Text(
                                        text =
                                                "Nombre maximum de quêtes atteint ($maxQuests/$maxQuests).\n\nVidez la base de données pour générer de nouvelles quêtes.",
                                        color = AppColor.MinusTextColor
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = { viewModel.dismissDialog() },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = AppColor.LightBlueColor,
                                                        contentColor = AppColor.MainTextColor
                                                )
                                ) { Text("OK") }
                        },
                        containerColor = AppColor.DarkBlueColor
                )
        }

        AppBottomNavBar(navController, BottomNavItem.Quetes) { innerPadding ->
                AppBackground {
                        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                AppHeader(title = "QUÊTES")

                                Column(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 8.dp
                                                        ),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        // Barre de progression des quêtes terminées
                                        val totalQuests = quests.size.coerceAtLeast(1)
                                        LinearProgressIndicator(
                                                progress = {
                                                        completedQuestsCount.toFloat() / totalQuests
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                color = AppColor.LectureColor,
                                                trackColor =
                                                        AppColor.MinusTextColor.copy(alpha = 0.3f),
                                                strokeCap = StrokeCap.Round,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                                text =
                                                        "$completedQuestsCount/${quests.size} quêtes terminées",
                                                fontSize = 14.sp,
                                                color = AppColor.MinusTextColor
                                        )

                                        // 🔘 BOUTON CRÉER UNE QUÊTE
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                                onClick = {
                                                        viewModel.generateNewQuest(context, userId)
                                                },
                                                enabled = !isGenerating,
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        AppColor.LightBlueColor,
                                                                contentColor =
                                                                        AppColor.MainTextColor,
                                                                disabledContainerColor =
                                                                        AppColor.LightBlueColor
                                                                                .copy(alpha = 0.5f),
                                                                disabledContentColor =
                                                                        AppColor.MainTextColor.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        ) { Text("Créer une quête") }

                                        // BOUTON POUR VIDER LA BASE DE DONNÉES
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                                onClick = {
                                                        viewModel.clearDatabase(context, userId)
                                                },
                                                enabled = !isGenerating,
                                                colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                                contentColor =
                                                                        AppColor.MinusTextColor
                                                        )
                                        ) { Text("Vider la BDD (Debug)") }
                                }

                                // 🔥 Loader pendant la génération initiale
                                if (isGenerating) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Column(
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                ) {
                                                        CircularProgressIndicator(
                                                                color = AppColor.LightBlueColor
                                                        )
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                                text = "Génération des quêtes...",
                                                                fontSize = 18.sp,
                                                                color = AppColor.MainTextColor
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                                text =
                                                                        "$generationProgress/$maxQuests",
                                                                fontSize = 24.sp,
                                                                color = AppColor.LightBlueColor
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        LinearProgressIndicator(
                                                                progress = {
                                                                        generationProgress
                                                                                .toFloat() /
                                                                                maxQuests
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        horizontal =
                                                                                                48.dp
                                                                                ),
                                                                color = AppColor.LectureColor,
                                                                trackColor =
                                                                        AppColor.MinusTextColor
                                                                                .copy(alpha = 0.3f),
                                                                strokeCap = StrokeCap.Round,
                                                        )
                                                }
                                        }
                                } else if (isLoading) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                CircularProgressIndicator(
                                                        color = AppColor.LightBlueColor
                                                )
                                        }
                                } else {
                                        LazyColumn(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(horizontal = 16.dp)
                                        ) {
                                                items(categories) { categorie ->
                                                        val questsForCategory =
                                                                quests.filter {
                                                                        it.categorie == categorie.id
                                                                }

                                                        if (questsForCategory.isNotEmpty()) {
                                                                QuestCategory(
                                                                        categorie = categorie,
                                                                        quests = questsForCategory,
                                                                        context = context,
                                                                        onQuestStateChanged = {
                                                                                _,
                                                                                isDone ->
                                                                                viewModel
                                                                                        .updateQuestState(
                                                                                                isDone
                                                                                        )
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
