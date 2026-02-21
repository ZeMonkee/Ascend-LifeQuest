package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.features.friends.SearchUiState
import com.example.ascendlifequest.ui.theme.themeColors

@Composable
fun AddFriendDialog(
        searchQuery: String,
        searchState: SearchUiState,
        addingFriendId: String?,
        requestSentMessage: String?,
        onSearchQueryChange: (String) -> Unit,
        onSendFriendRequest: (UserProfile) -> Unit,
        onDismiss: () -> Unit
) {
    val colors = themeColors()

    Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.darkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Ajouter un ami",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.mainText
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = colors.minusText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Champ de recherche
                OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Rechercher par pseudo...", color = colors.minusText)
                        },
                        leadingIcon = {
                            Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = colors.minusText
                            )
                        },
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = colors.mainText,
                                        unfocusedTextColor = colors.mainText,
                                        focusedBorderColor = colors.lightAccent,
                                        unfocusedBorderColor =
                                                colors.minusText.copy(alpha = 0.5f),
                                        cursorColor = colors.lightAccent
                                ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                )

                // Message de confirmation
                requestSentMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = message,
                            color =
                                    if (message.startsWith("Demande")) colors.lecture
                                    else colors.sport,
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Résultats de recherche
                Box(modifier = Modifier.weight(1f)) {
                    when (searchState) {
                        is SearchUiState.Idle -> {
                            if (searchQuery.isBlank()) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                            text = "Entrez un pseudo pour rechercher",
                                            color = colors.minusText,
                                            textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is SearchUiState.Loading -> {
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = colors.lightAccent) }
                        }
                        is SearchUiState.Success -> {
                            if (searchState.users.isEmpty()) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                            text = "Aucun utilisateur trouvé",
                                            color = colors.minusText,
                                            textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(searchState.users) { user ->
                                        SearchUserItem(
                                                user = user,
                                                onAddClick = { onSendFriendRequest(user) },
                                                isAdding = addingFriendId == user.uid
                                        )
                                    }
                                }
                            }
                        }
                        is SearchUiState.Error -> {
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = searchState.message,
                                        color = colors.minusText,
                                        textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
