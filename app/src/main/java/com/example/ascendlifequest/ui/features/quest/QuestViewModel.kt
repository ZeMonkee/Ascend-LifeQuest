package com.example.ascendlifequest.ui.features.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.util.QuestHelper
import android.content.Context
import com.example.ascendlifequest.data.repository.generateQuestForCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

sealed class QuestUiState {
    object Loading : QuestUiState()
    data class Success(val categories: List<Categorie>, val quests: List<Quest>) : QuestUiState()
    data class Error(val message: String) : QuestUiState()
}

class QuestViewModel(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestUiState>(QuestUiState.Loading)
    val uiState: StateFlow<QuestUiState> = _uiState

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = QuestUiState.Loading
            try {
                val loadedCategories = questRepository.getCategories()
                val loadedQuests = questRepository.getQuests()

                // Restaurer la couleur stockée (int) en Color
                val mappedCategories = loadedCategories.map { cat ->
                    val restoredColor = Color(cat.couleur.value)
                    cat.copy(couleur = restoredColor)
                }

                _uiState.value = QuestUiState.Success(mappedCategories, loadedQuests)
            } catch (e: Exception) {
                _uiState.value = QuestUiState.Error(e.localizedMessage ?: "Erreur chargement quêtes")
            }
        }
    }

    suspend fun generateQuestForRandomCategory(): Boolean {
        return try {
            val loadedCategories = questRepository.getCategories()
            val randomCategory = loadedCategories.randomOrNull()
            if (randomCategory != null) {
                val newQuest = generateQuestForCategory(randomCategory)
                // si generation nécessite repository persistance, appeler questRepository.saveQuest(newQuest)
                // Ici on suppose generateQuestForCategory fait la persistance si besoin.
                newQuest != null
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // Exposer la lecture/écriture de l'état d'une quête via QuestHelper (déplacé depuis la View)
    fun getQuestState(ctx: Context, uid: String, questId: Int): Boolean {
        return try {
            QuestHelper.getQuestState(ctx, uid, questId)
        } catch (e: Exception) {
            false
        }
    }

    fun saveQuestState(ctx: Context, uid: String, questId: Int, state: Boolean) {
        viewModelScope.launch {
            try {
                QuestHelper.saveQuestState(ctx, uid, questId, state)
                // Optionnel : rafraîchir l'état si nécessaire
            } catch (ignored: Exception) {
            }
        }
    }
}
