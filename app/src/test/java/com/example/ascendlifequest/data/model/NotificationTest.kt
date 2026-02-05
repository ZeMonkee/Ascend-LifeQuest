package com.example.ascendlifequest.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTest {

    @Test
    fun `default constructor creates valid notification`() {
        val notification = Notification()

        assertEquals("", notification.id)
        assertEquals("", notification.userId)
        assertEquals("", notification.type)
        assertEquals("", notification.message)
        assertEquals("", notification.fromUserId)
        assertEquals("", notification.fromUserPseudo)
        assertFalse(notification.read)
    }

    @Test
    fun `constructor with parameters sets values correctly`() {
        val notification = Notification(
            id = "notif-123",
            userId = "recipient-user",
            type = Notification.TYPE_FRIEND_REQUEST_DECLINED,
            message = "User declined your friend request",
            fromUserId = "decliner-user",
            fromUserPseudo = "DeclinerUser"
        )

        assertEquals("notif-123", notification.id)
        assertEquals("recipient-user", notification.userId)
        assertEquals(Notification.TYPE_FRIEND_REQUEST_DECLINED, notification.type)
        assertEquals("User declined your friend request", notification.message)
        assertEquals("decliner-user", notification.fromUserId)
        assertEquals("DeclinerUser", notification.fromUserPseudo)
        assertFalse(notification.read)
    }

    @Test
    fun `read status can be changed`() {
        val notification = Notification(
            id = "n1",
            userId = "u1"
        )

        assertFalse(notification.read)

        notification.read = true

        assertTrue(notification.read)
    }

    @Test
    fun `companion object TYPE constants are correct`() {
        assertEquals("friend_request_declined", Notification.TYPE_FRIEND_REQUEST_DECLINED)
        assertEquals("friend_request_accepted", Notification.TYPE_FRIEND_REQUEST_ACCEPTED)
    }

    @Test
    fun `notification with accepted type works correctly`() {
        val notification = Notification(
            id = "n1",
            userId = "user1",
            type = Notification.TYPE_FRIEND_REQUEST_ACCEPTED,
            message = "User accepted your friend request",
            fromUserId = "friend1",
            fromUserPseudo = "FriendUser"
        )

        assertEquals(Notification.TYPE_FRIEND_REQUEST_ACCEPTED, notification.type)
        assertTrue(notification.message.contains("accepted"))
    }

    @Test
    fun `createdAt is set by default`() {
        val notification = Notification(
            id = "n1",
            userId = "u1"
        )

        // Timestamp should not be null and have a valid time
        assertTrue(notification.createdAt.seconds > 0)
    }

    @Test
    fun `notification properties can be modified`() {
        val notification = Notification(
            id = "n1",
            userId = "u1"
        )

        notification.type = "custom_type"
        notification.message = "Custom message"
        notification.fromUserId = "sender"
        notification.fromUserPseudo = "SenderPseudo"

        assertEquals("custom_type", notification.type)
        assertEquals("Custom message", notification.message)
        assertEquals("sender", notification.fromUserId)
        assertEquals("SenderPseudo", notification.fromUserPseudo)
    }
}
