package com.example.ascendlifequest.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ascendlifequest.R
import com.example.ascendlifequest.di.AppViewModelFactory
import com.example.ascendlifequest.ui.components.AppBackground
import com.example.ascendlifequest.ui.components.AppBottomNavBar
import com.example.ascendlifequest.ui.components.AppHeader
import com.example.ascendlifequest.ui.components.BottomNavItem
import com.example.ascendlifequest.ui.features.profile.components.AccountAvatar
import com.example.ascendlifequest.ui.features.profile.components.SuccessNotice
import com.example.ascendlifequest.ui.theme.themeColors
import kotlinx.coroutines.delay

@Composable
fun AccountScreen(navController: NavHostController) {
    AccountScreenWithBottomNav(navController)
}

@Composable
fun AccountScreenWithBottomNav(navController: NavHostController) {
    AppBottomNavBar(navController = navController, current = BottomNavItem.Profil) { innerPadding ->
        AppBackground {
            Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
            ) {
                AppHeader(title = "COMPTE")
                AccountScreenContent(
                        navController = navController,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountScreenContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val factory = AppViewModelFactory()
    val vm: AccountViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }

    var pendingEmailVerificationRedirect by remember { mutableStateOf(false) }
    var pendingPasswordChangeRedirect by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }

    var currentPassword by remember { mutableStateOf("") }
    var askingNew by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNew by remember { mutableStateOf("") }
    var reauthTriggerPassword by remember { mutableStateOf<String?>(null) }
    var reauthLoadingPassword by remember { mutableStateOf(false) }
    var reauthErrorPassword by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }

    var reauthDialogVisible by remember { mutableStateOf(false) }
    var reauthCurrentPassword by remember { mutableStateOf("") }
    var reauthTriggerReauth by remember { mutableStateOf<String?>(null) }
    var reauthLoadingReauth by remember { mutableStateOf(false) }
    var reauthErrorReauth by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.loadCurrentUser() }

    LaunchedEffect(state) {
        if (state is AccountUiState.ReauthRequired) {
            reauthDialogVisible = true
        }
    }

    LaunchedEffect(
            showEmailDialog,
            showPasswordDialog,
            pendingEmailVerificationRedirect,
            pendingPasswordChangeRedirect
    ) {
        while (true) {
            delay(5000)
            val currentState = state
            val isReauthRequired = currentState is AccountUiState.ReauthRequired
            val isShowingMessage =
                    currentState is AccountUiState.Success || currentState is AccountUiState.Error

            if (!showEmailDialog &&
                            !showPasswordDialog &&
                            !isReauthRequired &&
                            !isShowingMessage &&
                            !pendingEmailVerificationRedirect &&
                            !pendingPasswordChangeRedirect
            ) {
                vm.refreshUser()
            }
        }
    }

    LaunchedEffect(pendingEmailVerificationRedirect) {
        if (pendingEmailVerificationRedirect) {
            delay(10000)
            vm.signOut()
            navController.navigate("login_option") { popUpTo(0) { inclusive = true } }
        }
    }

    LaunchedEffect(pendingPasswordChangeRedirect) {
        if (pendingPasswordChangeRedirect) {
            delay(5000)
            vm.signOut()
            navController.navigate("login_option") { popUpTo(0) { inclusive = true } }
        }
    }

    val colors = themeColors()

    Card(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.darkBackground),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AccountAvatar(resId = R.drawable.generic_pfp, size = 120.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayEmail =
                    when (state) {
                        is AccountUiState.Loaded -> (state as AccountUiState.Loaded).email
                        is AccountUiState.Success -> (state as AccountUiState.Success).email
                        is AccountUiState.Error -> (state as AccountUiState.Error).email
                        else -> null
                    }

            val displayEmailText = displayEmail ?: "Chargement..."

            Text(
                    text = "Email: $displayEmailText",
                    color =
                            if (displayEmail != null) colors.mainText
                            else colors.minusText,
                    fontWeight = if (displayEmail != null) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 8.dp)
            )

            if (displayEmail == null) {
                LaunchedEffect(Unit) {
                    delay(500)
                    vm.refreshUser()
                }
            }

            when (state) {
                is AccountUiState.ReauthRequired -> {
                    val action = (state as AccountUiState.ReauthRequired).action
                    val pending = (state as AccountUiState.ReauthRequired).pendingValue

                    if (reauthDialogVisible) {
                        AlertDialog(
                                onDismissRequest = {
                                    if (!reauthLoadingReauth) {
                                        reauthDialogVisible = false
                                        reauthCurrentPassword = ""
                                        reauthErrorReauth = null
                                    }
                                },
                                title = { Text("Ré-authentification requise") },
                                text = {
                                    Column {
                                        OutlinedTextField(
                                                value = reauthCurrentPassword,
                                                onValueChange = {
                                                    reauthCurrentPassword = it
                                                    reauthErrorReauth = null
                                                },
                                                label = { Text("Mot de passe actuel") },
                                                visualTransformation =
                                                        PasswordVisualTransformation()
                                        )
                                        if (!reauthErrorReauth.isNullOrEmpty()) {
                                            Text(
                                                    text = reauthErrorReauth ?: "",
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                            onClick = {
                                                if (!reauthLoadingReauth) {
                                                    reauthTriggerReauth = reauthCurrentPassword
                                                }
                                            }
                                    ) { Text("Valider") }
                                },
                                dismissButton = {
                                    TextButton(
                                            onClick = {
                                                if (!reauthLoadingReauth) {
                                                    reauthDialogVisible = false
                                                    reauthCurrentPassword = ""
                                                    reauthErrorReauth = null
                                                }
                                            }
                                    ) { Text("Annuler") }
                                }
                        )
                    }

                    LaunchedEffect(reauthTriggerReauth) {
                        val pw = reauthTriggerReauth
                        if (!pw.isNullOrEmpty()) {
                            reauthLoadingReauth = true
                            reauthErrorReauth = null
                            val res = vm.reauthenticate(pw)
                            if (res.isSuccess) {
                                if (action == "email") vm.updateEmail(pending)
                                if (action == "password") vm.updatePassword(pending)
                                reauthDialogVisible = false
                                reauthCurrentPassword = ""
                            } else {
                                reauthErrorReauth =
                                        res.exceptionOrNull()?.message
                                                ?: "Ré-authentification échouée"
                            }
                            reauthLoadingReauth = false
                            reauthTriggerReauth = null
                        }
                    }
                }
                is AccountUiState.Loaded -> {}
                is AccountUiState.Loading ->
                        CircularProgressIndicator(color = colors.lightAccent)
                is AccountUiState.Error -> {
                    val msg = (state as AccountUiState.Error).message
                    Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                    )
                }
                is AccountUiState.Success -> {
                    val msg = (state as AccountUiState.Success).message

                    if (msg.contains("e-mail de vérification")) {
                        successMessage = msg
                    } else if (msg.contains("Mot de passe mis à jour")) {
                        passwordSuccessMessage = msg
                    }

                    LaunchedEffect(msg) {
                        if (msg.contains("e-mail de vérification") &&
                                        !pendingEmailVerificationRedirect
                        ) {
                            pendingEmailVerificationRedirect = true
                        } else if (msg.contains("Mot de passe mis à jour") &&
                                        !pendingPasswordChangeRedirect
                        ) {
                            pendingPasswordChangeRedirect = true
                        }
                    }
                }
                else -> {}
            }

            val singleNoticeMessage =
                    when {
                        pendingPasswordChangeRedirect && !passwordSuccessMessage.isNullOrEmpty() ->
                                passwordSuccessMessage
                        pendingEmailVerificationRedirect && !successMessage.isNullOrEmpty() ->
                                successMessage
                        else -> null
                    }

            if (!singleNoticeMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SuccessNotice(message = singleNoticeMessage)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                        onClick = { showEmailDialog = true },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = colors.lightAccent
                                ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                            text = "Modifier l'e-mail",
                            color = colors.mainText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                        onClick = { showPasswordDialog = true },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = colors.lightAccent
                                ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                            text = "Modifier le mot de passe",
                            color = colors.mainText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                        onClick = {
                            vm.signOut()
                            navController.navigate("login_option") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = colors.darkBackground
                                ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                            text = "Se déconnecter",
                            color = colors.mainText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            // Email dialog
            if (showEmailDialog) {
                AlertDialog(
                        onDismissRequest = { showEmailDialog = false },
                        title = { Text("Modifier l'e-mail") },
                        text = {
                            OutlinedTextField(
                                    value = newEmail,
                                    onValueChange = { newEmail = it },
                                    label = { Text("Nouvel e-mail") }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                    onClick = {
                                        showEmailDialog = false
                                        vm.updateEmail(newEmail)
                                    }
                            ) { Text("Valider") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEmailDialog = false }) { Text("Annuler") }
                        }
                )
            }

            // Password dialog flow
            if (showPasswordDialog) {
                if (!askingNew) {
                    AlertDialog(
                            onDismissRequest = {
                                if (!reauthLoadingPassword) {
                                    showPasswordDialog = false
                                    askingNew = false
                                    currentPassword = ""
                                    reauthErrorPassword = null
                                }
                            },
                            title = { Text("Vérification") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                            value = currentPassword,
                                            onValueChange = {
                                                currentPassword = it
                                                reauthErrorPassword = null
                                            },
                                            label = { Text("Mot de passe actuel") },
                                            visualTransformation = PasswordVisualTransformation()
                                    )
                                    if (!reauthErrorPassword.isNullOrEmpty()) {
                                        Text(
                                                text = reauthErrorPassword ?: "",
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                        onClick = {
                                            if (!reauthLoadingPassword) {
                                                reauthTriggerPassword = currentPassword
                                            }
                                        }
                                ) { Text("Valider") }
                            },
                            dismissButton = {
                                TextButton(
                                        onClick = {
                                            if (!reauthLoadingPassword) {
                                                showPasswordDialog = false
                                                askingNew = false
                                                currentPassword = ""
                                                reauthErrorPassword = null
                                            }
                                        }
                                ) { Text("Annuler") }
                            }
                    )

                    LaunchedEffect(reauthTriggerPassword) {
                        val pw = reauthTriggerPassword
                        if (!pw.isNullOrEmpty()) {
                            reauthLoadingPassword = true
                            reauthErrorPassword = null
                            val res = vm.reauthenticate(pw)
                            if (res.isSuccess) {
                                askingNew = true
                                currentPassword = ""
                            } else {
                                reauthErrorPassword =
                                        res.exceptionOrNull()?.message
                                                ?: "Ré-authentification échouée"
                            }
                            reauthLoadingPassword = false
                            reauthTriggerPassword = null
                        }
                    }
                } else {
                    AlertDialog(
                            onDismissRequest = {
                                showPasswordDialog = false
                                askingNew = false
                                newPassword = ""
                                confirmNew = ""
                                newPasswordError = null
                            },
                            title = { Text("Nouveau mot de passe") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                            value = newPassword,
                                            onValueChange = {
                                                newPassword = it
                                                newPasswordError = null
                                            },
                                            label = { Text("Nouveau mot de passe") },
                                            visualTransformation = PasswordVisualTransformation()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                            value = confirmNew,
                                            onValueChange = {
                                                confirmNew = it
                                                newPasswordError = null
                                            },
                                            label = { Text("Confirmer le mot de passe") },
                                            visualTransformation = PasswordVisualTransformation()
                                    )
                                    if (!newPasswordError.isNullOrEmpty()) {
                                        Text(
                                                text = newPasswordError ?: "",
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(
                                        onClick = {
                                            when {
                                                newPassword.length < 6 -> {
                                                    newPasswordError =
                                                            "Le mot de passe doit contenir au moins 6 caractères"
                                                }
                                                newPassword != confirmNew -> {
                                                    newPasswordError =
                                                            "Les mots de passe ne correspondent pas"
                                                }
                                                else -> {
                                                    showPasswordDialog = false
                                                    askingNew = false
                                                    vm.updatePassword(newPassword)
                                                    newPassword = ""
                                                    confirmNew = ""
                                                    newPasswordError = null
                                                }
                                            }
                                        }
                                ) { Text("Valider") }
                            },
                            dismissButton = {
                                TextButton(
                                        onClick = {
                                            showPasswordDialog = false
                                            askingNew = false
                                            newPassword = ""
                                            confirmNew = ""
                                            newPasswordError = null
                                        }
                                ) { Text("Annuler") }
                            }
                    )
                }
            }
        }
    }
}
