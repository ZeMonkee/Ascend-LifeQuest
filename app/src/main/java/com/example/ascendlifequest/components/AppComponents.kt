package com.example.ascendlifequest.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.screen.QuestItem

// Background
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Image(
                painter = painterResource(R.drawable.background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        }

        content()
    }
}

// Header
@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    progress: Float? = null,
    backgroundColor: Color = Color(0xFF1E293B),
    progressColor: Color = Color(0xFF3B82F6)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        subtitle?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.White, fontSize = 14.sp)
        }

        progress?.let {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = Color.Gray,
            )
        }
    }
}

// BottomNavBar
enum class BottomNavItem(val label: String, val icon: Int) {
    Quetes("Quêtes", R.drawable.icon_quetes),
    Classement("Classement", R.drawable.icon_classement),
    Amis("Amis", R.drawable.icon_amis),
    Parametres("Paramètres", R.drawable.icon_parametres)
}

@Composable
fun AppBottomNavBar(
    current: BottomNavItem = BottomNavItem.Quetes,
    onItemSelected: (BottomNavItem) -> Unit = {}
) {
    NavigationBar(containerColor = Color(0xFF1E293B)) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = current == item,
                onClick = { onItemSelected(item) },
                icon = { Icon(painter = painterResource(item.icon), contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

// Catégorie des quêtes
@Composable
fun QuestCategory(title: String, color: Color, quests: List<QuestItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E293B))
            .padding(12.dp)
    ) {
        // Titre catégorie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(8.dp)
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Liste des quêtes
        quests.forEach {
            val questColor = if (it.done) Color.Gray else Color.White
            Text(
                text = "› ${it.title}  ${it.xp} XP",
                color = questColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
