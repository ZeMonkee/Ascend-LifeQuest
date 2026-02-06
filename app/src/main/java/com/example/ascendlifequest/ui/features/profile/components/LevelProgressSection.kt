package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.themeColors
import java.util.Locale

@Composable
fun LevelProgressSection(level: Int, progress: Float, xpToNext: Long) {
    val colors = themeColors()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Badge de niveau
        Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.lightAccent,
                modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                    text = "Niveau $level",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = colors.mainText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
            )
        }

        // Barre de progression
        LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = colors.lightAccent,
                trackColor = colors.minusText.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // XP restante pour le prochain niveau
        Text(
                text = "${formatXp(xpToNext)} XP pour le niveau ${level + 1}",
                fontSize = 12.sp,
                color = colors.minusText
        )
    }
}

/** Formate un nombre d'XP pour l'affichage (ex: 1234567 -> 1.2M) */
fun formatXp(xp: Long): String {
    return when {
        xp >= 1_000_000 -> String.format(Locale.FRANCE, "%.1fM", xp / 1_000_000.0)
        xp >= 1_000 -> String.format(Locale.FRANCE, "%.1fK", xp / 1_000.0)
        else -> xp.toString()
    }
}
