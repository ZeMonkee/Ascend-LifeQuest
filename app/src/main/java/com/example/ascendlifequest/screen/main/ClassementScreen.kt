package com.example.ascendlifequest.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.main.RankingItem
import com.example.ascendlifequest.fake_data.F_Users

@Composable
fun ClassementScreen(navController: NavHostController) {
    // User
    val rankingUsers = F_Users
    AppBottomNavBar(navController, BottomNavItem.Classement) { innerPadding ->
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