package com.example.ascendlifequest.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.profile.components.ErrorContent
import com.example.ascendlifequest.ui.features.profile.components.LoadingContent
import com.example.ascendlifequest.ui.features.profile.components.NotLoggedInContent
import com.example.ascendlifequest.ui.features.profile.components.ProfileContent
import com.example.ascendlifequest.ui.theme.themeColors

/**
 * Profile screen displaying user information, stats, and level progress.
 *
 * @param navController Navigation controller for screen transitions
 * @param userId Optional user ID for viewing other users' profiles
 * @param viewModel ViewModel managing profile state
 */
@Composable
fun ProfilScreen(
        navController: NavHostController,
        userId: String? = null,
        viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOtherUser = userId != null

    LaunchedEffect(userId) { viewModel.loadProfile(userId) }

    // Choisir le bon item de navigation en fonction du contexte
    val navItem = if (isOtherUser) BottomNavItem.Classement else BottomNavItem.Profil

    AppBottomNavBar(navController, navItem) { innerPadding ->
        AppBackground {
            val colors = themeColors()

            Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
            ) {
                // Header avec bouton retour si on visualise un autre profil
                if (isOtherUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = colors.mainText
                            )
                        }

                        Text(
                            text = "PROFIL",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.mainText,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(48.dp))
                    }
                } else {
                    AppHeader(title = "PROFIL")
                }

                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        LoadingContent()
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(
                            profile = state.profile,
                            rank = state.rank,
                            isOtherUser = isOtherUser,
                            onSendMessage = if (isOtherUser) {
                                {
                                    val targetUserId = state.profile.uid.ifEmpty { state.profile.id }
                                    navController.navigate("chat/$targetUserId")
                                }
                            } else null,
                            onAddFriend = null // Géré dans ProfileContent directement
                        )
                    }
                    is ProfileUiState.Error -> {
                        ErrorContent(
                                message = state.message,
                                onRetry = { viewModel.loadProfile(userId) }
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
