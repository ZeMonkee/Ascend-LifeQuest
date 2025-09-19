package com.example.ascendlifequest.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.*

data class QuestItem(val title: String, val xp: Int, val done: Boolean)

@Composable
fun QuestScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Quetes) { selected -> /* TODO */
            }
        }
    ) { innerPadding ->

        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppHeader(
                    title = "QUÊTES",
                    subtitle = "950/2400 XP",
                    progress = 0.39f
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    QuestCategory(
                        title = "Sport",
                        color = Color(0xFFE74C3C),
                        quests = listOf(
                            QuestItem("Faire 100 pompes", 400, done = true),
                            QuestItem("Faire 100 squats", 300, done = false),
                            QuestItem("Courir 10 km", 350, done = false),
                        )
                    )

                    QuestCategory(
                        title = "Cuisine",
                        color = Color(0xFFF39C12),
                        quests = listOf(
                            QuestItem("Faire une salade", 150, done = false),
                            QuestItem("Faire un plat italien", 250, done = true),
                        )
                    )

                    QuestCategory(
                        title = "Jeux Vidéo",
                        color = Color(0xFF9B59B6),
                        quests = listOf(
                            QuestItem("Faire une partie classée", 200, done = true),
                            QuestItem("Jouer 30 minutes", 100, done = false),
                        )
                    )

                    QuestCategory(
                        title = "Etudes",
                        color = Color(0xFF27AE60),
                        quests = listOf(
                            QuestItem("Faire ses devoirs", 350, done = false),
                            QuestItem("Réviser 30 minutes", 200, done = false),
                            QuestItem("Préparer ses affaires", 100, done = false),
                        )
                    )

                    QuestCategory(
                        title = "Etudes",
                        color = Color(0xFF27AE60),
                        quests = listOf(
                            QuestItem("Faire ses devoirs", 350, done = false),
                            QuestItem("Réviser 30 minutes", 200, done = false),
                            QuestItem("Préparer ses affaires", 100, done = false),
                        )
                    )

                    QuestCategory(
                        title = "Etudes",
                        color = Color(0xFF27AE60),
                        quests = listOf(
                            QuestItem("Faire ses devoirs", 350, done = false),
                            QuestItem("Réviser 30 minutes", 200, done = false),
                            QuestItem("Préparer ses affaires", 100, done = false),
                        )
                    )
                }
            }
        }
    }
}
