package com.example.ascendlifequest.ui.features.quest

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.model.Categorie
import com.example.ascendlifequest.data.model.Quest
import com.example.ascendlifequest.data.repository.ProfileRepository
import com.example.ascendlifequest.data.repository.QuestGeneratorRepository
import com.example.ascendlifequest.data.repository.QuestRepository
import com.example.ascendlifequest.util.CategorySelector
import com.example.ascendlifequest.util.QuestHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing quest operations including generation, completion tracking, and
 * synchronization with Firebase profiles.
 *
 * @property questRepository Repository for local quest data
 * @property questGeneratorRepository Repository for AI-powered quest generation
 * @property profileRepository Optional repository for user profile updates
 */
class QuestViewModel(
        private val questRepository: QuestRepository,
        private val questGeneratorRepository: QuestGeneratorRepository,
        authRepository: AuthRepository? = null,
        private val profileRepository: ProfileRepository? = null
) : ViewModel() {

    // Exposer l'id utilisateur courant via le ViewModel afin d'éviter la création de services dans
    // la View
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

    private val _completedQuestsCount = MutableStateFlow(0)
    val completedQuestsCount: StateFlow<Int> = _completedQuestsCount.asStateFlow()

    private val _showMaxQuestsDialog = MutableStateFlow(false)
    val showMaxQuestsDialog: StateFlow<Boolean> = _showMaxQuestsDialog.asStateFlow()

    /**
     * Vérifie si l'utilisateur actuel est différent de celui qui a généré les quêtes. Si oui, vide
     * la base de données des quêtes et réinitialise les compteurs.
     * @return true si la base a été vidée (nouvel utilisateur), false sinon
     */
    suspend fun checkAndClearQuestsForNewUser(context: Context, userId: String): Boolean {
        if (QuestHelper.isUserDifferent(context, userId)) {
            Log.d("QuestViewModel", "Utilisateur différent détecté - Nettoyage des quêtes")
            Log.d("QuestViewModel", "   Ancien userId: ${QuestHelper.getQuestUserId(context)}")
            Log.d("QuestViewModel", "   Nouveau userId: $userId")

            // Vider la base de données Room des quêtes
            questRepository.clearAllQuests()

            // Réinitialiser pour le nouvel utilisateur
            QuestHelper.resetForNewUser(context, userId)

            Log.d("QuestViewModel", "Base de données des quêtes vidée pour le nouvel utilisateur")
            return true
        } else {
            // Si c'est le même utilisateur ou premier lancement, on sauvegarde l'userId
            if (QuestHelper.getQuestUserId(context).isEmpty()) {
                QuestHelper.setQuestUserId(context, userId)
                Log.d("QuestViewModel", "Premier lancement - userId sauvegardé: $userId")
            }
            return false
        }
    }

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
        // Vérifier si la génération initiale a déjà été lancée cette session
        if (QuestHelper.hasInitialGenerationBeenDone()) {
            Log.d("QuestViewModel", "Génération initiale déjà effectuée cette session, ignorée")
            _isLoading.value = false
            return
        }

        // Marquer la génération comme démarrée
        QuestHelper.markInitialGenerationAsDone()

        viewModelScope.launch {
            val maxQuests = QuestHelper.getMaxQuests()
            val currentCounter = QuestHelper.getQuestCounter(context)
            if (currentCounter >= maxQuests) {
                Log.d(
                        "QuestViewModel",
                        "Maximum de quêtes déjà atteint ($currentCounter/$maxQuests)"
                )
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

            // Compteur d'échecs consécutifs - arrête après 5 échecs
            var consecutiveFailures = 0
            val maxConsecutiveFailures = 5

            while (QuestHelper.getQuestCounter(context) < maxQuests) {
                // Vérifier si on a atteint le maximum d'échecs consécutifs
                if (consecutiveFailures >= maxConsecutiveFailures) {
                    Log.w(
                            "QuestViewModel",
                            "Arrêt après $maxConsecutiveFailures échecs consécutifs. Affichage des quêtes générées."
                    )
                    break
                }

                val selectedCategory =
                        CategorySelector.selectWeightedCategory(context, userId, loadedCategories)
                                ?: break

                try {
                    val newQuest =
                            questGeneratorRepository.generateQuestForCategory(selectedCategory)
                    if (newQuest != null) {
                        // Réinitialiser le compteur d'échecs en cas de succès
                        consecutiveFailures = 0

                        QuestHelper.incrementQuestCounter(context)
                        _generationProgress.value = QuestHelper.getQuestCounter(context)
                        Log.d(
                                "QuestViewModel",
                                "Quête générée (${_generationProgress.value}/$maxQuests) : ${newQuest.nom}"
                        )

                        // Refresh quests locally
                        _quests.value = questRepository.getQuests()
                    } else {
                        consecutiveFailures++
                        Log.w(
                                "QuestViewModel",
                                "Échec génération ($consecutiveFailures/$maxConsecutiveFailures), retry dans 5s..."
                        )
                        delay(5000)
                    }
                } catch (e: Exception) {
                    consecutiveFailures++
                    Log.e(
                            "QuestViewModel",
                            "Erreur génération ($consecutiveFailures/$maxConsecutiveFailures), retry dans 5s...",
                            e
                    )
                    delay(5000)
                }
            }

            _questCounter.value = QuestHelper.getQuestCounter(context)
            _isGenerating.value = false
            _isLoading.value = false

            // Log du résultat final
            val generatedCount = _quests.value.size
            if (consecutiveFailures >= maxConsecutiveFailures) {
                Log.w(
                        "QuestViewModel",
                        "Génération arrêtée après $maxConsecutiveFailures échecs. $generatedCount quêtes affichées."
                )
            } else {
                Log.d("QuestViewModel", "Génération terminée. $generatedCount quêtes générées.")
            }
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
                    val newQuest =
                            questGeneratorRepository.generateQuestForCategory(selectedCategory)
                    if (newQuest != null) {
                        QuestHelper.incrementQuestCounter(context)
                        _questCounter.value = QuestHelper.getQuestCounter(context)
                        Log.d("QuestViewModel", "Quête générée : ${newQuest.nom}")
                        loadData(context, userId) // Refresh full data
                    } else {
                        Log.e("QuestViewModel", "Échec génération quête")
                    }
                }
            }
        }
    }

    fun clearDatabase(context: Context, userId: String) {
        viewModelScope.launch {
            questRepository.clearAllQuests()
            QuestHelper.resetQuestCounter(context)
            QuestHelper.resetInitialGenerationFlag() // Permettre une nouvelle génération
            QuestHelper.clearQuest(context, userId)
            _questCounter.value = 0
            _completedQuestsCount.value = 0
            loadData(context, userId)
        }
    }

    fun dismissDialog() {
        _showMaxQuestsDialog.value = false
    }

    /**
     * Met à jour l'état d'une quête et synchronise avec Firebase
     * @param isDone true si la quête vient d'être complétée
     * @param xpAmount quantité d'XP gagnée (positive si complétée, négative si annulée)
     */
    fun updateQuestState(isDone: Boolean, xpAmount: Int = 0) {
        viewModelScope.launch {
            if (isDone) {
                _completedQuestsCount.value += 1

                // Mettre à jour le profil Firebase si disponible
                val userId = _currentUserId.value
                if (userId.isNotEmpty() && profileRepository != null) {
                    try {
                        // Ajouter l'XP si fourni (peut être >0)
                        if (xpAmount != 0) {
                            profileRepository.updateXp(userId, xpAmount.toLong())
                            Log.d("QuestViewModel", "XP modifiée au profil Firebase: $xpAmount")
                        }

                        // Incrémenter le compteur de quêtes réalisées
                        profileRepository.incrementQuestsCompleted(userId)
                        Log.d("QuestViewModel", "Quêtes réalisées incrémentées dans Firebase")
                    } catch (e: Exception) {
                        Log.e("QuestViewModel", "Erreur mise à jour profil Firebase", e)
                    }
                }
            } else {
                _completedQuestsCount.value = (_completedQuestsCount.value - 1).coerceAtLeast(0)

                // Retirer l'XP si xpAmount négatif ou non nul
                val userId = _currentUserId.value
                if (userId.isNotEmpty() && profileRepository != null) {
                    try {
                        // Appliquer la modification d'XP (xpAmount peut être négatif)
                        if (xpAmount != 0) {
                            profileRepository.updateXp(userId, xpAmount.toLong())
                            Log.d(
                                    "QuestViewModel",
                                    "XP modifiée au profil Firebase (annulation): $xpAmount"
                            )
                        }

                        // Décrémenter le compteur de quêtes réalisées
                        profileRepository.decrementQuestsCompleted(userId)
                        Log.d("QuestViewModel", "Quêtes réalisées décrémentées dans Firebase")
                    } catch (e: Exception) {
                        Log.e(
                                "QuestViewModel",
                                "Erreur mise à jour profil Firebase lors de l'annulation",
                                e
                        )
                    }
                }
            }
        }
    }
}
