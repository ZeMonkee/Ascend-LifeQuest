package com.example.ascendlifequest.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

/**
 * Gestionnaire pour planifier les notifications quotidiennes
 */
object NotificationScheduler {

    private const val DAILY_QUEST_WORK_NAME = "daily_quest_notification"
    private const val TAG = "NotificationScheduler"

    /**
     * Planifie la notification quotidienne des quêtes
     * Par défaut à 9h00 chaque jour
     */
    fun scheduleDailyQuestNotification(
        context: Context,
        hourOfDay: Int = 9,
        minute: Int = 0
    ) {
        // Calculer le délai initial jusqu'à la prochaine occurrence de l'heure choisie
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Si l'heure cible est déjà passée aujourd'hui, planifier pour demain
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        Log.d(TAG, "Planification de la notification quotidienne dans ${initialDelay / 1000 / 60} minutes")

        // Créer la contrainte pour s'assurer que l'appareil est réveillé
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Autoriser même avec batterie faible
            .build()

        // Créer la requête de travail périodique (quotidienne)
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyQuestNotificationWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DAILY_QUEST_WORK_NAME)
            .build()

        // Planifier le travail
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_QUEST_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Garde la planification existante si présente
            dailyWorkRequest
        )

        Log.d(TAG, "Notification quotidienne planifiée avec succès")
    }

    /**
     * Annule la notification quotidienne des quêtes
     */
    fun cancelDailyQuestNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_QUEST_WORK_NAME)
        Log.d(TAG, "Notification quotidienne annulée")
    }


    /**
     * Met à jour l'heure de la notification quotidienne
     */
    fun updateDailyQuestNotificationTime(context: Context, hourOfDay: Int, minute: Int) {
        cancelDailyQuestNotification(context)
        scheduleDailyQuestNotification(context, hourOfDay, minute)
    }
}
