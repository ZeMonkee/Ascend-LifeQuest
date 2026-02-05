package com.example.ascendlifequest.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker pour les notifications quotidiennes de quêtes
 */
class DailyQuestNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("DailyQuestWorker", "Envoi de la notification de quêtes journalières")

            // Afficher la notification
            NotificationHelper.showDailyQuestNotification(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyQuestWorker", "Erreur lors de l'envoi de la notification", e)
            Result.failure()
        }
    }
}
