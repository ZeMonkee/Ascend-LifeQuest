package com.example.ascendlifequest.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.AppColor

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