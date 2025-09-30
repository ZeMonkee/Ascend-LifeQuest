package com.example.ascendlifequest.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.QuestCategory
import com.example.ascendlifequest.ui.theme.AppColor

data class QuestItem(val title: String, val xp: Int, val done: Boolean)

@Composable
fun QuestScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Quetes) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> {} // Déjà sur cet écran
                    BottomNavItem.Classement -> navController.navigate("classement")
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Parametres -> navController.navigate("parametres")
                }
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
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("60%", fontSize = 16.sp, color = AppColor.MinusTextColor)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    QuestCategory(
                        title = "Sport",
                        color = AppColor.SportColor,
                        quests = listOf(
                            QuestItem("Faire 100 pompes", 400, done = true),
                            QuestItem("Faire 100 squats", 300, done = false),
                            QuestItem("Courir 10 km", 350, done = false),
                        ),
                        iconRes = R.drawable.icon_sport
                    )

                    QuestCategory(
                        title = "Cuisine",
                        color = AppColor.CuisineColor,
                        quests = listOf(
                            QuestItem("Faire une salade", 150, done = false),
                            QuestItem("Faire un plat italien", 250, done = true),
                        ),
                        iconRes = R.drawable.icon_cuisine
                    )

                    QuestCategory(
                        title = "Jeux Vidéo",
                        color = AppColor.JeuxVideoColor,
                        quests = listOf(
                            QuestItem("Faire une partie classée", 200, done = true),
                            QuestItem("Jouer 30 minutes", 100, done = false),
                        ),
                        iconRes = R.drawable.icon_jeux_video
                    )

                    QuestCategory(
                        title = "Etudes",
                        color = AppColor.EtudesColor,
                        quests = listOf(
                            QuestItem("Faire ses devoirs", 350, done = false),
                            QuestItem("Réviser 30 minutes", 200, done = false),
                            QuestItem("Préparer ses affaires", 100, done = false),
                        ),
                        iconRes = R.drawable.icon_etudes
                    )
                }
            }
        }
    }
}
