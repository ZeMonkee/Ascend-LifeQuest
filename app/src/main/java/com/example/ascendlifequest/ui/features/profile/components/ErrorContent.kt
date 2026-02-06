package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    val colors = themeColors()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "Erreur", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "Oups ! Une erreur est survenue",
                    color = colors.mainText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = message,
                    color = colors.minusText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.lightAccent)
            ) { Text("Réessayer") }
        }
    }
}
