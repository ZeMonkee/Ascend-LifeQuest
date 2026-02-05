package com.example.ascendlifequest.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ascendlifequest.MainActivity

/**
 * Classe helper pour gérer les notifications de l'application
 */
object NotificationHelper {

    // IDs des canaux de notification
    const val CHANNEL_FRIEND_REQUESTS = "friend_requests_channel"
    const val CHANNEL_MESSAGES = "messages_channel"
    const val CHANNEL_DAILY_QUESTS = "daily_quests_channel"

    // IDs de notification
    const val NOTIFICATION_ID_FRIEND_REQUEST = 1001
    const val NOTIFICATION_ID_MESSAGE = 1002
    const val NOTIFICATION_ID_DAILY_QUEST = 1003

    /**
     * Crée tous les canaux de notification nécessaires
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal pour les demandes d'amis
            val friendRequestChannel = NotificationChannel(
                CHANNEL_FRIEND_REQUESTS,
                "Demandes d'amis",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les nouvelles demandes d'amis"
                enableLights(true)
                enableVibration(true)
            }

            // Canal pour les messages
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les nouveaux messages"
                enableLights(true)
                enableVibration(true)
            }

            // Canal pour les quêtes journalières
            val dailyQuestsChannel = NotificationChannel(
                CHANNEL_DAILY_QUESTS,
                "Quêtes journalières",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels quotidiens pour vérifier vos nouvelles quêtes"
                enableLights(true)
                enableVibration(false)
            }

            // Enregistrer les canaux
            notificationManager.createNotificationChannel(friendRequestChannel)
            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(dailyQuestsChannel)
        }
    }

    /**
     * Affiche une notification pour une nouvelle demande d'ami
     */
    fun showFriendRequestNotification(
        context: Context,
        senderName: String,
        senderId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "friend_requests")
            putExtra("sender_id", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FRIEND_REQUESTS)
            .setSmallIcon(android.R.drawable.ic_menu_add) // TODO: Remplacer par votre icône
            .setContentTitle("Nouvelle demande d'ami")
            .setContentText("$senderName vous a envoyé une demande d'ami")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_FRIEND_REQUEST + senderId.hashCode(),
            notification
        )
    }

    /**
     * Affiche une notification pour un nouveau message
     */
    fun showMessageNotification(
        context: Context,
        senderName: String,
        messagePreview: String,
        senderId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "messages")
            putExtra("sender_id", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_menu_send) // TODO: Remplacer par votre icône
            .setContentTitle("Nouveau message de $senderName")
            .setContentText(messagePreview)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messagePreview))
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_MESSAGE + senderId.hashCode(),
            notification
        )
    }

    /**
     * Affiche une notification de rappel pour les quêtes journalières
     */
    fun showDailyQuestNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "daily_quests")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_QUESTS)
            .setSmallIcon(android.R.drawable.ic_menu_today) // TODO: Remplacer par votre icône
            .setContentTitle("Nouvelles quêtes disponibles !")
            .setContentText("Découvrez vos nouvelles quêtes journalières")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_DAILY_QUEST,
            notification
        )
    }

    /**
     * Annule toutes les notifications
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Annule une notification spécifique
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
