package com.example.ascendlifequest.ui.features.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.settings.components.SettingsItem
import com.example.ascendlifequest.di.AppViewModelFactory

@Composable
fun SettingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val factory = AppViewModelFactory()
    val viewModel: SettingsViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when {
                ev == "SIGNED_OUT" -> {
                    Toast.makeText(context, "Vous avez été déconnecté", Toast.LENGTH_SHORT).show()
                    navController.navigate("login_option") { popUpTo(0) { inclusive = true } }
                }
                ev.startsWith("SIGNOUT_FAILED") -> {
                    Toast.makeText(context, ev.removePrefix("SIGNOUT_FAILED: "), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    AppBottomNavBar(navController, BottomNavItem.Parametres) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppHeader(
                    title = "PARAMETRES",
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Options de paramètres
                    SettingsItem("Comptes", R.drawable.icon_quetes){
                        navController.navigate("account")
                    }
                    SettingsItem("Préférences", R.drawable.icon_quetes) {
                        navController.navigate("preference")
                    }
                    // Bouton de déconnexion
                    SettingsItem("Se déconnecter", R.drawable.icon_quetes){ _ ->
                        viewModel.signOut()
                    }

                }
            }
        }
    }
}