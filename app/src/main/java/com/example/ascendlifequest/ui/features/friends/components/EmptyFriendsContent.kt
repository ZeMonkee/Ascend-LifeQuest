package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun EmptyFriendsContent(isOffline: Boolean = false) {
    val colors = themeColors()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
        ) {
            Text(
                    text = "Aucun ami pour le moment",
                    color = colors.mainText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = if (isOffline)
                        "Connectez-vous à Internet pour ajouter des amis"
                    else
                        "Appuyez sur + pour ajouter des amis",
                    color = colors.minusText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
            )
        }
    }
}
