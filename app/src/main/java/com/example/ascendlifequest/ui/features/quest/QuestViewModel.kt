package com.example.ascendlifequest.ui.features.quest

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.data.repository.generateQuestForCategory
import com.example.ascendlifequest.util.CategorySelector
import com.example.ascendlifequest.util.QuestHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestViewModel(
    private val questRepository: QuestRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    // Exposer l'id utilisateur courant via le ViewModel afin d'éviter la création de services dans la View
    private val _currentUserId = MutableStateFlow(authRepository?.getCurrentUserId() ?: "")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _categories = MutableStateFlow<List<Categorie>>(emptyList())
    val categories: StateFlow<List<Categorie>> = _categories.asStateFlow()

    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationProgress = MutableStateFlow(0)
    val generationProgress: StateFlow<Int> = _generationProgress.asStateFlow()

    private val _questCounter = MutableStateFlow(0)
    val questCounter: StateFlow<Int> = _questCounter.asStateFlow()

    private val _completedQuestsCount = MutableStateFlow(0)
    val completedQuestsCount: StateFlow<Int> = _completedQuestsCount.asStateFlow()

    private val _showMaxQuestsDialog = MutableStateFlow(false)
    val showMaxQuestsDialog: StateFlow<Boolean> = _showMaxQuestsDialog.asStateFlow()

    fun loadData(context: Context, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedCategories = questRepository.getCategories()
                val loadedQuests = questRepository.getQuests()

                _categories.value =
                    loadedCategories.map { cat ->
                        val restoredColor = Color(cat.couleur.value)
                        cat.copy(couleur = restoredColor)
                    }
                _quests.value = loadedQuests
                _questCounter.value = QuestHelper.getQuestCounter(context)

                val questIds = loadedQuests.map { it.id }
                _completedQuestsCount.value =
                    QuestHelper.getCompletedQuestsCount(context, userId, questIds)

                // Mettre à jour le userId interne si besoin
                _currentUserId.update { userId }
            } catch (e: Exception) {
                Log.e("QuestViewModel", "Error loading data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateInitialQuests(context: Context, userId: String) {
        viewModelScope.launch {
            val maxQuests = QuestHelper.getMaxQuests()
            val currentCounter = QuestHelper.getQuestCounter(context)
            if (currentCounter >= maxQuests) {
                _isLoading.value = false
                return@launch
            }

            _isGenerating.value = true
            _generationProgress.value = currentCounter

            // Charger les catégories si pas encore chargées
            if (_categories.value.isEmpty()) {
                loadData(context, userId) // Assure que les catégories sont dispos
                // Attendre un peu que loadData finisse ou utiliser les valeurs retournées par le
                // repo direct
                // Simplification: on recharge direct du repo ici pour être sûr
                val cats = questRepository.getCategories()
                _categories.value = cats.map { cat -> cat.copy(couleur = Color(cat.couleur.value)) }
            }

            val loadedCategories = _categories.value

            while (QuestHelper.getQuestCounter(context) < maxQuests) {
                val selectedCategory =
                    CategorySelector.selectWeightedCategory(context, userId, loadedCategories)
                        ?: break

                try {
                    val newQuest = generateQuestForCategory(context, selectedCategory)
                    if (newQuest != null) {
                        QuestHelper.incrementQuestCounter(context)
                        _generationProgress.value = QuestHelper.getQuestCounter(context)
                        Log.d(
                            "QuestViewModel",
                            "✅ Quête générée (${_generationProgress.value}/$maxQuests) : ${newQuest.nom}"
                        )

                        // Refresh quests locally
                        _quests.value = questRepository.getQuests()
                    } else {
                        Log.w("QuestViewModel", "⚠️ Échec génération, retry dans 10s...")
                        delay(10000)
                    }
                } catch (e: Exception) {
                    Log.e("QuestViewModel", "❌ Erreur génération, retry dans 10s...", e)
                    delay(10000)
                }
            }

            _questCounter.value = QuestHelper.getQuestCounter(context)
            _isGenerating.value = false
            _isLoading.value = false
        }
    }

    fun generateNewQuest(context: Context, userId: String) {
        viewModelScope.launch {
            if (!QuestHelper.canGenerateMoreQuests(context)) {
                _showMaxQuestsDialog.value = true
            } else {
                // Double check generation not in progress?
                if (_isGenerating.value) return@launch

                val selectedCategory =
                    CategorySelector.selectWeightedCategory(context, userId, _categories.value)
                if (selectedCategory != null) {
                    val newQuest = generateQuestForCategory(context, selectedCategory)
                    if (newQuest != null) {
                        QuestHelper.incrementQuestCounter(context)
                        _questCounter.value = QuestHelper.getQuestCounter(context)
                        Log.d("QuestViewModel", "✅ Quête générée : ${newQuest.nom}")
                        loadData(context, userId) // Refresh full data
                    } else {
                        Log.e("QuestViewModel", "❌ Échec génération quête")
                    }
                }
            }
        }
    }

    fun clearDatabase(context: Context, userId: String) {
        viewModelScope.launch {
            questRepository.clearAllQuests()
            QuestHelper.resetQuestCounter(context)
            QuestHelper.clearQuest(context, userId)
            _questCounter.value = 0
            _completedQuestsCount.value = 0
            loadData(context, userId)
        }
    }

    fun dismissDialog() {
        _showMaxQuestsDialog.value = false
    }

    fun updateQuestState(isDone: Boolean) {
        if (isDone) {
            _completedQuestsCount.value += 1
        } else {
            _completedQuestsCount.value = (_completedQuestsCount.value - 1).coerceAtLeast(0)
        }
    }
}
