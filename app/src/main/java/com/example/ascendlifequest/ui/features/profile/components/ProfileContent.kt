package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.FriendRepositoryImpl
import com.example.ascendlifequest.ui.theme.themeColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileContent(
    profile: UserProfile,
    rank: Int,
    isOtherUser: Boolean = false,
    onSendMessage: (() -> Unit)? = null,
    onAddFriend: (() -> Unit)? = null
) {
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

    // État pour les boutons d'action
    val scope = rememberCoroutineScope()
    val friendRepository = remember { FriendRepositoryImpl() }
    val authRepository = remember { AuthRepositoryImpl(AuthService()) }

    var isFriend by remember { mutableStateOf(false) }
    var isRequestSent by remember { mutableStateOf(false) }
    var isAddingFriend by remember { mutableStateOf(false) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    // Vérifier le statut d'amitié si c'est un autre utilisateur
    LaunchedEffect(isOtherUser, profile.id) {
        if (isOtherUser) {
            val currentUserId = authRepository.getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                isFriend = friendRepository.areFriends(currentUserId, profile.uid.ifEmpty { profile.id })
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val colors = themeColors()

        // Carte principale
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.darkBackground),
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
                                        .background(colors.minusText, shape = CircleShape),
                        contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nom utilisateur
                Text(
                        text = profile.pseudo.ifEmpty { "Utilisateur" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.mainText
                )

                // ID utilisateur (utilise les 8 premiers caractères de l'ID Firebase)
                Text(
                        text = "ID #${profile.id.take(8).uppercase()}",
                        fontSize = 16.sp,
                        color = colors.minusText
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Date de membre
                Text(
                        text = "Membre depuis le $memberSince",
                        fontSize = 14.sp,
                        color = colors.minusText
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

                // Boutons d'action pour le profil d'un autre utilisateur
                if (isOtherUser) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Message de confirmation/erreur
                    actionMessage?.let { message ->
                        Text(
                            text = message,
                            color = if (message.contains("Erreur")) colors.sport else colors.lecture,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Boutons d'action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Bouton Ajouter en ami (si pas encore ami)
                        if (!isFriend) {
                            Button(
                                onClick = {
                                    if (!isRequestSent && !isAddingFriend) {
                                        isAddingFriend = true
                                        scope.launch {
                                            try {
                                                val currentUserId = authRepository.getCurrentUserId()
                                                val targetUserId = profile.uid.ifEmpty { profile.id }
                                                val result = friendRepository.sendFriendRequest(currentUserId, targetUserId)
                                                result.fold(
                                                    onSuccess = {
                                                        isRequestSent = true
                                                        actionMessage = "Demande d'ami envoyée !"
                                                    },
                                                    onFailure = { error ->
                                                        actionMessage = "Erreur: ${error.message}"
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                actionMessage = "Erreur: ${e.message}"
                                            } finally {
                                                isAddingFriend = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isRequestSent && !isAddingFriend,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.lightAccent,
                                    disabledContainerColor = colors.minusText.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isAddingFriend) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.mainText
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isRequestSent) Icons.Default.HourglassTop else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRequestSent) "Demande envoyée" else "Ajouter en ami",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Bouton Envoyer message (uniquement si ami)
                        if (isFriend && onSendMessage != null) {
                            Button(
                                onClick = onSendMessage,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.lightAccent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Message,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Envoyer un message",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
