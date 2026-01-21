package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.data.model.UserProfile

@Composable
fun FriendsContent(
        friends: List<UserProfile>,
        navController: NavHostController,
        onDeleteFriend: (UserProfile) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyFriendsContent()
    } else {
        LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendItem(
                        user = friend,
                        onMessageClick = { navController.navigate("chat/${friend.uid}") },
                        onClick = { navController.navigate("profil/${friend.uid}") },
                        onLongPress = { onDeleteFriend(friend) }
                )
            }
        }
    }
}
