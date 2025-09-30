package com.example.ascendlifequest.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.settings.PreferenceQuestion
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun PreferenceScreen(navController: NavHostController) {
    AppBottomNavBar(navController, BottomNavItem.Parametres) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                AppHeader(title = "PRÉFÉRENCES")

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    PreferenceQuestion("A quel point aimez-vous le sport ?", AppColor.SportColor)
                    PreferenceQuestion("A quel point aimez-vous la cuisine ?", AppColor.CuisineColor)
                    PreferenceQuestion("A quel point aimez-vous les jeux vidéo ?", AppColor.JeuxVideoColor)
                    PreferenceQuestion("A quel point aimez-vous la lecture ?", AppColor.EtudesColor)
                    PreferenceQuestion("A quel point aimez-vous le dessin ?", AppColor.DessinColor)
                }
            }
        }
    }
}
