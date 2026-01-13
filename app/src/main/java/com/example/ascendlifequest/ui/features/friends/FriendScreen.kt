package com.example.ascendlifequest.ui.features.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.friends.components.FriendItem
import com.example.ascendlifequest.data.fakes.F_Users

@Composable
fun FriendScreen(navController: NavHostController) {
    // TEST USER
    val friendsUsers = F_Users

    AppBottomNavBar(navController, BottomNavItem.Amis) { innerPadding ->
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