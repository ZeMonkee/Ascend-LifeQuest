package com.example.ascendlifequest.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.AppBottomNavBar
import com.example.ascendlifequest.components.AppHeader
import com.example.ascendlifequest.components.BottomNavItem
import com.example.ascendlifequest.model.RankingUser

@Composable
fun ClassementScreen(navController: NavHostController) {
    val rankingUsers = listOf(
        RankingUser(1, "Monkee", 99999, R.drawable.generic_pfp),
        RankingUser(2, "ArcLeRetour", 99996, R.drawable.generic_pfp),
        RankingUser(3, "Acno", 84826, R.drawable.generic_pfp),
        RankingUser(4, "Kohlt", 75815, R.drawable.generic_pfp),
        RankingUser(5, "Luca", 60150, R.drawable.generic_pfp),
        RankingUser(6, "FruitySkyLine", 60084, R.drawable.generic_pfp),
        RankingUser(7, "NoFastOne", 57515, R.drawable.generic_pfp),
        RankingUser(8, "CookySensei", 54641, R.drawable.generic_pfp),
        RankingUser(9, "LesLuong", 52674, R.drawable.generic_pfp),
        RankingUser(10, "Lippido", 51234, R.drawable.generic_pfp),
        RankingUser(11, "AnthonyBiv", 50516, R.drawable.generic_pfp),
        RankingUser(12, "AnthonyBiv²", 49999, R.drawable.generic_pfp)
    )
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Classement) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quest")
                    BottomNavItem.Classement -> {} // Déjà sur cet écran
                    BottomNavItem.Amis -> {}
                    BottomNavItem.Parametres -> {}
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
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing between items
                ) {
                    items(rankingUsers) { user ->
                        RankingItem(user)
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(user: RankingUser) {
    val backgroundColor = Color(0xFF2E3F78) // Bleu foncé pour toutes les tuiles

    val rankingColor = when (user.position) {
        1 -> Color(0xFFFFD700) // Or
        2 -> Color(0xFFC0C0C0) // Argent
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White
    }

    val rankBackgroundColor = when (user.position) {
        1 -> Color(0xFFF39C12) // Fond orange/or pour le premier
        2 -> Color(0xFF95A5A6) // Fond gris argenté pour le second
        3 -> Color(0xFFCD7F32) // Fond bronze pour le troisième
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp), // Augmentation de la hauteur
        shape = RoundedCornerShape(12.dp), // Coins plus arrondis
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number with special background for top 3
            if (user.position <= 3) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(rankBackgroundColor, CircleShape)
                ) {
                    Text(
                        text = user.position.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Regular position number
                Text(
                    text = user.position.toString(),
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
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${user.xp} XP",
                    fontSize = 16.sp,
                    color = Color(0xFF9EABBE) // Couleur bleu clair/gris
                )
            }

            // Avatar - plus grand
            Image(
                painter = painterResource(id = user.avatarRes),
                contentDescription = "Avatar de ${user.name}",
                modifier = Modifier
                    .size(50.dp) // Taille augmentée
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}