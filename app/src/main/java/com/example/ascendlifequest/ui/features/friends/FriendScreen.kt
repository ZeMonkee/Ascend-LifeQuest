package com.example.ascendlifequest.ui.features.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.friends.components.AddFriendDialog
import com.example.ascendlifequest.ui.features.friends.components.DeleteFriendConfirmDialog
import com.example.ascendlifequest.ui.features.friends.components.FriendsContent
import com.example.ascendlifequest.ui.features.friends.components.PendingRequestsDialog
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun FriendScreen(navController: NavHostController, viewModel: FriendsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddFriendDialog by viewModel.showAddFriendDialog.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val showPendingRequestsDialog by viewModel.showPendingRequestsDialog.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val addingFriendId by viewModel.addingFriendId.collectAsState()
    val requestSentMessage by viewModel.requestSentMessage.collectAsState()
    val pendingRequestsCount by viewModel.pendingRequestsCount.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Compteur pour forcer le rechargement à chaque fois qu'on revient sur l'écran
    var refreshKey by remember { mutableIntStateOf(0) }

    // Recharger les données à chaque affichage de l'écran
    LaunchedEffect(refreshKey) { viewModel.loadFriendsAndRequests() }

    // Incrémenter le compteur quand l'écran devient visible
    DisposableEffect(Unit) {
        refreshKey++
        onDispose {}
    }

    // Dialogue d'ajout d'ami
    if (showAddFriendDialog) {
        AddFriendDialog(
                searchQuery = searchQuery,
                searchState = searchState,
                addingFriendId = addingFriendId,
                requestSentMessage = requestSentMessage,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSendFriendRequest = { viewModel.sendFriendRequest(it) },
                onDismiss = { viewModel.closeAddFriendDialog() }
        )
    }

    // Dialogue des demandes d'amis en attente et notifications
    if (showPendingRequestsDialog) {
        val state = uiState
        if (state is FriendsUiState.Success) {
            PendingRequestsDialog(
                    pendingRequests = state.pendingRequests,
                    notifications = state.notifications,
                    onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                    onDeclineRequest = { viewModel.declineFriendRequest(it) },
                    onDeleteNotification = { viewModel.deleteNotification(it) },
                    onDismiss = { viewModel.closePendingRequestsDialog() }
            )
        }
    }

    // Dialogue de confirmation de suppression
    showDeleteConfirmDialog?.let { friend ->
        DeleteFriendConfirmDialog(
                friend = friend,
                onConfirm = { viewModel.removeFriend(friend) },
                onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }

    AppBottomNavBar(navController, BottomNavItem.Amis) { innerPadding ->
        AppBackground {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Indicateur de refresh en haut
                    if (isRefreshing && uiState is FriendsUiState.Success) {
                        LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = AppColor.LightBlueColor,
                                trackColor = AppColor.DarkBlueColor
                        )
                    }

                    // Header avec titre centré et bouton des demandes à droite
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Titre centré
                        Text(
                                text = "AMIS",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColor.MainTextColor,
                                modifier = Modifier.align(Alignment.Center)
                        )

                        // Bouton des demandes d'amis avec badge à droite
                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            IconButton(onClick = { viewModel.openPendingRequestsDialog() }) {
                                Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Demandes d'amis",
                                        tint =
                                                if (pendingRequestsCount > 0) AppColor.CuisineColor
                                                else AppColor.MinusTextColor,
                                        modifier = Modifier.size(28.dp)
                                )
                            }

                            // Badge avec le nombre de demandes
                            if (pendingRequestsCount > 0) {
                                Box(
                                        modifier =
                                                Modifier.align(Alignment.TopEnd)
                                                        .offset(x = (-4).dp, y = 4.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(AppColor.SportColor),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                            text =
                                                    if (pendingRequestsCount > 9) "9+"
                                                    else pendingRequestsCount.toString(),
                                            color = AppColor.MainTextColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    when (val state = uiState) {
                        is FriendsUiState.Loading -> {
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = AppColor.LightBlueColor) }
                        }
                        is FriendsUiState.Success -> {
                            FriendsContent(
                                    friends = state.friends,
                                    navController = navController,
                                    onDeleteFriend = { viewModel.showDeleteConfirmation(it) }
                            )
                        }
                        is FriendsUiState.Error -> {
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = state.message,
                                        color = AppColor.MinusTextColor,
                                        textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Bouton flottant pour ajouter un ami
                FloatingActionButton(
                        onClick = { viewModel.openAddFriendDialog() },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        containerColor = AppColor.LightBlueColor,
                        contentColor = AppColor.MainTextColor,
                        shape = CircleShape
                ) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter un ami",
                            modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
