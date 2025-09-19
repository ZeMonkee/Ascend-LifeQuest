package com.example.ascendlifequest.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.screen.QuestItem
import com.example.ascendlifequest.ui.theme.AppColor

// Background
@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Image(
            painter = painterResource(R.drawable.background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

    content()
}

// Header
@Composable
fun AppHeader(
    title: String,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor.MainTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
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
    NavigationBar(
        containerColor = AppColor.DarkBlueColor,
        contentColor = AppColor.MainTextColor
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = current == item,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        tint = if (current == item) AppColor.LightBlueColor else AppColor.MainTextColor
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (current == item) AppColor.LightBlueColor else AppColor.MainTextColor
                    )
                }
            )
        }
    }
}

// Catégorie des quêtes
@Composable
fun QuestCategory(
    title: String,
    color: Color,
    iconRes: Int,
    quests: List<QuestItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(AppColor.DarkBlueColor, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
    ) {
        // Header avec icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color, shape = RoundedCornerShape(30.dp))
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Liste des quêtes
        quests.forEach { quest ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(AppColor.DarkBlueColor, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "» ${quest.title}",
                    color = if (quest.done) Color.Gray else AppColor.MainTextColor,
                    fontWeight = if (quest.done) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (quest.done) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${quest.xp} XP",
                    color = if (quest.done) Color.Gray else AppColor.MainTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
