package com.example.ascendlifequest.ui.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.data.model.Message
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun ChatMessagesList(messages: List<Message>, currentUserId: String) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barre de séparation turquoise en haut
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(AppColor.DarkBlueColor))

        if (messages.isEmpty()) {
            Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
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
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(AppColor.DarkBlueColor))
    }
}
