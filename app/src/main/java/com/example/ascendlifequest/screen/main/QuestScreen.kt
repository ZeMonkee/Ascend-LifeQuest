package com.example.ascendlifequest.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ascendlifequest.fake_data.F_Categorie
import com.example.ascendlifequest.fake_data.F_Quests
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun QuestScreen(navController: NavHostController) {
    AppBottomNavBar(navController, BottomNavItem.Quetes) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppHeader(
                    title = "QUÊTES",
                )
                // Barre de progression
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = {
                            0.6f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.LightGray,
                        strokeCap = StrokeCap.Butt,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("60%", fontSize = 16.sp, color = AppColor.MinusTextColor)
                }

                // Utilisation de LazyColumn pour une meilleure performance avec les listes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    // Itération sur la liste des catégories
                    items(F_Categorie) { categorie ->
                        // Filtrage des quêtes pour la catégorie actuelle
                        val questsForCategory = F_Quests.filter { it.categorie == categorie.id }

                        // Affichage de la catégorie et de ses quêtes si la liste n'est pas vide
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
