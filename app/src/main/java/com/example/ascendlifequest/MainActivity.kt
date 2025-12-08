package com.example.ascendlifequest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ascendlifequest.components.datastorage.queststorage
import com.example.ascendlifequest.screen.login.LoginOptionScreen
import com.example.ascendlifequest.screen.login.LoginScreen
import com.example.ascendlifequest.screen.login.RegisterScreen
import com.example.ascendlifequest.screen.main.ClassementScreen
import com.example.ascendlifequest.screen.main.FriendScreen
import com.example.ascendlifequest.screen.main.ProfilScreen
import com.example.ascendlifequest.screen.main.QuestScreen
import com.example.ascendlifequest.screen.main.SettingScreen
import com.example.ascendlifequest.screen.settings.PreferenceScreen
import com.example.ascendlifequest.ui.theme.AscendLifeQuestTheme
import com.google.firebase.FirebaseApp
import com.example.ascendlifequest.screen.main.AccountScreen
import com.example.ascendlifequest.components.PermissionRequester

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase initialisé avec succès")
            } else {
                Log.d(TAG, "Firebase déjà initialisé")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'initialisation de Firebase", e)
        }
        queststorage().uploadFakeData()
        enableEdgeToEdge()
        setContent {
            AscendLifeQuestTheme {
                val navController = rememberNavController()
                // Request location permission once on app start (first launch will trigger permission prompt)
                PermissionRequester()
                NavHost(navController = navController, startDestination = "login_option") {
                    composable("login_option") { LoginOptionScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("quetes") { QuestScreen(navController) }
                    composable("classement") { ClassementScreen(navController) }
                    composable("amis") { FriendScreen(navController) }
                    composable("parametres") { SettingScreen(navController) }
                    composable("account") { AccountScreen(navController) }
                    composable("profil") { ProfilScreen(navController) }
                    composable("preference") { PreferenceScreen(navController) }
                }
            }
        }
    }

}
