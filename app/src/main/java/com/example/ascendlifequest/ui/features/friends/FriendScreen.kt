package com.example.ascendlifequest.ui.features.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.friends.components.FriendItem
import com.example.ascendlifequest.ui.features.friends.components.FriendRequestItem
import com.example.ascendlifequest.ui.features.friends.components.SearchUserItem
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun FriendScreen(
    navController: NavHostController,
    viewModel: FriendsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddFriendDialog by viewModel.showAddFriendDialog.collectAsState()
    val showDeleteConfirmDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val showPendingRequestsDialog by viewModel.showPendingRequestsDialog.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val isAddingFriend by viewModel.isAddingFriend.collectAsState()
    val requestSentMessage by viewModel.requestSentMessage.collectAsState()
    val pendingRequestsCount by viewModel.pendingRequestsCount.collectAsState()

    // Recharger les données à chaque affichage de l'écran
    LaunchedEffect(Unit) {
        viewModel.loadFriendsAndRequests()
    }

    // Dialogue d'ajout d'ami
    if (showAddFriendDialog) {
        AddFriendDialog(
            searchQuery = searchQuery,
            searchState = searchState,
            isAddingFriend = isAddingFriend,
            requestSentMessage = requestSentMessage,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onSendFriendRequest = { viewModel.sendFriendRequest(it) },
            onDismiss = { viewModel.closeAddFriendDialog() }
        )
    }

    // Dialogue des demandes d'amis en attente
    if (showPendingRequestsDialog) {
        val state = uiState
        if (state is FriendsUiState.Success) {
            PendingRequestsDialog(
                pendingRequests = state.pendingRequests,
                onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                onDeclineRequest = { viewModel.declineFriendRequest(it) },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header avec bouton des demandes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMIS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.MainTextColor
                        )

                        // Bouton des demandes d'amis avec badge
                        Box {
                            IconButton(
                                onClick = { viewModel.openPendingRequestsDialog() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Demandes d'amis",
                                    tint = if (pendingRequestsCount > 0) AppColor.CuisineColor else AppColor.MinusTextColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Badge avec le nombre de demandes
                            if (pendingRequestsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(AppColor.SportColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (pendingRequestsCount > 9) "9+" else pendingRequestsCount.toString(),
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
                            ) {
                                CircularProgressIndicator(color = AppColor.LightBlueColor)
                            }
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
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
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

@Composable
private fun FriendsContent(
    friends: List<UserProfile>,
    navController: NavHostController,
    onDeleteFriend: (UserProfile) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyFriendsContent()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendItem(
                    user = friend,
                    onMessageClick = {
                        navController.navigate("chat/${friend.uid}")
                    },
                    onLongPress = {
                        onDeleteFriend(friend)
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyFriendsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "👥",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucun ami pour le moment",
                color = AppColor.MainTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Appuyez sur + pour ajouter des amis",
                color = AppColor.MinusTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddFriendDialog(
    searchQuery: String,
    searchState: SearchUiState,
    isAddingFriend: Boolean,
    requestSentMessage: String?,
    onSearchQueryChange: (String) -> Unit,
    onSendFriendRequest: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ajouter un ami",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = AppColor.MinusTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Champ de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Rechercher par pseudo...",
                            color = AppColor.MinusTextColor
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AppColor.MinusTextColor
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColor.MainTextColor,
                        unfocusedTextColor = AppColor.MainTextColor,
                        focusedBorderColor = AppColor.LightBlueColor,
                        unfocusedBorderColor = AppColor.MinusTextColor.copy(alpha = 0.5f),
                        cursorColor = AppColor.LightBlueColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Message de confirmation
                requestSentMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = if (message.startsWith("Demande")) AppColor.LectureColor else AppColor.SportColor,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Résultats de recherche
                Box(modifier = Modifier.weight(1f)) {
                    when (searchState) {
                        is SearchUiState.Idle -> {
                            if (searchQuery.isBlank()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Entrez un pseudo pour rechercher",
                                        color = AppColor.MinusTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is SearchUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColor.LightBlueColor)
                            }
                        }
                        is SearchUiState.Success -> {
                            if (searchState.users.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aucun utilisateur trouvé",
                                        color = AppColor.MinusTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(searchState.users) { user ->
                                        SearchUserItem(
                                            user = user,
                                            onAddClick = { onSendFriendRequest(user) },
                                            isAdding = isAddingFriend
                                        )
                                    }
                                }
                            }
                        }
                        is SearchUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = searchState.message,
                                    color = AppColor.MinusTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestsDialog(
    pendingRequests: List<UserProfile>,
    onAcceptRequest: (UserProfile) -> Unit,
    onDeclineRequest: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.6f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Demandes d'amis (${pendingRequests.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = AppColor.MinusTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Liste des demandes
                if (pendingRequests.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📭",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aucune demande en attente",
                                color = AppColor.MinusTextColor,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingRequests) { request ->
                            FriendRequestItem(
                                user = request,
                                onAccept = { onAcceptRequest(request) },
                                onDecline = { onDeclineRequest(request) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteFriendConfirmDialog(
    friend: UserProfile,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Supprimer l'ami",
                color = AppColor.MainTextColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Voulez-vous vraiment supprimer ${friend.pseudo} de votre liste d'amis ?",
                color = AppColor.MinusTextColor
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColor.SportColor
                )
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Annuler",
                    color = AppColor.MinusTextColor
                )
            }
        },
        containerColor = AppColor.DarkBlueColor
    )
}

