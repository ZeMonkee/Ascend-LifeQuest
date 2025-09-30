package com.example.ascendlifequest.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.components.FriendItem
import com.example.ascendlifequest.fake_data.F_Users

@Composable
fun FriendScreen(navController: NavHostController) {
    // TEST USER
    val friendsUsers = F_Users

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Amis) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quest")
                    BottomNavItem.Classement -> navController.navigate("classement") // Navigate to ranking
                    BottomNavItem.Amis -> {} // Already on this screen
                    BottomNavItem.Parametres -> navController.navigate("parametres")
                }
            }
        }
    ) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                AppHeader(
                    title = "AMIS",
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(friendsUsers) { user ->
                        FriendItem(user)
                    }
                }
            }
        }
    }
}