package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileContent(profile: UserProfile, rank: Int) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    val memberSince = dateFormat.format(profile.dateDeCreation.toDate())

    // Calcul du niveau et de la progression
    val level = profile.calculateLevel()
    val levelProgress = profile.calculateLevelProgress()
    val xpToNext = profile.xpToNextLevel()

    // Formatage du rang
    val rankText =
            when (rank) {
                1 -> "1er"
                2 -> "2ème"
                3 -> "3ème"
                else -> "${rank}ème"
            }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Carte principale
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        modifier =
                                Modifier.size(120.dp)
                                        .clip(CircleShape)
                                        .background(AppColor.MinusTextColor, shape = CircleShape),
                        contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nom utilisateur
                Text(
                        text = profile.pseudo.ifEmpty { "Utilisateur" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                )

                // ID utilisateur (utilise les 8 premiers caractères de l'ID Firebase)
                Text(
                        text = "ID #${profile.id.take(8).uppercase()}",
                        fontSize = 16.sp,
                        color = AppColor.MinusTextColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Date de membre
                Text(
                        text = "Membre depuis le $memberSince",
                        fontSize = 14.sp,
                        color = AppColor.MinusTextColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Niveau et barre de progression XP
                LevelProgressSection(level = level, progress = levelProgress, xpToNext = xpToNext)

                Spacer(modifier = Modifier.height(20.dp))

                // Statistiques
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                            value = "${formatXp(profile.xp)} XP",
                            label = "XP totale",
                            modifier = Modifier.weight(1f)
                    )
                    StatItem(
                            value = rankText,
                            label = "Rang actuel",
                            modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                            value = "${profile.quetesRealisees}",
                            label = "Quêtes réalisées",
                            modifier = Modifier.weight(1f)
                    )
                    StatItem(
                            value = "${profile.streak}",
                            label = "Jours d'affilés",
                            modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
