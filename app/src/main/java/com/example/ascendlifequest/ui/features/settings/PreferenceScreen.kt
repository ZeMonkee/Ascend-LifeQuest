package com.example.ascendlifequest.ui.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.settings.components.PreferenceQuestion
import com.example.ascendlifequest.data.local.PreferencesHelper
import com.example.ascendlifequest.ui.theme.themeColors
import com.example.ascendlifequest.data.remote.AuthService

@Composable
fun PreferenceScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthService() }
    val userId = authService.getUserId()
    val prefsVm = remember { PreferencesViewModel(context, userId) }
    val colors = themeColors()

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
                    val sport by prefsVm.getPreferenceFlow(PreferencesHelper.KEY_SPORT).collectAsState()
                    PreferenceQuestion(
                        question = "A quel point aimez-vous le sport ?",
                        color = colors.sport,
                        selected = sport,
                        onSelectedChange = { prefsVm.savePreference(PreferencesHelper.KEY_SPORT, it) }
                    )

                    val cuisine by prefsVm.getPreferenceFlow(PreferencesHelper.KEY_CUISINE).collectAsState()
                    PreferenceQuestion(
                        question = "A quel point aimez-vous la cuisine ?",
                        color = colors.cuisine,
                        selected = cuisine,
                        onSelectedChange = { prefsVm.savePreference(PreferencesHelper.KEY_CUISINE, it) }
                    )

                    val jeux by prefsVm.getPreferenceFlow(PreferencesHelper.KEY_JEUX_VIDEO).collectAsState()
                    PreferenceQuestion(
                        question = "A quel point aimez-vous les jeux vidéo ?",
                        color = colors.jeuxVideo,
                        selected = jeux,
                        onSelectedChange = { prefsVm.savePreference(PreferencesHelper.KEY_JEUX_VIDEO, it) }
                    )

                    val lecture by prefsVm.getPreferenceFlow(PreferencesHelper.KEY_LECTURE).collectAsState()
                    PreferenceQuestion(
                        question = "A quel point aimez-vous la lecture ?",
                        color = colors.lecture,
                        selected = lecture,
                        onSelectedChange = { prefsVm.savePreference(PreferencesHelper.KEY_LECTURE, it) }
                    )
                }
            }
        }
    }
}
