package com.example.ascendlifequest.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendshipTest {

    @Test
    fun `default constructor creates valid friendship`() {
        val friendship = Friendship()

        assertEquals("", friendship.id)
        assertEquals("", friendship.userId)
        assertEquals("", friendship.friendId)
        assertEquals("accepted", friendship.status)
    }

    @Test
    fun `constructor with parameters sets values correctly`() {
        val friendship = Friendship(
            id = "friendship-123",
            userId = "user-1",
            friendId = "user-2"
        )

        assertEquals("friendship-123", friendship.id)
        assertEquals("user-1", friendship.userId)
        assertEquals("user-2", friendship.friendId)
    }

    @Test
    fun `status can be set to pending`() {
        val friendship = Friendship(
            id = "f1",
            userId = "u1",
            friendId = "u2"
        ).apply { status = Friendship.STATUS_PENDING }

        assertEquals(Friendship.STATUS_PENDING, friendship.status)
    }

    @Test
    fun `status can be set to accepted`() {
        val friendship = Friendship(
            id = "f1",
            userId = "u1",
            friendId = "u2"
        ).apply { status = Friendship.STATUS_ACCEPTED }

        assertEquals(Friendship.STATUS_ACCEPTED, friendship.status)
    }

    @Test
    fun `status can be set to declined`() {
        val friendship = Friendship(
            id = "f1",
            userId = "u1",
            friendId = "u2"
        ).apply { status = Friendship.STATUS_DECLINED }

        assertEquals(Friendship.STATUS_DECLINED, friendship.status)
    }

    @Test
    fun `companion object constants are correct`() {
        assertEquals("pending", Friendship.STATUS_PENDING)
        assertEquals("accepted", Friendship.STATUS_ACCEPTED)
        assertEquals("declined", Friendship.STATUS_DECLINED)
    }

    @Test
    fun `createdAt is set by default`() {
        val friendship = Friendship(
            id = "f1",
            userId = "u1",
            friendId = "u2"
        )

        // Timestamp should not be null
        assertTrue(friendship.createdAt.seconds > 0)
    }
}
