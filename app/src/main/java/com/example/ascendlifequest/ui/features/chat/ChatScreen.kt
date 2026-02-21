package com.example.ascendlifequest.ui.features.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.data.network.NetworkConnectivityManager
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.chat.components.ChatInputBar
import com.example.ascendlifequest.ui.features.chat.components.ChatMessagesList
import com.example.ascendlifequest.ui.features.chat.components.ChatTopBar
import com.example.ascendlifequest.ui.features.offline.OfflineIndicatorBanner
import com.example.ascendlifequest.ui.theme.themeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
        navController: NavHostController,
        friendId: String,
        viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val colors = themeColors()

    // Détecter le mode hors ligne
    val context = LocalContext.current
    val networkManager = remember { NetworkConnectivityManager.getInstance(context) }
    val isOnline by networkManager.isConnected.collectAsState()

    LaunchedEffect(friendId) { viewModel.loadConversation(friendId) }

    AppBottomNavBar(navController, BottomNavItem.Amis) { innerPadding ->
        AppBackground {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Indicateur de mode hors ligne
                if (!isOnline) {
                    OfflineIndicatorBanner()
                }

                Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            ChatTopBar(
                                    uiState = uiState,
                                    onBackClick = { navController.popBackStack() }
                            )
                        },
                        bottomBar = {
                            ChatInputBar(
                                    messageText = messageText,
                                    onMessageChange = { viewModel.updateMessageText(it) },
                                    onSendClick = { viewModel.sendMessage() },
                                    isSending = isSending,
                                    enabled = isOnline // Désactiver l'envoi en mode hors ligne
                            )
                        }
                ) { scaffoldPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
                        when (val state = uiState) {
                            is ChatUiState.Loading -> {
                                CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = colors.lightAccent
                                )
                            }
                            is ChatUiState.Success -> {
                                ChatMessagesList(
                                        messages = state.messages,
                                        currentUserId = currentUserId
                                )
                            }
                            is ChatUiState.Error -> {
                                Text(
                                        text = state.message,
                                        color = colors.minusText,
                                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
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
