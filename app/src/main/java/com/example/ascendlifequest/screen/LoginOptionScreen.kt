package com.example.ascendlifequest.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun LoginOptionScreen(navController: NavHostController) {
    AppBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo cercle
            Image(
                painter = painterResource(id = R.drawable.logo_circle),
                contentDescription = "Ascend Logo",
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Bouton connexion
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(55.dp)
            ) {
                Text(
                    "Se connecter",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.MainTextColor
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Pas encore de compte ? S’inscrire",
                color = AppColor.MinusTextColor,
                fontSize = 15.sp,
                modifier = Modifier.clickable { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(35.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialLoginButton(R.drawable.ic_facebook)
                SocialLoginButton(R.drawable.ic_google)
                SocialLoginButton(R.drawable.ic_apple)
            }
        }
    }
}

@Composable
fun SocialLoginButton(iconRes: Int) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(AppColor.MainTextColor, shape = CircleShape)
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "social login",
            modifier = Modifier.size(24.dp)
        )
    }
}
