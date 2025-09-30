package com.example.ascendlifequest.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.AppColor

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