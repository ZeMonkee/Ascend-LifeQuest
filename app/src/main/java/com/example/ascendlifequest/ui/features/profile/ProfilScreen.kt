package com.example.ascendlifequest.ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.profile.components.StatItem
import com.example.ascendlifequest.ui.theme.AppColor
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfilScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

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

                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        LoadingContent()
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(
                            profile = state.profile,
                            rank = state.rank
                        )
                    }
                    is ProfileUiState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.loadProfile() }
                        )
                    }
                    is ProfileUiState.NotLoggedIn -> {
                        NotLoggedInContent(
                            onNavigateToLogin = { navController.navigate("login_option") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = AppColor.MainTextColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement du profil...",
                color = AppColor.MinusTextColor,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfile,
    rank: Int
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    val memberSince = dateFormat.format(profile.dateDeCreation.toDate())

    // Calcul du niveau et de la progression
    val level = profile.calculateLevel()
    val levelProgress = profile.calculateLevelProgress()
    val xpToNext = profile.xpToNextLevel()

    // Formatage du rang
    val rankText = when (rank) {
        1 -> "1er"
        2 -> "2ème"
        3 -> "3ème"
        else -> "${rank}ème"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                LevelProgressSection(
                    level = level,
                    progress = levelProgress,
                    xpToNext = xpToNext
                )

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

@Composable
private fun LevelProgressSection(
    level: Int,
    progress: Float,
    xpToNext: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge de niveau
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColor.LightBlueColor,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "Niveau $level",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = AppColor.MainTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Barre de progression
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AppColor.LightBlueColor,
            trackColor = AppColor.MinusTextColor.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // XP restante pour le prochain niveau
        Text(
            text = "${formatXp(xpToNext)} XP pour le niveau ${level + 1}",
            fontSize = 12.sp,
            color = AppColor.MinusTextColor
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "😕",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oups ! Une erreur est survenue",
                color = AppColor.MainTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = AppColor.MinusTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColor.LightBlueColor
                )
            ) {
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun NotLoggedInContent(
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🔒",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Vous n'êtes pas connecté",
                color = AppColor.MainTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Connectez-vous pour accéder à votre profil",
                color = AppColor.MinusTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColor.LightBlueColor
                )
            ) {
                Text("Se connecter")
            }
        }
    }
}

/**
 * Formate un nombre d'XP pour l'affichage (ex: 1234567 -> 1.2M)
 */
private fun formatXp(xp: Long): String {
    return when {
        xp >= 1_000_000 -> String.format(Locale.FRANCE, "%.1fM", xp / 1_000_000.0)
        xp >= 1_000 -> String.format(Locale.FRANCE, "%.1fK", xp / 1_000.0)
        else -> xp.toString()
    }
}
