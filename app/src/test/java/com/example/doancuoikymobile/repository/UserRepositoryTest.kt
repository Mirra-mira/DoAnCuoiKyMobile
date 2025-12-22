package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.tasks.await

class UserRepositoryTest {
    private lateinit var repository: UserRepository
    private val mockRemote: UserRemoteDataSource = mockk()

    @Before
    fun setup() {
        repository = UserRepository(mockRemote)
    }

    @Test
    fun getUserOnce_success_returnsUser() = runTest {
        val userId = "user1"
        val user = User(
            userId = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = "http://example.com/avatar.jpg",
            role = "user"
        )

        coEvery { mockRemote.getUserOnce(userId) } returns user

        val result = repository.getUserOnce(userId)

        assertNotNull(result)
        assertEquals("testuser", result!!.username)
        coVerify { mockRemote.getUserOnce(userId) }
    }

    @Test
    fun getUserOnce_notFound_returnsNull() = runTest {
        val userId = "nonexistent"

        coEvery { mockRemote.getUserOnce(userId) } returns null

        val result = repository.getUserOnce(userId)

        assertNull(result)
    }

    @Test
    fun watchUser_returnUser() = runTest {
        val userId = "user1"
        val user = User(
            userId = userId,
            username = "testuser",
            email = "test@example.com"
        )

        coEvery { mockRemote.watchUser(userId) } returns flowOf(user)

        val result = mutableListOf<User?>()
        repository.watchUser(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertNotNull(result[0])
        assertEquals("testuser", result[0]?.username)
    }

    @Test
    fun watchUser_returnNull() = runTest {
        val userId = "user999"

        coEvery { mockRemote.watchUser(userId) } returns flowOf(null)

        val result = mutableListOf<User?>()
        repository.watchUser(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertNull(result[0])
    }

    @Test
    fun upsertUser_success() = runTest {
        val user = User(
            userId = "user1",
            username = "newuser",
            email = "new@example.com"
        )

        coEvery { mockRemote.upsertUser(user) } returns Unit

        repository.upsertUser(user)

        coVerify { mockRemote.upsertUser(user) }
    }

    @Test
    fun deleteUser_success() = runTest {
        val userId = "user1"

        coEvery { mockRemote.deleteUser(userId) } returns Unit

        repository.deleteUser(userId)

        coVerify { mockRemote.deleteUser(userId) }
    }

    @Test
    fun getUserOnce_multipleUsers() = runTest {
        val userId = "user1"
        val user = User(userId = userId, username = "user1")

        coEvery { mockRemote.getUserOnce(userId) } returns user

        val result1 = repository.getUserOnce(userId)
        val result2 = repository.getUserOnce(userId)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(result1!!.username, result2!!.username)
    }

    @Test
    fun watchUser_multipleEmissions() = runTest {
        val userId = "user1"
        val user1 = User(userId = userId, username = "user1", email = "old@example.com")
        val user2 = User(userId = userId, username = "user1", email = "new@example.com")

        coEvery { mockRemote.watchUser(userId) } returns flowOf(user1, user2)

        val result = mutableListOf<User?>()
        repository.watchUser(userId).collect { result.add(it) }

        assertEquals(2, result.size)
        assertEquals("old@example.com", result[0]?.email)
        assertEquals("new@example.com", result[1]?.email)
    }

    @Test
    fun upsertUser_updateExisting() = runTest {
        val user = User(
            userId = "user1",
            username = "updateduser",
            email = "updated@example.com"
        )

        coEvery { mockRemote.upsertUser(user) } returns Unit

        repository.upsertUser(user)
        repository.upsertUser(user.copy(username = "newname"))

        coVerify(exactly = 2) { mockRemote.upsertUser(any()) }
    }

    @Test
    fun deleteUser_multiple() = runTest {
        val userIds = listOf("user1", "user2", "user3")

        coEvery { mockRemote.deleteUser(any()) } returns Unit

        userIds.forEach { userId ->
            repository.deleteUser(userId)
        }

        coVerify(exactly = 3) { mockRemote.deleteUser(any()) }
    }

    @Test
    fun getUserOnce_emptyId_returnsNull() = runTest {
        coEvery { mockRemote.getUserOnce("") } returns null

        val result = repository.getUserOnce("")

        assertNull(result)
    }
}
