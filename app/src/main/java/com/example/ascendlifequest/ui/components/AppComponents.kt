package com.example.ascendlifequest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.data.network.LocalOfflineMode
import com.example.ascendlifequest.ui.theme.themeColors

// Background avec overlay du thème (comme Flutter)
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val colors = themeColors()

    Box(modifier = Modifier.fillMaxSize()) {
        // Image de fond
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay avec le gradient du thème pour teinter l'image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.gradientStart.copy(alpha = 0.7f),
                            colors.gradientEnd.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Contenu
        content()
    }
}

/**
 * Badge "Mode offline" affiché en haut à gauche quand pas de connexion
 */
@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val colors = themeColors()

    Row(
        modifier = modifier
            .background(
                color = colors.cuisine.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Hors ligne",
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Mode offline",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Header avec support du mode offline
@Composable
fun AppHeader(
    title: String,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = themeColors()
    val offlineMode = LocalOfflineMode.current

    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Badge offline en haut à gauche
        if (offlineMode.isOffline) {
            OfflineBadge(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
            )
        }

        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = colors.mainText,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        // Slot trailing aligné à droite sur la même ligne
        if (trailing != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)) {
                trailing()
            }
        }
    }
}

// BottomNavBar
@Composable
fun AppBottomNavBar(
    navController: NavHostController,
    current: BottomNavItem,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            InitNavItem(current = current) { selected ->
                if (selected != current) {
                    when (selected) {
                        BottomNavItem.Quetes -> navController.navigate("quetes")
                        BottomNavItem.Classement -> navController.navigate("classement")
                        BottomNavItem.Amis -> navController.navigate("amis")
                        BottomNavItem.Parametres -> navController.navigate("parametres")
                        BottomNavItem.Profil -> navController.navigate("profil")
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding) // le contenu
    }
}

enum class BottomNavItem(val label: String, val icon: Int) {
    Quetes("Quêtes", R.drawable.icon_quetes),
    Classement("Classement", R.drawable.icon_classement),
    Amis("Amis", R.drawable.icon_amis),
    Profil("Profil", R.drawable.icon_profil),
    Parametres("Paramètres", R.drawable.icon_parametres)
}

@Composable
fun InitNavItem(
    current: BottomNavItem = BottomNavItem.Quetes,
    onItemSelected: (BottomNavItem) -> Unit = {}
) {
    val colors = themeColors()
    NavigationBar(
        containerColor = colors.darkBackground,
        contentColor = colors.mainText
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = current == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        tint = if (current == item) colors.lightAccent else colors.mainText
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (current == item) colors.lightAccent else colors.mainText,
                        fontSize = 11.sp,
                    )
                }
            )
        }
    }
}