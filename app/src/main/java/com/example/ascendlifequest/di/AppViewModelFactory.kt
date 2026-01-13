@file:Suppress("UNCHECKED_CAST")
package com.example.ascendlifequest.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.ui.features.auth.LoginViewModel
import com.example.ascendlifequest.ui.features.auth.RegisterViewModel
import com.example.ascendlifequest.ui.features.auth.LoginOptionViewModel
import com.example.ascendlifequest.ui.features.quest.QuestViewModel
import com.example.ascendlifequest.ui.features.settings.SettingsViewModel
import com.example.ascendlifequest.ui.features.profile.AccountViewModel

class AppViewModelFactory(
    private val authRepository: AuthRepository? = null,
    private val questRepository: QuestRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> modelClass.cast(LoginViewModel(authRepository!!))
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> modelClass.cast(RegisterViewModel(authRepository!!))
            modelClass.isAssignableFrom(LoginOptionViewModel::class.java) -> modelClass.cast(LoginOptionViewModel(authRepository!!))
            modelClass.isAssignableFrom(QuestViewModel::class.java) -> modelClass.cast(QuestViewModel(questRepository!!))
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> modelClass.cast(SettingsViewModel(authRepository!!))
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> modelClass.cast(AccountViewModel())
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${'$'}{modelClass.name}")
        }
    }
}
