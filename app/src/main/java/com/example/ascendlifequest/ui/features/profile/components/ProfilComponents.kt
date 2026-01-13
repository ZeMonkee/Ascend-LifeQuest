package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor.MainTextColor
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = AppColor.MinusTextColor,
            textAlign = TextAlign.Center
        )
    }
}