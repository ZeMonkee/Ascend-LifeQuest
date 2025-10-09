package com.example.ascendlifequest.components.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun SocialLoginButton(iconRes: Int, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(AppColor.MainTextColor, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "social login",
            modifier = Modifier.size(24.dp)
        )
    }
}