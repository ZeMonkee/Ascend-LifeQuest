package com.example.ascendlifequest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ascendlifequest.notifications.NotificationHelper
import com.example.ascendlifequest.notifications.NotificationScheduler
import com.example.ascendlifequest.notifications.NotificationManager
import com.example.ascendlifequest.ui.components.PermissionRequester
import com.google.firebase.auth.FirebaseAuth
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
import com.example.ascendlifequest.ui.theme.AscendLifeQuestTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

private const val TAG = "MainActivity"

/** Main entry point for the application. Handles Firebase initialization and navigation setup. */
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    private var notificationManager: NotificationManager? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permission de notification accordée")
            initializeNotifications()
        } else {
            Log.d(TAG, "Permission de notification refusée")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        // Initialiser les canaux de notification
        NotificationHelper.createNotificationChannels(this)

        // Demander la permission pour les notifications (Android 13+)
        askNotificationPermission()

        // Obtenir le token FCM
        getFCMToken()

        // Planifier les notifications quotidiennes (à 9h00 par défaut)
        NotificationScheduler.scheduleDailyQuestNotification(this, 9, 0)

        enableEdgeToEdge()
        setContent {
            AscendLifeQuestTheme {
                val navController = rememberNavController()
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
        }
    }

    private fun askNotificationPermission() {
        // Vérifier si nous devons demander la permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                initializeNotifications()
            }
        } else {
            initializeNotifications()
        }
    }

    private fun initializeNotifications() {
        Log.d(TAG, "Notifications initialisées avec succès")

        // Démarrer l'écoute des notifications si l'utilisateur est connecté
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d(TAG, "Démarrage de l'écoute des notifications pour l'utilisateur: $userId")
            notificationManager = NotificationManager.getInstance(this)
            notificationManager?.startListening(userId)
        } else {
            Log.d(TAG, "Aucun utilisateur connecté, écoute des notifications non démarrée")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêter l'écoute des notifications
        notificationManager?.stopListening()
        Log.d(TAG, "NotificationManager arrêté")
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Échec de récupération du token FCM", task.exception)
                return@addOnCompleteListener
            }

            // Récupérer le nouveau token FCM
            val token = task.result
            Log.d(TAG, "Token FCM: $token")

            // TODO: Sauvegarder le token dans Firestore pour l'utilisateur connecté
            // Vous pourrez implémenter cela quand l'utilisateur se connecte
        }
    }
}
