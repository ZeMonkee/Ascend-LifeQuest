@file:Suppress("UNCHECKED_CAST")

package com.example.ascendlifequest.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.ProfileRepositoryImpl
import com.example.ascendlifequest.data.repository.QuestGeneratorRepository
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.ui.features.auth.LoginOptionViewModel
import com.example.ascendlifequest.ui.features.auth.LoginViewModel
import com.example.ascendlifequest.ui.features.auth.RegisterViewModel
import com.example.ascendlifequest.ui.features.profile.AccountViewModel
import com.example.ascendlifequest.ui.features.quest.QuestViewModel
import com.example.ascendlifequest.ui.features.settings.SettingsViewModel

class AppViewModelFactory(
        private val authRepository: AuthRepository? = null,
        private val questRepository: QuestRepository? = null,
        private val questGeneratorRepository: QuestGeneratorRepository? = null,
        private val profileRepository: ProfileRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Fournir un AuthRepository par défaut si non fourni
        val providedAuthRepo = authRepository ?: AuthRepositoryImpl(AuthService())

        // Fournir un ProfileRepository par défaut si non fourni
        val providedProfileRepo = profileRepository ?: ProfileRepositoryImpl(providedAuthRepo)

        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                    modelClass.cast(LoginViewModel(providedAuthRepo))
            modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                    modelClass.cast(RegisterViewModel(providedAuthRepo))
            modelClass.isAssignableFrom(LoginOptionViewModel::class.java) ->
                    modelClass.cast(LoginOptionViewModel(providedAuthRepo))
            modelClass.isAssignableFrom(QuestViewModel::class.java) ->
                    modelClass.cast(
                            QuestViewModel(
                                    questRepository!!,
                                    questGeneratorRepository!!,
                                    providedAuthRepo,
                                    providedProfileRepo
                            )
                    )
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                    modelClass.cast(SettingsViewModel(providedAuthRepo))
            modelClass.isAssignableFrom(AccountViewModel::class.java) ->
                    modelClass.cast(AccountViewModel(providedAuthRepo))
            else ->
                    throw IllegalArgumentException(
                            "Unknown ViewModel class: ${'$'}{modelClass.name}"
                    )
        }
    }
}
