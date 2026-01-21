package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun SuccessNotice(message: String, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = AppColor.LectureColor.copy(alpha = 0.12f)
                    ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = message, color = AppColor.LectureColor, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColor.LectureColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = "Redirection dans quelques secondes...",
                    color = AppColor.LectureColor,
                    fontSize = 12.sp
            )
        }
    }
}
