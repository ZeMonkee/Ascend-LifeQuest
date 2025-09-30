package com.example.ascendlifequest.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.SettingsItem

@Composable
fun SettingScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Amis) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quest")
                    BottomNavItem.Classement -> navController.navigate("classement")
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Parametres -> {} // Actual
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
                    title = "PARAMETRES",
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Test settings
                    SettingsItem("Comptes", R.drawable.icon_quetes)
                    SettingsItem("Notifications", R.drawable.icon_quetes)
                    SettingsItem("Thèmes", R.drawable.icon_quetes)
                    SettingsItem("Préférences", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)
                    SettingsItem("Autres", R.drawable.icon_quetes)

                }
            }
        }
    }
}