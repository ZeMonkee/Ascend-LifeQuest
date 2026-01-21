package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        onClick: () -> Unit = {},
        onLongPress: () -> Unit = {}
) {
        val backgroundColor = AppColor.DarkBlueColor

        Card(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(70.dp)
                                .combinedClickable(onClick = onClick, onLongClick = onLongPress),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
                Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Avatar
                        Image(
                                painter = painterResource(id = R.drawable.generic_pfp),
                                contentDescription = "Avatar de ${user.pseudo}",
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                        )

                        // Spacer
                        Spacer(modifier = Modifier.width(16.dp))

                        // User info
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = user.pseudo,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColor.MainTextColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        IconButton(onClick = onMessageClick, modifier = Modifier.size(40.dp)) {
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

/** Composant pour afficher une demande d'ami en attente (tuile grisée avec accepter/refuser) */
@Composable
fun FriendRequestItem(user: UserProfile, onAccept: () -> Unit, onDecline: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = AppColor.DarkBlueColor.copy(alpha = 0.6f)
                        )
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                color = AppColor.MinusTextColor.copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Avatar
                        Image(
                                painter = painterResource(id = R.drawable.generic_pfp),
                                contentDescription = "Avatar de ${user.pseudo}",
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // User info
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = user.pseudo,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColor.MainTextColor
                                )
                                Text(
                                        text = "Veut devenir votre ami",
                                        fontSize = 12.sp,
                                        color = AppColor.MinusTextColor
                                )
                                Text(
                                        text = "${user.xp} XP • Niveau ${user.calculateLevel()}",
                                        fontSize = 11.sp,
                                        color = AppColor.MinusTextColor.copy(alpha = 0.7f)
                                )
                        }

                        // Boutons Accepter / Refuser
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Bouton Refuser
                                IconButton(
                                        onClick = onDecline,
                                        modifier =
                                                Modifier.size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                AppColor.SportColor.copy(
                                                                        alpha = 0.8f
                                                                )
                                                        )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Refuser",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                        )
                                }

                                // Bouton Accepter
                                IconButton(
                                        onClick = onAccept,
                                        modifier =
                                                Modifier.size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                                AppColor.LectureColor.copy(
                                                                        alpha = 0.8f
                                                                )
                                                        )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Accepter",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                        )
                                }
                        }
                }
        }
}

@Composable
fun SearchUserItem(user: UserProfile, onAddClick: () -> Unit, isAdding: Boolean = false) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Avatar
                        Image(
                                painter = painterResource(id = R.drawable.generic_pfp),
                                contentDescription = "Avatar de ${user.pseudo}",
                                modifier = Modifier.size(45.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // User info
                        Column(modifier = Modifier.weight(1f)) {
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

                        // Bouton Envoyer demande
                        Button(
                                onClick = onAddClick,
                                enabled = !isAdding,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = AppColor.LightBlueColor,
                                                disabledContainerColor =
                                                        AppColor.LightBlueColor.copy(alpha = 0.5f)
                                        ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                                if (isAdding) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = AppColor.MainTextColor,
                                                strokeWidth = 2.dp
                                        )
                                } else {
                                        Text(text = "Demander", fontSize = 13.sp)
                                }
                        }
                }
        }
}

/** Composant pour afficher une notification (ex: demande d'ami refusée) */
@Composable
fun NotificationItem(message: String, onDismiss: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = AppColor.DarkBlueColor.copy(alpha = 0.7f)
                        )
        ) {
                Row(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(color = AppColor.SportColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Icône
                        Box(
                                modifier =
                                        Modifier.size(40.dp)
                                                .clip(CircleShape)
                                                .background(AppColor.SportColor.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = AppColor.MainTextColor,
                                        modifier = Modifier.size(24.dp)
                                )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Message
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = message,
                                        fontSize = 14.sp,
                                        color = AppColor.MainTextColor
                                )
                        }

                        // Bouton fermer
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = AppColor.MinusTextColor,
                                        modifier = Modifier.size(18.dp)
                                )
                        }
                }
        }
}
