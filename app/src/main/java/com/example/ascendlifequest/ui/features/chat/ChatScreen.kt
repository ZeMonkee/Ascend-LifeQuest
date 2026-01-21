package com.example.ascendlifequest.ui.features.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.chat.components.ChatInputBar
import com.example.ascendlifequest.ui.features.chat.components.ChatMessagesList
import com.example.ascendlifequest.ui.features.chat.components.ChatTopBar
import com.example.ascendlifequest.ui.theme.AppColor

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

    LaunchedEffect(friendId) { viewModel.loadConversation(friendId) }

    AppBottomNavBar(navController, BottomNavItem.Amis) { innerPadding ->
        AppBackground {
            Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(innerPadding),
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
                                isSending = isSending
                        )
                    }
            ) { scaffoldPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
                    when (val state = uiState) {
                        is ChatUiState.Loading -> {
                            CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = AppColor.LightBlueColor
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
                                    color = AppColor.MinusTextColor,
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
