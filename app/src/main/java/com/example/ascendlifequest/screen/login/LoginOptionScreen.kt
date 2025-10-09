package com.example.ascendlifequest.screen.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import com.example.ascendlifequest.components.login.SocialLoginButton
import com.example.ascendlifequest.service.AuthService
import com.example.ascendlifequest.ui.theme.AppColor
import kotlinx.coroutines.launch

private const val TAG = "LoginOptionScreen"

@Composable
fun LoginOptionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthService(context) }
    val scope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Vérifier si l'utilisateur est déjà connecté
    LaunchedEffect(key1 = true) {
        if (authService.isUserLoggedIn()) {
            Log.d(TAG, "Utilisateur déjà connecté, redirection vers les quêtes")
            navigateToQuests(navController)
        }
    }

    // Launcher pour la connexion Google avec une meilleure gestion des erreurs
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Résultat Google Sign In reçu: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            isGoogleLoading = true
            scope.launch {
                try {
                    Log.d(TAG, "Traitement du résultat Google Sign In")
                    val signInResult = authService.handleGoogleSignInResult(result.data)

                    signInResult.fold(
                        onSuccess = {
                            Log.d(TAG, "Connexion Google réussie: ${it.email}")
                            Toast.makeText(context, "Connexion réussie avec ${it.email}", Toast.LENGTH_LONG).show()
                            navigateToQuests(navController)
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "Échec de connexion Google", exception)
                            Toast.makeText(context, "Échec: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception lors du traitement de connexion Google", e)
                    Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isGoogleLoading = false
                }
            }
        } else {
            isGoogleLoading = false
            if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.w(TAG, "Connexion Google annulée par l'utilisateur")
                Toast.makeText(context, "Connexion Google annulée", Toast.LENGTH_SHORT).show()
            } else {
                Log.w(TAG, "Résultat inattendu de connexion Google: ${result.resultCode}")
                Toast.makeText(context, "La connexion Google a échoué", Toast.LENGTH_SHORT).show()
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
                text = "Pas encore de compte ? S'inscrire",
                color = AppColor.MinusTextColor,
                fontSize = 15.sp,
                modifier = Modifier.clickable { navController.navigate("register") }
            )

            Spacer(modifier = Modifier.height(35.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialLoginButton(R.drawable.ic_facebook) {
                    Toast.makeText(context, "Connexion Facebook non implémentée", Toast.LENGTH_SHORT).show()
                }

                // Bouton Google avec gestion améliorée
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(AppColor.MainTextColor, shape = CircleShape)
                        .clickable {
                            if (!isGoogleLoading) {
                                isGoogleLoading = true
                                Log.d(TAG, "Bouton Google cliqué - lancement du processus de connexion")
                                try {
                                    val activity = context as? ComponentActivity
                                    if (activity != null) {
                                        authService.signInWithGoogle(activity, googleSignInLauncher)
                                    } else {
                                        isGoogleLoading = false
                                        Log.e(TAG, "Contexte invalide pour Google Sign-In")
                                        Toast.makeText(context, "Erreur: Contexte invalide", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isGoogleLoading = false
                                    Log.e(TAG, "Erreur lors du lancement de la connexion Google", e)
                                    Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Log.d(TAG, "Connexion Google déjà en cours, ignorer")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google login",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                SocialLoginButton(R.drawable.ic_apple) {
                    Toast.makeText(context, "Connexion Apple non implémentée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Fonction utilitaire pour la navigation
private fun navigateToQuests(navController: NavHostController) {
    navController.navigate("quetes") {
        popUpTo("login_option") { inclusive = true }
    }
}
