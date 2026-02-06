package com.example.ascendlifequest.ui.features.leaderboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun RankingItem(user: UserProfile, onClick: () -> Unit) {
    val colors = themeColors()
    val backgroundColor = colors.darkBackground

    val rankingColor =
            when (user.rang) {
                1 -> colors.or // Or
                2 -> colors.argent // Argent
                3 -> colors.bronze // Bronze
                else -> colors.minusText
            }

    Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Rang avec trophée pour top 3
            Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .border(
                            width = 2.dp,
                            color = rankingColor,
                            shape = CircleShape
                        )
            ) {
                if (user.rang <= 3) {
                    // Trophée pour le top 3
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = "Trophée rang ${user.rang}",
                        tint = rankingColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    // Numéro pour les autres rangs
                    Text(
                            text = user.rang.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = rankingColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = user.pseudo.ifEmpty { "Utilisateur" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.mainText
                )
                Text(
                    text = "Niveau ${user.calculateLevel()} • ${user.quetesRealisees} quêtes",
                    fontSize = 12.sp,
                    color = colors.minusText
                )
            }

            // XP affiché à droite
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${user.xp}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankingColor
                )
                Text(
                    text = "XP",
                    fontSize = 12.sp,
                    color = colors.minusText
                )
            }
        }
    }
}
