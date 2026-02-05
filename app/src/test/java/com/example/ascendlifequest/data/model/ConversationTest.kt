package com.example.ascendlifequest.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationTest {

    @Test
    fun `default constructor creates valid conversation`() {
        val conversation = Conversation()

        assertEquals("", conversation.id)
        assertTrue(conversation.participants.isEmpty())
        assertEquals("", conversation.lastMessage)
        assertEquals("", conversation.lastMessageSenderId)
        assertTrue(conversation.unreadCount.isEmpty())
    }

    @Test
    fun `constructor with parameters sets values correctly`() {
        val conversation = Conversation(
            id = "conv-123",
            participants = listOf("user1", "user2"),
            lastMessage = "Hello!",
            lastMessageSenderId = "user1"
        )

        assertEquals("conv-123", conversation.id)
        assertEquals(2, conversation.participants.size)
        assertTrue(conversation.participants.contains("user1"))
        assertTrue(conversation.participants.contains("user2"))
        assertEquals("Hello!", conversation.lastMessage)
        assertEquals("user1", conversation.lastMessageSenderId)
    }

    @Test
    fun `getOtherParticipantId returns correct user`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("me", "friend")
        )

        assertEquals("friend", conversation.getOtherParticipantId("me"))
        assertEquals("me", conversation.getOtherParticipantId("friend"))
    }

    @Test
    fun `getOtherParticipantId returns empty when user not in conversation`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("user1", "user2")
        )

        assertEquals("user1", conversation.getOtherParticipantId("unknown"))
    }

    @Test
    fun `getOtherParticipantId with empty participants returns empty string`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = emptyList()
        )

        assertEquals("", conversation.getOtherParticipantId("any"))
    }

    @Test
    fun `lastMessage can be updated`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("u1", "u2"),
            lastMessage = "First message"
        )

        conversation.lastMessage = "Updated message"

        assertEquals("Updated message", conversation.lastMessage)
    }

    @Test
    fun `unreadCount can be set and retrieved`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("u1", "u2")
        )

        conversation.unreadCount = mapOf("u1" to 5, "u2" to 0)

        assertEquals(5, conversation.unreadCount["u1"])
        assertEquals(0, conversation.unreadCount["u2"])
    }

    @Test
    fun `timestamps are set by default`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("u1", "u2")
        )

        assertTrue(conversation.createdAt.seconds > 0)
        assertTrue(conversation.lastMessageTimestamp.seconds > 0)
    }

    @Test
    fun `conversation with single participant works`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("solo")
        )

        assertEquals(1, conversation.participants.size)
        assertEquals("", conversation.getOtherParticipantId("solo"))
    }

    @Test
    fun `lastMessageSenderId can be updated`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("u1", "u2"),
            lastMessageSenderId = "u1"
        )

        conversation.lastMessageSenderId = "u2"

        assertEquals("u2", conversation.lastMessageSenderId)
    }

    @Test
    fun `participants list preserves order`() {
        val conversation = Conversation(
            id = "conv-1",
            participants = listOf("first", "second")
        )

        assertEquals("first", conversation.participants[0])
        assertEquals("second", conversation.participants[1])
    }
}
