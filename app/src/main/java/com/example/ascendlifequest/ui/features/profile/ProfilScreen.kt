package com.example.ascendlifequest.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ProfilScreen(
        navController: NavHostController,
        userId: String? = null,
        viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) { viewModel.loadProfile(userId) }

    AppBottomNavBar(navController, BottomNavItem.Profil) { innerPadding ->
        AppBackground {
            Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
            ) {
                AppHeader(title = "PROFIL")

                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        LoadingContent()
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(profile = state.profile, rank = state.rank)
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
