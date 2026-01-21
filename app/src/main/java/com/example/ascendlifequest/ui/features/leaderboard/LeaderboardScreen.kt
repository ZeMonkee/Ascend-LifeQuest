package com.example.ascendlifequest.ui.features.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.leaderboard.components.RankingItem
import com.example.ascendlifequest.ui.features.profile.components.ErrorContent
import com.example.ascendlifequest.ui.features.profile.components.LoadingContent
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun ClassementScreen(
        navController: NavHostController,
        viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AppBottomNavBar(navController, BottomNavItem.Classement) { innerPadding ->
        AppBackground {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AppHeader(
                        title = "CLASSEMENTS",
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Toggle
                val filterMode by viewModel.filterMode.collectAsState()

                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(45.dp)
                                        .background(
                                                color = AppColor.DarkBlueColor,
                                                shape = RoundedCornerShape(25.dp)
                                        )
                                        .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Global Tab
                    Box(
                            modifier =
                                    Modifier.weight(1f)
                                            .fillMaxHeight()
                                            .background(
                                                    color =
                                                            if (filterMode == LeaderboardFilter.ALL)
                                                                    AppColor.LightBlueColor
                                                            else Color.Transparent,
                                                    shape = RoundedCornerShape(25.dp)
                                            )
                                            .clickable {
                                                viewModel.setFilterMode(LeaderboardFilter.ALL)
                                            },
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = "Tout le monde",
                                color =
                                        if (filterMode == LeaderboardFilter.ALL)
                                                AppColor.MainTextColor
                                        else AppColor.MinusTextColor,
                                fontWeight =
                                        if (filterMode == LeaderboardFilter.ALL) FontWeight.Bold
                                        else FontWeight.Normal,
                                fontSize = 14.sp
                        )
                    }

                    // Friends Tab
                    Box(
                            modifier =
                                    Modifier.weight(1f)
                                            .fillMaxHeight()
                                            .background(
                                                    color =
                                                            if (filterMode ==
                                                                            LeaderboardFilter
                                                                                    .FRIENDS
                                                            )
                                                                    AppColor.LightBlueColor
                                                            else Color.Transparent,
                                                    shape = RoundedCornerShape(25.dp)
                                            )
                                            .clickable {
                                                viewModel.setFilterMode(LeaderboardFilter.FRIENDS)
                                            },
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = "Mes amis",
                                color =
                                        if (filterMode == LeaderboardFilter.FRIENDS)
                                                AppColor.MainTextColor
                                        else AppColor.MinusTextColor,
                                fontWeight =
                                        if (filterMode == LeaderboardFilter.FRIENDS) FontWeight.Bold
                                        else FontWeight.Normal,
                                fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (val state = uiState) {
                    is LeaderboardUiState.Loading -> {
                        LoadingContent()
                    }
                    is LeaderboardUiState.Success -> {
                        // Rankings List
                        LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.leaderboard) { user ->
                                RankingItem(
                                        user = user,
                                        onClick = { navController.navigate("profil/${user.uid}") }
                                )
                            }
                        }
                    }
                    is LeaderboardUiState.Error -> {
                        ErrorContent(
                                message = state.message,
                                onRetry = { viewModel.loadLeaderboard() }
                        )
                    }
                }
            }
        }
    }
}
