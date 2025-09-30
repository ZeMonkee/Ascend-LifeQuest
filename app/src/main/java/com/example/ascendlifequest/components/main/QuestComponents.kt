package com.example.ascendlifequest.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.screen.main.QuestItem
import com.example.ascendlifequest.ui.theme.AppColor

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