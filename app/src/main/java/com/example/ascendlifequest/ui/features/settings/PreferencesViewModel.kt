package com.example.ascendlifequest.ui.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.local.PreferencesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel(
    context: Context,
    private val userId: String
) : ViewModel() {

    // use applicationContext to avoid leaking Activity context
    private val appContext = context.applicationContext
    private val prefsMap = mutableMapOf<String, MutableStateFlow<Int>>()

    fun getPreferenceFlow(key: String, default: Int = 3): StateFlow<Int> {
        return prefsMap.getOrPut(key) {
            val initial = PreferencesHelper.getPreference(appContext, userId, key, default)
            MutableStateFlow(initial)
        }
    }

    fun savePreference(key: String, value: Int) {
        viewModelScope.launch {
            try {
                PreferencesHelper.savePreference(appContext, userId, key, value)
                prefsMap.getOrPut(key) { MutableStateFlow(value) }.value = value
            } catch (ignored: Exception) {
                // ignore or log
            }
        }
    }
}
