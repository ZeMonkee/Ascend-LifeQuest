package com.example.ascendlifequest.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.theme.AppColor
import com.example.ascendlifequest.di.AppViewModelFactory

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // ViewModel instantiation en utilisant la factory simple
    val factory = AppViewModelFactory()
    val viewModel: LoginViewModel = viewModel(factory = factory)

    // Vérifier si l'utilisateur est déjà connecté
    LaunchedEffect(key1 = true) {
        if (viewModel.isUserLoggedIn()) {
            navController.navigate("quetes") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Observing events from ViewModel
    LaunchedEffect(key1 = viewModel.events) {
        viewModel.events.collect { ev ->
            when {
                ev.startsWith("LOGIN_SUCCESS") -> {
                    Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                    navController.navigate("quetes") { popUpTo("login") { inclusive = true } }
                }
                ev.startsWith("LOGIN_FAILED") -> {
                    Toast.makeText(context, ev.removePrefix("LOGIN_FAILED: "), Toast.LENGTH_SHORT).show()
                }
                ev.startsWith("RESET_EMAIL_SENT") -> {
                    Toast.makeText(context, "Instructions de réinitialisation envoyées à votre email", Toast.LENGTH_LONG).show()
                }
                ev.startsWith("RESET_FAILED") -> {
                    Toast.makeText(context, ev.removePrefix("RESET_FAILED: "), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
                color = AppColor.MinusTextColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 40.dp)
                    .clickable {
                        if (email.isNotEmpty()) {
                            viewModel.resetPassword(email)
                        } else {
                            Toast.makeText(context, "Veuillez saisir votre email", Toast.LENGTH_SHORT).show()
                        }
                    }
            )

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        viewModel.login(email, password)
                    } else {
                        Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(55.dp),
                enabled = !isLoading
            ) {
                // UI loading state derived from viewModel.uiState could be used; for simplicity toggling local isLoading
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColor.MainTextColor,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Se connecter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Pas encore de compte ? S'inscrire",
                color = AppColor.MinusTextColor,
                fontSize = 15.sp,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }
    }
}
