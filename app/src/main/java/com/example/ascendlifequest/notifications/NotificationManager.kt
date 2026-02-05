package com.example.ascendlifequest.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Gestionnaire centralisé des notifications
 * Écoute les changements Firestore et déclenche les notifications appropriées
 */
class NotificationManager(private val context: Context) {

    private val TAG = "NotificationManager"
    private val firestore = FirebaseFirestore.getInstance()

    private var friendRequestListener: ListenerRegistration? = null
    private var messageListener: ListenerRegistration? = null

    /**
     * Démarre l'écoute des notifications pour un utilisateur
     */
    fun startListening(userId: String) {
        Log.d(TAG, "Démarrage de l'écoute des notifications pour $userId")

        // Arrêter les listeners existants avant d'en créer de nouveaux
        stopListening()

        // Écouter les demandes d'amis
        listenForFriendRequests(userId)

        // Écouter les nouveaux messages
        listenForMessages(userId)
    }

    /**
     * Arrête l'écoute des notifications
     */
    fun stopListening() {
        Log.d(TAG, "Arrêt de l'écoute des notifications")
        friendRequestListener?.remove()
        messageListener?.remove()
        friendRequestListener = null
        messageListener = null
    }

    /**
     * Écoute les nouvelles demandes d'amis
     */
    private fun listenForFriendRequests(userId: String) {
        friendRequestListener = firestore.collection("friendships")
            .whereEqualTo("friendId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'écoute des demandes d'amis", error)
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val senderId = change.document.getString("userId") ?: return@forEach

                        // Récupérer le nom de l'expéditeur
                        firestore.collection("profile")
                            .document(senderId)
                            .get()
                            .addOnSuccessListener { senderDoc ->
                                val senderName = senderDoc.getString("pseudo") ?: "Un utilisateur"

                                // Afficher la notification
                                NotificationHelper.showFriendRequestNotification(
                                    context,
                                    senderName,
                                    senderId
                                )

                                Log.d(TAG, "Notification de demande d'ami affichée: $senderName")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Erreur lors de la récupération du profil expéditeur", e)
                            }
                    }
                }
            }
    }

    /**
     * Écoute les nouveaux messages
     */
    private fun listenForMessages(userId: String) {
        messageListener = firestore.collection("messages")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Erreur lors de l'écoute des messages", error)
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val senderId = change.document.getString("senderId") ?: return@forEach
                        val messageContent = change.document.getString("content") ?: "Nouveau message"
                        val messagePreview = if (messageContent.length > 50) {
                            messageContent.substring(0, 50) + "..."
                        } else {
                            messageContent
                        }

                        // Récupérer le nom de l'expéditeur
                        firestore.collection("profile")
                            .document(senderId)
                            .get()
                            .addOnSuccessListener { senderDoc ->
                                val senderName = senderDoc.getString("pseudo") ?: "Un utilisateur"

                                // Afficher la notification
                                NotificationHelper.showMessageNotification(
                                    context,
                                    senderName,
                                    messagePreview,
                                    senderId
                                )

                                Log.d(TAG, "Notification de message affichée: $senderName")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Erreur lors de la récupération du profil expéditeur", e)
                            }
                    }
                }
            }
    }

    companion object {
        @Volatile
        private var instance: NotificationManager? = null

        /**
         * Obtient l'instance singleton du NotificationManager
         */
        fun getInstance(context: Context): NotificationManager {
            return instance ?: synchronized(this) {
                instance ?: NotificationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
