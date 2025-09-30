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
import com.example.ascendlifequest.model.User
import java.util.Date

@Composable
fun ClassementScreen(navController: NavHostController) {
    val rankingUsers = listOf(
        User(accountId = 1, pseudo = "Monkee", photoUrl = R.drawable.generic_pfp, xp = 99999, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 1),
        User(accountId = 2, pseudo = "ArcLeRetour", photoUrl = R.drawable.generic_pfp, xp = 99996, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 2),
        User(accountId = 3, pseudo = "Acno", photoUrl = R.drawable.generic_pfp, xp = 84826, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 3),
        User(accountId = 4, pseudo = "Kohlt", photoUrl = R.drawable.generic_pfp, xp = 75815, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 4),
        User(accountId = 5, pseudo = "Luca", photoUrl = R.drawable.generic_pfp, xp = 60150, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 5),
        User(accountId = 6, pseudo = "FruitySkyLine", photoUrl = R.drawable.generic_pfp, xp = 60084, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 6),
        User(accountId = 7, pseudo = "NoFastOne", photoUrl = R.drawable.generic_pfp, xp = 57515, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 7),
        User(accountId = 8, pseudo = "CookySensei", photoUrl = R.drawable.generic_pfp, xp = 54641, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 8),
        User(accountId = 9, pseudo = "LesLuong", photoUrl = R.drawable.generic_pfp, xp = 52674, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 9),
        User(accountId = 10, pseudo = "Lippido", photoUrl = R.drawable.generic_pfp, xp = 51234, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 10),
        User(accountId = 11, pseudo = "AnthonyBiv", photoUrl = R.drawable.generic_pfp, xp = -50516, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 11),
        User(accountId = 12, pseudo = "AnthonyBiv²", photoUrl = R.drawable.generic_pfp, xp = -4999999, online = false, quetesRealisees = 0, streak = 0, dateDeCreation = Date(), rang = 12)
    )
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Classement) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quest")
                    BottomNavItem.Classement -> {} // Déjà sur cet écran
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Profil -> navController.navigate("profil")
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

@Composable
fun RankingItem(user: User) {
    val backgroundColor = Color(0xFF2E3F78)

    val rankingColor = when (user.rang) {
        1 -> Color(0xFFFFD700) // Or
        2 -> Color(0xFFC0C0C0) // Argent
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White
    }

    val rankBackgroundColor = when (user.rang) {
        1 -> Color(0xFFF39C12) // Fond orange/or pour le premier
        2 -> Color(0xFF95A5A6) // Fond gris argenté pour le second
        3 -> Color(0xFFCD7F32) // Fond bronze pour le troisième
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
                        color = Color.White
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
                    color = Color.White
                )
                Text(
                    text = "${user.xp} XP",
                    fontSize = 16.sp,
                    color = Color(0xFF9EABBE)
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