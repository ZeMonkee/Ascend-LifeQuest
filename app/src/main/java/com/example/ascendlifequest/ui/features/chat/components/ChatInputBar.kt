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
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun ChatInputBar(
        messageText: String,
        onMessageChange: (String) -> Unit,
        onSendClick: () -> Unit,
        isSending: Boolean
) {
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
                        Text(text = "Écrire un message...", color = AppColor.MinusTextColor)
                    },
                    colors =
                            OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = AppColor.MainTextColor,
                                    unfocusedTextColor = AppColor.MainTextColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = AppColor.LightBlueColor,
                                    unfocusedBorderColor =
                                            AppColor.MinusTextColor.copy(alpha = 0.5f),
                                    cursorColor = AppColor.LightBlueColor
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
