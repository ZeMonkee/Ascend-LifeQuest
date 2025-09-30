package com.example.ascendlifequest.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ProfilScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            AppBottomNavBar(current = BottomNavItem.Profil) { selected ->
                when (selected) {
                    BottomNavItem.Quetes -> navController.navigate("quest")
                    BottomNavItem.Classement -> navController.navigate("classement")
                    BottomNavItem.Amis -> navController.navigate("amis")
                    BottomNavItem.Profil -> {} // Déjà sur cet écran
                    BottomNavItem.Parametres -> {}
                }
            }
        }
    ) { paddingValues ->
        AppBackground {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
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
                                .background(Color.Gray, shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nom utilisateur
                        Text(
                            text = "Kohit",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // ID utilisateur
                        Text(
                            text = "ID #35383773",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Date de membre
                        Text(
                            text = "Membre depuis le 15/09/2025",
                            fontSize = 13.sp,
                            color = Color.Gray
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

                        Spacer(modifier = Modifier.height(12.dp))

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

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}
