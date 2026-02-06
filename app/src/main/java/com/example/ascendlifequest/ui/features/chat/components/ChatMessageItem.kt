package com.example.ascendlifequest.ui.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.data.model.Message
import com.example.ascendlifequest.ui.theme.themeColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessageItem(message: Message, isFromCurrentUser: Boolean) {
    val colors = themeColors()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
    val timeString = dateFormat.format(message.timestamp.toDate())

    Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Box(
                modifier =
                        Modifier.widthIn(max = 280.dp)
                                .background(
                                        color =
                                                if (isFromCurrentUser) colors.lightAccent
                                                else colors.darkBackground,
                                        shape =
                                                RoundedCornerShape(
                                                        topStart = 16.dp,
                                                        topEnd = 16.dp,
                                                        bottomStart =
                                                                if (isFromCurrentUser) 16.dp
                                                                else 4.dp,
                                                        bottomEnd =
                                                                if (isFromCurrentUser) 4.dp
                                                                else 16.dp
                                                )
                                )
                                .padding(12.dp)
        ) { Text(text = message.content, color = colors.mainText, fontSize = 15.sp) }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
                text = timeString,
                color = colors.minusText,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
