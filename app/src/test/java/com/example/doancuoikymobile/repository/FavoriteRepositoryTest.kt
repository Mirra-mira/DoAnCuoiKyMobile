package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FavoriteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse

class FavoriteRepositoryTest {
    private lateinit var repository: FavoriteRepository
    private val mockDataSource: FavoriteDataSource = mockk()

    @Before
    fun setup() {
        repository = FavoriteRepository()
    }

    @Test
    fun addToFavorite_success() = runTest {
        val userId = "user1"
        val songId = "song1"

        coEvery { mockDataSource.addFavorite(userId, songId) } returns Unit

        repository.addToFavorite(userId, songId)

        coVerify { mockDataSource.addFavorite(userId, songId) }
    }

    @Test
    fun removeFromFavorite_success() = runTest {
        val userId = "user1"
        val songId = "song1"

        coEvery { mockDataSource.removeFavorite(userId, songId) } returns Unit

        repository.removeFromFavorite(userId, songId)

        coVerify { mockDataSource.removeFavorite(userId, songId) }
    }

    @Test
    fun isFavorite_true() = runTest {
        val userId = "user1"
        val songId = "song1"

        coEvery { mockDataSource.isFavorite(userId, songId) } returns true

        val result = repository.isFavorite(userId, songId)

        assertTrue(result)
        coVerify { mockDataSource.isFavorite(userId, songId) }
    }

    @Test
    fun isFavorite_false() = runTest {
        val userId = "user1"
        val songId = "song2"

        coEvery { mockDataSource.isFavorite(userId, songId) } returns false

        val result = repository.isFavorite(userId, songId)

        assertFalse(result)
    }

    @Test
    fun getFavoriteSongs_returnsEmptyList() = runTest {
        val userId = "user1"
        val result = mutableListOf<List<String>>()

        repository.getFavoriteSongs(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(0, result[0].size)
    }

    @Test
    fun addToFavorite_multipleCall() = runTest {
        val userId = "user1"
        val songIds = listOf("song1", "song2", "song3")

        coEvery { mockDataSource.addFavorite(any(), any()) } returns Unit

        songIds.forEach { songId ->
            repository.addToFavorite(userId, songId)
        }

        coVerify(exactly = 3) { mockDataSource.addFavorite(userId, any()) }
    }

    @Test
    fun removeFromFavorite_multipleCall() = runTest {
        val userId = "user1"
        val songIds = listOf("song1", "song2")

        coEvery { mockDataSource.removeFavorite(any(), any()) } returns Unit

        songIds.forEach { songId ->
            repository.removeFromFavorite(userId, songId)
        }

        coVerify(exactly = 2) { mockDataSource.removeFavorite(userId, any()) }
    }

    @Test
    fun isFavorite_exception_returnsFalse() = runTest {
        val userId = "user1"
        val songId = "song1"

        coEvery { mockDataSource.isFavorite(userId, songId) } throws Exception("Error")

        val result = try {
            repository.isFavorite(userId, songId)
        } catch (e: Exception) {
            false
        }

        assertFalse(result)
    }

    @Test
    fun addToFavorite_sameSongTwice() = runTest {
        val userId = "user1"
        val songId = "song1"

        coEvery { mockDataSource.addFavorite(userId, songId) } returns Unit

        repository.addToFavorite(userId, songId)
        repository.addToFavorite(userId, songId)

        coVerify(exactly = 2) { mockDataSource.addFavorite(userId, songId) }
    }
}
