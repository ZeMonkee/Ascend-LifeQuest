package com.example.ascendlifequest.ui.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.data.model.Message
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.theme.AppColor
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(friendId) {
        viewModel.loadConversation(friendId)
    }

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                ) {
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
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    uiState: ChatUiState,
    onBackClick: () -> Unit
) {
    val title = when (uiState) {
        is ChatUiState.Success -> uiState.otherUser?.pseudo?.uppercase() ?: "CHAT"
        else -> "CHAT"
    }

    Column {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Bouton retour à gauche
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = AppColor.MainTextColor
                )
            }

            // Titre centré
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor.MainTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ChatMessagesList(
    messages: List<Message>,
    currentUserId: String
) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barre de séparation turquoise en haut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(AppColor.DarkBlueColor)
        )

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucun message\nCommencez la conversation ! 💬",
                    color = AppColor.MinusTextColor,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        isFromCurrentUser = message.senderId == currentUserId
                    )
                }
            }
        }

        // Barre de séparation turquoise en bas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(AppColor.DarkBlueColor)
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: Message,
    isFromCurrentUser: Boolean
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
    val timeString = dateFormat.format(message.timestamp.toDate())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isFromCurrentUser) AppColor.LightBlueColor else AppColor.DarkBlueColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = AppColor.MainTextColor,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = timeString,
            color = AppColor.MinusTextColor,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Écrire un message...",
                        color = AppColor.MinusTextColor
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColor.MainTextColor,
                    unfocusedTextColor = AppColor.MainTextColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = AppColor.LightBlueColor,
                    unfocusedBorderColor = AppColor.MinusTextColor.copy(alpha = 0.5f),
                    cursorColor = AppColor.LightBlueColor
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (messageText.isNotBlank()) AppColor.LightBlueColor
                        else AppColor.MinusTextColor.copy(alpha = 0.3f)
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColor.MainTextColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Envoyer",
                        tint = AppColor.MainTextColor
                    )
                }
            }
        }
    }
}

