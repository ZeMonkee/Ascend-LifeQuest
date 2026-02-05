package com.example.ascendlifequest.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Service Firebase Cloud Messaging pour recevoir les notifications push
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message reçu de: ${remoteMessage.from}")

        // Vérifier si le message contient des données
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Vérifier si le message contient une notification
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // La notification est affichée automatiquement par le système
            // si l'app est en arrière-plan
        }
    }

    /**
     * Gère les messages de données personnalisés
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return

        when (type) {
            "friend_request" -> {
                val senderName = data["sender_name"] ?: "Un utilisateur"
                val senderId = data["sender_id"] ?: return
                NotificationHelper.showFriendRequestNotification(
                    applicationContext,
                    senderName,
                    senderId
                )
            }
            "message" -> {
                val senderName = data["sender_name"] ?: "Un utilisateur"
                val messagePreview = data["message_preview"] ?: "Nouveau message"
                val senderId = data["sender_id"] ?: return
                NotificationHelper.showMessageNotification(
                    applicationContext,
                    senderName,
                    messagePreview,
                    senderId
                )
            }
            "daily_quest" -> {
                NotificationHelper.showDailyQuestNotification(applicationContext)
            }
        }
    }

    /**
     * Appelé quand un nouveau token FCM est généré
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token FCM actualisé: $token")

        // Enregistrer le token dans Firestore pour l'utilisateur connecté
        saveTokenToFirestore(token)
    }

    /**
     * Sauvegarde le token FCM dans Firestore
     */
    private fun saveTokenToFirestore(token: String) {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = sharedPrefs.getString("user_id", null)

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token FCM sauvegardé avec succès")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erreur lors de la sauvegarde du token FCM", e)
                }
        } else {
            // Sauvegarder temporairement le token pour l'utiliser après connexion
            sharedPrefs.edit()
                .putString("pending_fcm_token", token)
                .apply()
        }
    }
}
