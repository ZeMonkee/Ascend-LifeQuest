package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun DeleteFriendConfirmDialog(friend: UserProfile, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = themeColors()

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = "Supprimer l'ami",
                        color = colors.mainText,
                        fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                        text =
                                "Voulez-vous vraiment supprimer ${friend.pseudo} de votre liste d'amis ?",
                        color = colors.minusText
                )
            },
            confirmButton = {
                Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.sport)
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Annuler", color = colors.minusText)
                }
            },
            containerColor = colors.darkBackground
    )
}
