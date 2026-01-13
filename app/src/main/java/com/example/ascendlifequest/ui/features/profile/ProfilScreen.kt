package com.example.ascendlifequest.ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.profile.components.StatItem
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun ProfilScreen(navController: NavHostController) {
    AppBottomNavBar(navController, BottomNavItem.Profil) { innerPadding ->
        AppBackground {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                AppHeader(title = "PROFIL")

                // Carte principale
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Photo de profil
                        Image(
                            painter = painterResource(id = R.drawable.generic_pfp),
                            contentDescription = "Photo de profil",
                            modifier = Modifier
                                .size(120.dp)
                                .background(AppColor.MinusTextColor, shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nom utilisateur
                        Text(
                            text = "Kohit",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.MainTextColor
                        )

                        // ID utilisateur
                        Text(
                            text = "ID #35383773",
                            fontSize = 16.sp,
                            color = AppColor.MinusTextColor
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Date de membre
                        Text(
                            text = "Membre depuis le 15/09/2025",
                            fontSize = 14.sp,
                            color = AppColor.MinusTextColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Statistiques
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("78412 XP", "XP gagnée", modifier = Modifier.weight(1f))
                            StatItem("4ème", "Rang actuel", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("2560", "Quêtes réalisées", modifier = Modifier.weight(1f))
                            StatItem("120", "Jours d'affilés", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
