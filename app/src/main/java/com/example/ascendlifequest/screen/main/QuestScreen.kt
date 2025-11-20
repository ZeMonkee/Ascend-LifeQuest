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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.*
import com.example.ascendlifequest.components.main.QuestCategory
import com.example.ascendlifequest.model.Categorie
import com.example.ascendlifequest.model.Quest
import com.example.ascendlifequest.repository.QuestRepository
import com.example.ascendlifequest.repository.generateQuestForCategory
import com.example.ascendlifequest.ui.theme.AppColor
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun QuestScreen(navController: NavHostController) {
    val repository = remember { QuestRepository() }
    var categories by remember { mutableStateOf<List<Categorie>>(emptyList()) }
    var quests by remember { mutableStateOf<List<Quest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refreshData() {
        scope.launch {
            isLoading = true
            val loadedCategories = repository.getCategories()
            val loadedQuests = repository.getQuests()

            categories = loadedCategories.map { cat ->
                val restoredColor = Color(cat.couleur.value)
                cat.copy(couleur = restoredColor)
            }
            quests = loadedQuests
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
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
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray,
                        strokeCap = StrokeCap.Butt,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("60%", fontSize = 16.sp, color = AppColor.MinusTextColor)

                    // 🔘 BOUTON CRÉER UNE QUÊTE
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val randomCategory = categories.randomOrNull()
                                if (randomCategory != null) {
                                    val newQuest = generateQuestForCategory(randomCategory)
                                    if (newQuest != null) {
                                        Log.d("QuestScreen", "✅ Quête générée : ${newQuest.nom}")
                                        refreshData()
                                    } else {
                                        Log.e("QuestScreen", "❌ Échec génération quête")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Créer une quête")
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
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
                                    context = LocalContext.current
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}