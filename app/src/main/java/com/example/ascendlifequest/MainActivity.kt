package com.example.ascendlifequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ascendlifequest.screen.LoginOptionScreen
import com.example.ascendlifequest.screen.LoginScreen
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
                }
            }
        }
    }
}
