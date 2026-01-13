package com.example.ascendlifequest.ui.features.leaderboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.model.User
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun RankingItem(user: User) {
    val backgroundColor = AppColor.DarkBlueColor

    val rankingColor = when (user.rang) {
        1 -> AppColor.Or // Or
        2 -> AppColor.Argent // Argent
        3 -> AppColor.Bronze // Bronze
        else -> AppColor.MainTextColor
    }

    val rankBackgroundColor = when (user.rang) {
        1 -> AppColor.Or // Or
        2 -> AppColor.Argent // Argent
        3 -> AppColor.Bronze // Bronze
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number with special background for top 3
            if (user.rang <= 3) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(rankBackgroundColor, CircleShape)
                ) {
                    Text(
                        text = user.rang.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                }
            } else {

                Text(
                    text = user.rang.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankingColor,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }

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
                Text(
                    text = "${user.xp} XP",
                    fontSize = 16.sp,
                    color = AppColor.MinusTextColor
                )
            }

            // Avatar - plus grand
            Image(
                painter = painterResource(id = user.photoUrl ?: R.drawable.generic_pfp),
                contentDescription = "Avatar de ${user.pseudo}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}