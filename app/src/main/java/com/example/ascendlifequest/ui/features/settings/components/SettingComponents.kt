package com.example.ascendlifequest.ui.features.settings.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.themeColors

/**
 * Item des settings style Flutter
 * @param title Titre de l'option
 * @param icon Icône vectorielle Material
 * @param isDestructive Si true, utilise une couleur rouge pour indiquer une action destructive
 * @param onClick Callback au clic
 */
@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val colors = themeColors()
    val itemColor = if (isDestructive) colors.sport else colors.mainText

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.darkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône à gauche
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = itemColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Titre
            Text(
                text = title,
                color = itemColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Chevron à droite
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isDestructive) itemColor else colors.minusText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Item des settings avec ressource drawable (rétrocompatibilité)
 */
@Composable
fun SettingsItem(
    title: String,
    iconRes: Int,
    isDestructive: Boolean = false,
    onClick: (context: Context) -> Unit
) {
    val context = LocalContext.current

    // Mapper vers les icônes Material appropriées
    val icon = when (title) {
        "Comptes" -> Icons.Filled.Person
        "Thèmes" -> Icons.Filled.Palette
        "Préférences" -> Icons.Filled.Tune
        "Se déconnecter" -> Icons.AutoMirrored.Filled.Logout
        "Notifications" -> Icons.Filled.Notifications
        else -> Icons.Filled.Tune
    }

    SettingsItem(
        title = title,
        icon = icon,
        isDestructive = isDestructive,
        onClick = { onClick(context) }
    )
}
