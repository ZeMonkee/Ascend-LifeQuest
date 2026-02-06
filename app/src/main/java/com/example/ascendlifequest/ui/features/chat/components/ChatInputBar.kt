package com.example.ascendlifequest.ui.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun ChatInputBar(
        messageText: String,
        onMessageChange: (String) -> Unit,
        onSendClick: () -> Unit,
        isSending: Boolean
) {
    val colors = themeColors()

    Surface(color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Écrire un message...", color = colors.minusText)
                    },
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.mainText,
                                    unfocusedTextColor = colors.mainText,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = colors.lightAccent,
                                    unfocusedBorderColor =
                                            colors.minusText.copy(alpha = 0.5f),
                                    cursorColor = colors.lightAccent
                            ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                    onClick = onSendClick,
                    enabled = messageText.isNotBlank() && !isSending,
                    modifier =
                            Modifier.size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                            if (messageText.isNotBlank()) colors.lightAccent
                                            else colors.minusText.copy(alpha = 0.3f)
                                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.mainText,
                            strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Envoyer",
                            tint = colors.mainText
                    )
                }
            }
        }
    }
}
