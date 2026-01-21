package com.example.ascendlifequest.ui.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun AccountAvatar(resId: Int, size: Dp = 96.dp) {
    Image(
            painter = painterResource(id = resId),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier =
                    Modifier.size(size)
                            .clip(CircleShape)
                            .background(AppColor.MinusTextColor, CircleShape)
                            .border(
                                    width = 2.dp,
                                    color = AppColor.LightBlueColor,
                                    shape = CircleShape
                            )
    )
}
