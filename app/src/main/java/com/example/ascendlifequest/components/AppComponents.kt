package com.example.ascendlifequest.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.R
import com.example.ascendlifequest.model.User
import com.example.ascendlifequest.screen.main.QuestItem
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
    Profil("Profil", R.drawable.icon_profil),
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
                        color = if (current == item) AppColor.LightBlueColor else AppColor.MainTextColor,
                        fontSize = 11.sp,
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
            .background(
                AppColor.DarkBlueColor,
                shape = RoundedCornerShape(
                    topStart = 30.dp,
                    topEnd = 30.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                )
            )
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

@Composable
fun FriendItem(user: User) {
    val backgroundColor = AppColor.DarkBlueColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Image(
                painter = painterResource(id = user.photoUrl),
                contentDescription = "Avatar de ${user.pseudo}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Spacer
            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.pseudo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.MainTextColor
                )
                Text(
                    text = "${user.xp} XP",
                    fontSize = 16.sp,
                    color = AppColor.MinusTextColor
                )
            }

        }
    }
}


@Composable
fun RankingItem(user: User) {
    val backgroundColor = AppColor.DarkBlueColor

    val rankingColor = when (user.rang) {
        1 -> AppColor.Or // Or
        2 -> AppColor.Argent // Argent
        3 -> AppColor.Bronze // Bronze
        else -> AppColor.MainTextColor
    }

    val rankBackgroundColor = when (user.rang) {
        1 -> AppColor.Or // Or
        2 -> AppColor.Argent // Argent
        3 -> AppColor.Bronze // Bronze
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number with special background for top 3
            if (user.rang <= 3) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(rankBackgroundColor, CircleShape)
                ) {
                    Text(
                        text = user.rang.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                }
            } else {

                Text(
                    text = user.rang.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankingColor,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Spacer
            Spacer(modifier = Modifier.width(16.dp))

            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.pseudo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.MainTextColor
                )
                Text(
                    text = "${user.xp} XP",
                    fontSize = 16.sp,
                    color = AppColor.MinusTextColor
                )
            }

            // Avatar - plus grand
            Image(
                painter = painterResource(id = user.photoUrl ?: R.drawable.generic_pfp),
                contentDescription = "Avatar de ${user.pseudo}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// Item des settings
@Composable
fun SettingsItem(
    title: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    // Item avec icon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColor.DarkBlueColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = AppColor.MainTextColor,
            modifier = Modifier
                .size(30.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            color = AppColor.MainTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}


// Item des questions de preferences
@Composable
fun PreferenceQuestion(question: String, color: Color) {
    var selected by remember { androidx.compose.runtime.mutableIntStateOf(3) } // valeur par défaut au milieu

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question,
                fontSize = 24.sp,
                color = AppColor.MainTextColor,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            Box(contentAlignment = Alignment.CenterStart) {
                LinearProgressIndicator(
                    progress = {
                        (selected -1) / 4f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    color = color,
                    trackColor = Color.DarkGray,
                    strokeCap = Butt,
                    gapSize = 0.dp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { value ->
                        Button(
                            onClick = { selected = value },
                            modifier = Modifier
                                .size(42.dp),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(0.dp),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected >= value) color else Color.DarkGray
                            )
                        ) {
                            Text(
                                text = value.toString(),
                                color = AppColor.MainTextColor,
                                fontSize = 24.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}