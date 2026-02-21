package com.example.ascendlifequest.ui.features.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.features.chat.ChatUiState
import com.example.ascendlifequest.ui.theme.themeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(uiState: ChatUiState, onBackClick: () -> Unit) {
    val colors = themeColors()
    val title =
            when (uiState) {
                is ChatUiState.Success -> uiState.otherUser?.pseudo?.uppercase() ?: "CHAT"
                else -> "CHAT"
            }

    Column {
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            // Bouton retour à gauche
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = colors.mainText
                )
            }

            // Titre centré
            Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.mainText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
