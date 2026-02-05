package com.example.ascendlifequest.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageTest {

    @Test
    fun `default constructor creates valid message`() {
        val message = Message()

        assertEquals("", message.id)
        assertEquals("", message.conversationId)
        assertEquals("", message.senderId)
        assertEquals("", message.receiverId)
        assertEquals("", message.content)
        assertFalse(message.isRead)
        assertEquals("text", message.type)
    }

    @Test
    fun `constructor with parameters sets values correctly`() {
        val message = Message(
            id = "msg-123",
            conversationId = "conv-456",
            senderId = "sender-user",
            receiverId = "receiver-user",
            content = "Hello, World!"
        )

        assertEquals("msg-123", message.id)
        assertEquals("conv-456", message.conversationId)
        assertEquals("sender-user", message.senderId)
        assertEquals("receiver-user", message.receiverId)
        assertEquals("Hello, World!", message.content)
    }

    @Test
    fun `isRead defaults to false`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Test"
        )

        assertFalse(message.isRead)
    }

    @Test
    fun `isRead can be set to true`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Test"
        )

        message.isRead = true

        assertTrue(message.isRead)
    }

    @Test
    fun `type defaults to text`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Test"
        )

        assertEquals("text", message.type)
    }

    @Test
    fun `type can be changed`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Test"
        )

        message.type = "image"

        assertEquals("image", message.type)
    }

    @Test
    fun `message content can be modified`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Original content"
        )

        message.content = "Updated content"

        assertEquals("Updated content", message.content)
    }

    @Test
    fun `timestamp is set by default`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = "Test"
        )

        assertTrue(message.timestamp.seconds > 0)
    }

    @Test
    fun `message with empty content is valid`() {
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = ""
        )

        assertEquals("", message.content)
    }

    @Test
    fun `message with long content is handled`() {
        val longContent = "A".repeat(1000)
        val message = Message(
            id = "m1",
            conversationId = "c1",
            senderId = "s1",
            receiverId = "r1",
            content = longContent
        )

        assertEquals(1000, message.content.length)
    }
}
