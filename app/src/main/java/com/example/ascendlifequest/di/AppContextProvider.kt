package com.example.ascendlifequest.di

import android.annotation.SuppressLint
import android.content.Context

/**
 * Singleton pour fournir le contexte de l'application aux repositories.
 * Utilisé pour accéder à Room et aux services nécessitant un contexte.
 */
@SuppressLint("StaticFieldLeak")
object AppContextProvider {

    private var applicationContext: Context? = null

    /**
     * Initialise le provider avec le contexte de l'application.
     * Doit être appelé dans la classe Application ou MainActivity.
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Récupère le contexte de l'application.
     * @throws IllegalStateException si le provider n'a pas été initialisé
     */
    fun getContext(): Context {
        return applicationContext
            ?: throw IllegalStateException("AppContextProvider n'a pas été initialisé. Appelez initialize() dans Application ou MainActivity.")
    }

    /**
     * Récupère le contexte de façon nullable (pour les cas où on veut gérer l'absence de contexte)
     */
    fun getContextOrNull(): Context? = applicationContext
}
