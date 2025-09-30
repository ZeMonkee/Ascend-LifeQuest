package com.example.ascendlifequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ascendlifequest.screen.main.ClassementScreen
import com.example.ascendlifequest.screen.main.FriendScreen
import com.example.ascendlifequest.screen.login.LoginOptionScreen
import com.example.ascendlifequest.screen.login.LoginScreen
import com.example.ascendlifequest.screen.main.ProfilScreen
import com.example.ascendlifequest.screen.main.QuestScreen
import com.example.ascendlifequest.screen.main.SettingScreen
import com.example.ascendlifequest.screen.settings.PreferenceScreen
import com.example.ascendlifequest.ui.theme.AscendLifeQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AscendLifeQuestTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login_option") {
                    composable("login_option") { LoginOptionScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("quetes") { QuestScreen(navController) }
                    composable("classement") { ClassementScreen(navController) }
                    composable("amis") { FriendScreen(navController) }
                    composable("parametres") { SettingScreen(navController) }
                    composable("profil") { ProfilScreen(navController) }
                    composable("preference") { PreferenceScreen(navController)}
                }

            }
        }
    }
}
