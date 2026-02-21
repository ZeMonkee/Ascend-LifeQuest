package com.example.ascendlifequest

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ascendlifequest.data.network.OfflineModeProvider
import com.example.ascendlifequest.di.AppContextProvider
import com.example.ascendlifequest.ui.components.PermissionRequester
import com.example.ascendlifequest.ui.features.auth.LoginOptionScreen
import com.example.ascendlifequest.ui.features.auth.LoginScreen
import com.example.ascendlifequest.ui.features.auth.RegisterScreen
import com.example.ascendlifequest.ui.features.chat.ChatScreen
import com.example.ascendlifequest.ui.features.friends.FriendScreen
import com.example.ascendlifequest.ui.features.leaderboard.ClassementScreen
import com.example.ascendlifequest.ui.features.profile.AccountScreen
import com.example.ascendlifequest.ui.features.profile.ProfilScreen
import com.example.ascendlifequest.ui.features.quest.QuestScreen
import com.example.ascendlifequest.ui.features.settings.PreferenceScreen
import com.example.ascendlifequest.ui.features.settings.SettingScreen
import com.example.ascendlifequest.ui.features.settings.ThemeCreatorScreen
import com.example.ascendlifequest.ui.features.settings.ThemeScreen
import com.example.ascendlifequest.ui.theme.AppThemeProvider
import com.example.ascendlifequest.ui.theme.AscendLifeQuestTheme
import com.example.ascendlifequest.ui.theme.ThemeViewModel
import com.google.firebase.FirebaseApp

private const val TAG = "MainActivity"

/** Main entry point for the application. Handles Firebase initialization and navigation setup. */
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser le provider de contexte pour les repositories
        AppContextProvider.initialize(this)

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
        enableEdgeToEdge()
        setContent {
            val themeViewModel = ThemeViewModel(this)

            AscendLifeQuestTheme {
                AppThemeProvider(themeViewModel = themeViewModel) {
                    // Provider pour le mode hors ligne (accessible dans toute l'app)
                    OfflineModeProvider {
                        val navController = rememberNavController()
                        PermissionRequester()
                        NavHost(navController = navController, startDestination = "login_option") {
                            composable("login_option") { LoginOptionScreen(navController) }
                            composable("login") { LoginScreen(navController) }
                            composable("register") { RegisterScreen(navController) }
                            composable("quetes") { QuestScreen(navController) }
                            // Route pour le mode hors ligne (mêmes écrans mais avec données locales)
                            composable("quetes_offline") { QuestScreen(navController) }
                            composable("classement") { ClassementScreen(navController) }
                            composable("amis") { FriendScreen(navController) }
                            composable("parametres") { SettingScreen(navController) }
                            composable("account") { AccountScreen(navController) }
                            composable("profil") { ProfilScreen(navController) }
                            composable("preference") { PreferenceScreen(navController) }
                            composable("theme") { ThemeScreen(navController, themeViewModel) }
                            composable("theme_creator") { ThemeCreatorScreen(navController, themeViewModel) }
                            composable(
                                    route = "profil/{userId}",
                                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("userId")
                                ProfilScreen(navController = navController, userId = userId)
                            }
                            composable(
                                    route = "chat/{friendId}",
                                    arguments =
                                            listOf(navArgument("friendId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                                ChatScreen(navController = navController, friendId = friendId)
                            }
                        }
                    }

                    // Badge d'environnement en haut à gauche (visible uniquement en DEV et PREPROD)
                    EnvironmentBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    )
                }
            }
        }
    }
}
