package com.example.ascendlifequest.ui.features.offline

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.theme.themeColors

/**
 * Composant qui affiche le bouton "Continuer hors ligne" quand l'utilisateur est déconnecté
 * mais a des données locales.
 */
@Composable
fun OfflineModeButton(
    pseudo: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = themeColors()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Indicateur hors ligne
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Hors ligne",
                tint = colors.cuisine,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mode hors ligne disponible",
                color = colors.minusText,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bouton continuer hors ligne
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.mainText
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(colors.lightAccent)
            )
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (pseudo != null) "Continuer en tant que $pseudo" else "Continuer hors ligne",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (pseudo != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fonctionnalités limitées",
                color = colors.minusText.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Écran affiché quand l'utilisateur est hors ligne sans données locales
 */
@Composable
fun NoConnectionScreen(
    onRetry: () -> Unit
) {
    val colors = themeColors()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_circle),
                contentDescription = "Ascend Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Icône hors ligne
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Hors ligne",
                tint = colors.cuisine,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pas de connexion",
                color = colors.mainText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Connectez-vous à Internet pour accéder à l'application.\n\nSi vous vous êtes déjà connecté, vos données seront disponibles hors ligne.",
                color = colors.minusText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton réessayer
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.lightAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Réessayer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Indicateur de mode hors ligne pour les écrans de l'application
 */
@Composable
fun OfflineIndicatorBanner(
    modifier: Modifier = Modifier
) {
    val colors = themeColors()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.cuisine.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Hors ligne",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mode hors ligne - Certaines fonctionnalités sont limitées",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}
