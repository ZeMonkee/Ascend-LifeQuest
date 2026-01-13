package com.example.ascendlifequest.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.settings.PreferenceQuestion
import com.example.ascendlifequest.helpers.PreferencesHelper
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
                    PreferenceQuestion(
                        question = "A quel point aimez-vous le sport ?",
                        color = AppColor.SportColor,
                        context = LocalContext.current,
                        preferenceKey = PreferencesHelper.KEY_SPORT
                    )

                    PreferenceQuestion(
                        question = "A quel point aimez-vous la cuisine ?",
                        color = AppColor.CuisineColor,
                        context = LocalContext.current,
                        preferenceKey = PreferencesHelper.KEY_CUISINE
                    )

                    PreferenceQuestion(
                        question = "A quel point aimez-vous les jeux vidéo ?",
                        color = AppColor.JeuxVideoColor,
                        context = LocalContext.current,
                        preferenceKey = PreferencesHelper.KEY_JEUX_VIDEO
                    )

                    PreferenceQuestion(
                        question = "A quel point aimez-vous la lecture ?",
                        color = AppColor.LectureColor,
                        context = LocalContext.current,
                        preferenceKey = PreferencesHelper.KEY_LECTURE
                    )
                }
            }
        }
    }
}
