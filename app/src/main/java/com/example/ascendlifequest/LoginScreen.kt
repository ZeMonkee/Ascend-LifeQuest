package com.example.ascendlifequest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

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

            Spacer(modifier = Modifier.height(40.dp))

            // Email
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Adresse e-mail") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.85f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    disabledContainerColor = Color.White.copy(alpha = 0.85f),
                    errorContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),

                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.85f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    disabledContainerColor = Color.White.copy(alpha = 0.85f),
                    errorContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),

                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Mot de passe oublié",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 40.dp)
                    .clickable { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(55.dp)
            ) {
                Text("Se connecter", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Pas encore de compte ? S’inscrire",
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.clickable { /* TODO */ }
            )
        }
    }
}

