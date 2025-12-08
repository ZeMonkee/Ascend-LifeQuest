package com.example.ascendlifequest.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.components.AppBackground
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.sp
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun AccountScreen(navController: NavHostController, vm: AccountViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.loadCurrentUser() }

    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        AppBackground {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp)) {
                // Header with back button (chevron text)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // simple text chevron instead of icon
                        Text(text = "‹", color = AppColor.MainTextColor, fontSize = 24.sp)
                    }
                    Text(text = "COMPTE", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColor.MainTextColor, modifier = Modifier.fillMaxWidth().padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // Avatar par défaut
                    Image(
                        painter = painterResource(id = R.drawable.generic_pfp),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (state) {
                    is AccountUiState.ReauthRequired -> {
                        val action = (state as AccountUiState.ReauthRequired).action
                        val pending = (state as AccountUiState.ReauthRequired).pendingValue

                        var reauthDialogVisible by remember { mutableStateOf(true) }
                        var currentPassword by remember { mutableStateOf("") }
                        var reauthTrigger by remember { mutableStateOf<String?>(null) }
                        var reauthLoading by remember { mutableStateOf(false) }
                        var reauthError by remember { mutableStateOf<String?>(null) }

                        if (reauthDialogVisible) {
                            AlertDialog(
                                onDismissRequest = { if (!reauthLoading) reauthDialogVisible = false },
                                title = { Text("Ré-authentification requise") },
                                text = {
                                    Column {
                                        OutlinedTextField(
                                            value = currentPassword,
                                            onValueChange = { currentPassword = it; reauthError = null },
                                            label = { Text("Mot de passe actuel") },
                                            visualTransformation = PasswordVisualTransformation()
                                        )
                                        if (!reauthError.isNullOrEmpty()) {
                                            Text(text = reauthError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { if (!reauthLoading) reauthTrigger = currentPassword }) { Text("Valider") }
                                },
                                dismissButton = { TextButton(onClick = { if (!reauthLoading) reauthDialogVisible = false }) { Text("Annuler") } }
                            )
                        }

                        LaunchedEffect(reauthTrigger) {
                            val pw = reauthTrigger
                            if (!pw.isNullOrEmpty()) {
                                reauthLoading = true
                                reauthError = null
                                val res = vm.reauthenticate(pw)
                                if (res.isSuccess) {
                                    // proceed with pending action
                                    if (action == "email") vm.updateEmail(pending)
                                    if (action == "password") vm.updatePassword(pending)
                                    reauthDialogVisible = false
                                } else {
                                    reauthError = res.exceptionOrNull()?.message ?: "Ré-authentification échouée"
                                }
                                reauthLoading = false
                                reauthTrigger = null
                            }
                        }
                    }
                    is AccountUiState.Loaded -> {
                        val email = (state as AccountUiState.Loaded).email ?: "Email inconnu"
                        Text(text = "Email: $email", color = AppColor.MainTextColor, fontWeight = FontWeight.Medium)
                    }
                    is AccountUiState.Loading -> CircularProgressIndicator()
                    is AccountUiState.Error -> {
                        val msg = (state as AccountUiState.Error).message
                        Text(text = msg, color = Color.Red)
                        LaunchedEffect(msg) {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                    is AccountUiState.Success -> {
                        val msg = (state as AccountUiState.Success).message
                        Text(text = msg, color = Color.Green)
                        LaunchedEffect(msg) {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = { showEmailDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Modifier l'e-mail")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showPasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Modifier le mot de passe")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    vm.signOut()
                    navController.navigate("login_option") {
                        popUpTo(0) { inclusive = true }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Se déconnecter")
                }

                // Dialogues
                if (showEmailDialog) {
                    AlertDialog(
                        onDismissRequest = { showEmailDialog = false },
                        title = { Text("Modifier l'e-mail") },
                        text = {
                            Column {
                                OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Nouvel e-mail") })
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showEmailDialog = false
                                vm.updateEmail(newEmail)
                            }) { Text("Valider") }
                        },
                        dismissButton = { TextButton(onClick = { showEmailDialog = false }) { Text("Annuler") } }
                    )
                }

                // Password change flow: first ask current password, then ask new password
                if (showPasswordDialog) {
                    var currentPassword by remember { mutableStateOf("") }
                    var askingNew by remember { mutableStateOf(false) }
                    var confirmNew by remember { mutableStateOf("") }
                    var reauthTrigger by remember { mutableStateOf<String?>(null) }
                    var reauthLoading by remember { mutableStateOf(false) }
                    var reauthError by remember { mutableStateOf<String?>(null) }
                    var newPasswordError by remember { mutableStateOf<String?>(null) }

                    if (!askingNew) {
                        // Dialog to enter current password for re-authentication
                        AlertDialog(
                            onDismissRequest = { if (!reauthLoading) showPasswordDialog = false },
                            title = { Text("Vérification") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = currentPassword,
                                        onValueChange = { currentPassword = it; reauthError = null },
                                        label = { Text("Mot de passe actuel") },
                                        visualTransformation = PasswordVisualTransformation()
                                    )
                                    if (!reauthError.isNullOrEmpty()) Text(text = reauthError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { if (!reauthLoading) reauthTrigger = currentPassword }) { Text("Valider") }
                            },
                            dismissButton = { TextButton(onClick = { if (!reauthLoading) showPasswordDialog = false }) { Text("Annuler") } }
                        )

                        LaunchedEffect(reauthTrigger) {
                            val pw = reauthTrigger
                            if (!pw.isNullOrEmpty()) {
                                reauthLoading = true
                                reauthError = null
                                val res = vm.reauthenticate(pw)
                                if (res.isSuccess) {
                                    askingNew = true
                                } else {
                                    reauthError = res.exceptionOrNull()?.message ?: "Ré-authentification échouée"
                                }
                                reauthLoading = false
                                reauthTrigger = null
                            }
                        }
                    } else {
                        // Now ask for new password + confirmation
                        var newPassword by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showPasswordDialog = false },
                            title = { Text("Nouveau mot de passe") },
                            text = {
                                Column {
                                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; newPasswordError = null }, label = { Text("Nouveau mot de passe") }, visualTransformation = PasswordVisualTransformation())
                                    OutlinedTextField(value = confirmNew, onValueChange = { confirmNew = it; newPasswordError = null }, label = { Text("Confirmer le mot de passe") }, visualTransformation = PasswordVisualTransformation())
                                    if (!newPasswordError.isNullOrEmpty()) Text(text = newPasswordError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (newPassword.length < 6) {
                                        newPasswordError = "Le mot de passe doit contenir au moins 6 caractères"
                                    } else if (newPassword != confirmNew) {
                                        newPasswordError = "Les mots de passe ne correspondent pas"
                                    } else {
                                        showPasswordDialog = false
                                        vm.updatePassword(newPassword)
                                    }
                                }) { Text("Valider") }
                            },
                            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Annuler") } }
                        )
                    }
                }

            }
        }
    }
}
