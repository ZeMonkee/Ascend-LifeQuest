package com.example.ascendlifequest.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.RankingItem
import com.example.ascendlifequest.fake_data.F_Users

@Composable
fun ClassementScreen(navController: NavHostController) {
    // User
    val rankingUsers = F_Users

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Classement) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quetes")
                    BottomNavItem.Classement -> {} // Déjà sur cet écran
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Parametres -> navController.navigate("parametres")
                    BottomNavItem.Profil -> navController.navigate("profil")
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
                    title = "CLASSEMENTS",
                )

                // Rankings List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rankingUsers) { user ->
                        RankingItem(user)
                    }
                }
            }
        }
    }
}