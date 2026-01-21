package com.example.ascendlifequest.ui.features.friends.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.ascendlifequest.data.model.Notification
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.ui.theme.AppColor

@Composable
fun PendingRequestsDialog(
        pendingRequests: List<UserProfile>,
        notifications: List<Notification>,
        onAcceptRequest: (UserProfile) -> Unit,
        onDeclineRequest: (UserProfile) -> Unit,
        onDeleteNotification: (String) -> Unit,
        onDismiss: () -> Unit
) {
    val totalCount = pendingRequests.size + notifications.size

    Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.6f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColor.DarkBlueColor)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Notifications ($totalCount)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColor.MainTextColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = AppColor.MinusTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Liste des demandes et notifications
                if (pendingRequests.isEmpty() && notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    text = "Aucune notification",
                                    color = AppColor.MinusTextColor,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Notifications (demandes refusées, etc.)
                        if (notifications.isNotEmpty()) {
                            item {
                                Text(
                                        text = "Notifications",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColor.MinusTextColor,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(notifications) { notification ->
                                NotificationItem(
                                        message = notification.message,
                                        fromUserPseudo = notification.fromUserPseudo,
                                        onDismiss = { onDeleteNotification(notification.id) }
                                )
                            }
                        }

                        // Demandes d'amis en attente
                        if (pendingRequests.isNotEmpty()) {
                            item {
                                Text(
                                        text = "Demandes d'amis",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColor.MinusTextColor,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(pendingRequests) { request ->
                                FriendRequestItem(
                                        user = request,
                                        onAccept = { onAcceptRequest(request) },
                                        onDecline = { onDeclineRequest(request) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
