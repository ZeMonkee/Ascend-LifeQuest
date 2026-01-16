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
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val factory = AppViewModelFactory()
    val viewModel: RegisterViewModel = viewModel(factory = factory)

    // Observing events from ViewModel in a Compose coroutine
    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when {
                ev.startsWith("REGISTER_SUCCESS") -> {
                    Toast.makeText(context, "Inscription réussie", Toast.LENGTH_SHORT).show()
                    // Après création du compte, on redirige d'abord vers les préférences
                    navController.navigate("preference") { popUpTo("register") { inclusive = true } }
                }
                ev.startsWith("REGISTER_FAILED") -> {
                    Toast.makeText(context, ev.removePrefix("REGISTER_FAILED: "), Toast.LENGTH_SHORT).show()
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

            Spacer(modifier = Modifier.height(15.dp))

            // Confirm Password
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirmer le mot de passe") },
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

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                        if (password == confirmPassword) {
                            isLoading = true
                            viewModel.register(email, password)
                        } else {
                            Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                        }
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
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColor.MainTextColor,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "S'inscrire",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColor.MainTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Déjà un compte ? Se connecter",
                color = AppColor.MinusTextColor,
                fontSize = 15.sp,
                modifier = Modifier.clickable { navController.popBackStack() }
            )
        }
    }
}
