package com.example.ascendlifequest.ui.features.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ascendlifequest.data.auth.AuthRepository
import com.example.ascendlifequest.data.auth.AuthRepositoryImpl
import com.example.ascendlifequest.data.model.Conversation
import com.example.ascendlifequest.data.model.Message
import com.example.ascendlifequest.data.model.UserProfile
import com.example.ascendlifequest.data.remote.AuthService
import com.example.ascendlifequest.data.repository.FriendRepository
import com.example.ascendlifequest.data.repository.FriendRepositoryImpl
import com.example.ascendlifequest.data.repository.MessageRepository
import com.example.ascendlifequest.data.repository.MessageRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
            val messages: List<Message>,
            val conversation: Conversation,
            val otherUser: UserProfile?
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

/**
 * ViewModel managing real-time chat between users. Handles message sending, receiving, and
 * conversation state.
 *
 * @property authRepository Repository for authentication state
 * @property messageRepository Repository for message operations
 * @property friendRepository Repository for user profile retrieval
 */
class ChatViewModel(
        private val authRepository: AuthRepository = AuthRepositoryImpl(AuthService()),
        private val messageRepository: MessageRepository = MessageRepositoryImpl(),
        private val friendRepository: FriendRepository = FriendRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private var currentConversationId: String = ""
    private var otherUserId: String = ""

    init {
        _currentUserId.value = authRepository.getCurrentUserId()
    }

    fun loadConversation(friendId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading

            val userId = authRepository.getCurrentUserId()
            if (userId.isEmpty()) {
                _uiState.value = ChatUiState.Error("Utilisateur non connecté")
                return@launch
            }

            _currentUserId.value = userId
            otherUserId = friendId

            // Récupérer ou créer la conversation
            val conversationResult = messageRepository.getOrCreateConversation(userId, friendId)

            conversationResult.fold(
                    onSuccess = { conversation ->
                        currentConversationId = conversation.id

                        // Récupérer le profil de l'autre utilisateur
                        val otherUserResult = friendRepository.getProfileById(friendId)
                        val otherUser = otherUserResult.getOrNull()

                        // Récupérer les messages
                        val messagesResult = messageRepository.getMessages(conversation.id)

                        messagesResult.fold(
                                onSuccess = { messages ->
                                    _uiState.value =
                                            ChatUiState.Success(
                                                    messages = messages,
                                                    conversation = conversation,
                                                    otherUser = otherUser
                                            )

                                    // Marquer les messages comme lus
                                    messageRepository.markMessagesAsRead(conversation.id, userId)
                                },
                                onFailure = { error ->
                                    _uiState.value =
                                            ChatUiState.Error(
                                                    error.message ?: "Erreur chargement messages"
                                            )
                                }
                        )

                        // Observer les nouveaux messages en temps réel
                        observeMessages(conversation.id)
                    },
                    onFailure = { error ->
                        _uiState.value =
                                ChatUiState.Error(error.message ?: "Erreur chargement conversation")
                    }
            )
        }
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            messageRepository.observeMessages(conversationId).collect { messages ->
                val currentState = _uiState.value
                if (currentState is ChatUiState.Success) {
                    _uiState.value = currentState.copy(messages = messages)

                    // Marquer les nouveaux messages comme lus
                    val userId = _currentUserId.value
                    if (userId.isNotEmpty()) {
                        messageRepository.markMessagesAsRead(conversationId, userId)
                    }
                }
            }
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val content = _messageText.value.trim()
        if (content.isBlank() || currentConversationId.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true

            val userId = _currentUserId.value
            if (userId.isEmpty()) {
                _isSending.value = false
                return@launch
            }

            val result =
                    messageRepository.sendMessage(
                            conversationId = currentConversationId,
                            senderId = userId,
                            receiverId = otherUserId,
                            content = content
                    )

            result.fold(
                    onSuccess = { message ->
                        Log.d(TAG, "Message envoyé: ${message.id}")
                        _messageText.value = ""
                    },
                    onFailure = { error -> Log.e(TAG, "Erreur envoi message", error) }
            )

            _isSending.value = false
        }
    }
}
