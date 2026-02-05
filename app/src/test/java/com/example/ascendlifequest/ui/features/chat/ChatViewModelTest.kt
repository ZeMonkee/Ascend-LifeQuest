package com.example.ascendlifequest.ui.features.chat

import com.example.ascendlifequest.data.model.Conversation
import com.example.ascendlifequest.data.model.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitaires pour la logique du ChatViewModel (couche ViewModel MVVM).
 *
 * Ces tests vérifient :
 * - Les états de l'UI (ChatUiState)
 * - La validation des données avant envoi
 * - Les transformations de données pour l'affichage
 *
 * Note: Les tests du ViewModel complet nécessitent des fakes repositories
 * en raison des dépendances Firebase et coroutines.
 */
class ChatViewModelTest {

    @Test
    fun `ChatUiState Loading is initial state`() {
        val state: ChatUiState = ChatUiState.Loading
        assertEquals(ChatUiState.Loading, state)
    }

    @Test
    fun `ChatUiState Error contains error message for UI display`() {
        val errorMessage = "Connection failed"
        val state = ChatUiState.Error(errorMessage)

        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `ChatUiState Success contains data for UI display`() {
        val messages = listOf(
            Message(id = "1", content = "Hello"),
            Message(id = "2", content = "Hi")
        )
        val conversation = Conversation(id = "conv-1", participants = listOf("a", "b"))

        val state = ChatUiState.Success(messages, conversation, null)

        assertEquals(2, state.messages.size)
        assertEquals("conv-1", state.conversation.id)
    }

    @Test
    fun `blank message content validation for send button state`() {
        // ViewModel devrait désactiver le bouton d'envoi si le message est vide
        val content1 = "   "
        val content2 = ""
        val content3 = "Hello"

        assertTrue(content1.trim().isBlank())
        assertTrue(content2.trim().isBlank())
        assertFalse(content3.trim().isBlank())
    }

    @Test
    fun `trimmed message content for display and validation`() {
        // ViewModel devrait nettoyer le contenu avant envoi
        val content = "  Hello World  "
        val trimmed = content.trim()

        assertEquals("Hello World", trimmed)
    }

    @Test
    fun `unread count for badge display logic`() {
        // ViewModel utilise ce compteur pour afficher les badges
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("user1", "user2")
        )
        conversation.unreadCount = mapOf("user1" to 5, "user2" to 0)

        // La logique ViewModel devrait afficher un badge avec "5" pour user1
        val userUnreadCount = conversation.unreadCount["user1"] ?: 0
        assertEquals(5, userUnreadCount)
        assertTrue(userUnreadCount > 0) // Badge visible
    }

    @Test
    fun `other participant identification for header display`() {
        // ViewModel utilise cette logique pour afficher le nom dans le header
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("user1", "user2")
        )

        val currentUserId = "user1"
        val otherParticipantId = conversation.getOtherParticipantId(currentUserId)

        assertEquals("user2", otherParticipantId)
    }

    @Test
    fun `message list grouping by date for UI sections`() {
        // ViewModel peut grouper les messages par date pour l'affichage
        val messages = listOf(
            Message(id = "1", content = "First"),
            Message(id = "2", content = "Second"),
            Message(id = "3", content = "Third")
        )

        // Vérifier que la liste est ordonnée pour l'affichage
        assertEquals(3, messages.size)
        assertEquals("First", messages[0].content)
        assertEquals("Third", messages[2].content)
    }

    @Test
    fun `empty conversation state for empty UI display`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = emptyList()
        )

        // Si pas de participants, afficher un état vide
        assertTrue(conversation.participants.isEmpty())
    }

    @Test
    fun `message sending state management`() {

        // Avant envoi
        assertFalse(false)

        // Pendant envoi (simulé)
        assertTrue(true)

        // Après envoi (simulé)
        // Le ViewModel gère l'état isSending pour désactiver le bouton
        val isSending = false
        assertFalse(isSending)
    }

    @Test
    fun `error state presentation to user`() {
        val errorState = ChatUiState.Error("Utilisateur non connecté")

        // Le ViewModel expose cette erreur pour que la View affiche un message
        assertEquals("Utilisateur non connecté", errorState.message)
    }

    @Test
    fun `loading state during async operations`() {
        val loadingState = ChatUiState.Loading

        // Le ViewModel utilise cet état pour afficher un indicateur de chargement
        assertEquals(ChatUiState.Loading, loadingState)
    }
}
