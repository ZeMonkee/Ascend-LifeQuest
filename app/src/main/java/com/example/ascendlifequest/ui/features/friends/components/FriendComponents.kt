package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.theme.AppColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendItem(
    user: UserProfile,
    onMessageClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val backgroundColor = AppColor.DarkBlueColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.generic_pfp),
                contentDescription = "Avatar de ${user.pseudo}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Spacer
            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.pseudo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.MainTextColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${user.xp} XP",
                        fontSize = 14.sp,
                        color = AppColor.MinusTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Niveau ${user.calculateLevel()}",
                        fontSize = 14.sp,
                        color = AppColor.MinusTextColor
                    )
                }
            }

            // Bouton Message
            IconButton(
                onClick = onMessageClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Envoyer un message",
                    tint = AppColor.LightBlueColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SearchUserItem(
    user: UserProfile,
    onAddClick: () -> Unit,
    isAdding: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.generic_pfp),
                contentDescription = "Avatar de ${user.pseudo}",
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.pseudo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.MainTextColor
                )
                Text(
                    text = "${user.xp} XP • Niveau ${user.calculateLevel()}",
                    fontSize = 12.sp,
                    color = AppColor.MinusTextColor
                )
            }

            // Bouton Ajouter
            Button(
                onClick = onAddClick,
                enabled = !isAdding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColor.LightBlueColor,
                    disabledContainerColor = AppColor.LightBlueColor.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AppColor.MainTextColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Ajouter",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}