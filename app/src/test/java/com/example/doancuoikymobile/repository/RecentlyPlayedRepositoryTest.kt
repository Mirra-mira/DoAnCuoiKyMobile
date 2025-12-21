package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.model.RecentlyPlayed
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

class RecentlyPlayedRepositoryTest {
    private lateinit var repository: RecentlyPlayedRepository
    private val mockRemote: RecentlyPlayedDataSource = mockk()

    @Before
    fun setup() {
        repository = RecentlyPlayedRepository(mockRemote)
    }

    @Test
    fun addPlayed_success() = runTest {
        val record = RecentlyPlayed(
            userId = "user1",
            songId = "song1",
            playedAt = System.currentTimeMillis()
        )

        coEvery { mockRemote.addRecord(record) } returns Unit

        repository.addPlayed(record)

        coVerify { mockRemote.addRecord(record) }
    }

    @Test
    fun addPlayed_multiple() = runTest {
        val records = listOf(
            RecentlyPlayed(userId = "user1", songId = "song1"),
            RecentlyPlayed(userId = "user1", songId = "song2"),
            RecentlyPlayed(userId = "user1", songId = "song3")
        )

        coEvery { mockRemote.addRecord(any()) } returns Unit

        records.forEach { record ->
            repository.addPlayed(record)
        }

        coVerify(exactly = 3) { mockRemote.addRecord(any()) }
    }

    @Test
    fun watchUserRecent_returnsList() = runTest {
        val userId = "user1"
        val records = listOf(
            RecentlyPlayed(userId = userId, songId = "song1", playedAt = 1000),
            RecentlyPlayed(userId = userId, songId = "song2", playedAt = 2000)
        )

        coEvery { mockRemote.watchUserRecent(userId, 50) } returns flowOf(records)

        val result = mutableListOf<List<RecentlyPlayed>>()
        repository.watchUserRecent(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        coVerify { mockRemote.watchUserRecent(userId, 50) }
    }

    @Test
    fun watchUserRecent_customLimit() = runTest {
        val userId = "user1"
        val limit = 100L
        val records = emptyList<RecentlyPlayed>()

        coEvery { mockRemote.watchUserRecent(userId, limit) } returns flowOf(records)

        val result = mutableListOf<List<RecentlyPlayed>>()
        repository.watchUserRecent(userId, limit).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(0, result[0].size)
        coVerify { mockRemote.watchUserRecent(userId, limit) }
    }

    @Test
    fun watchUserRecent_emptyList() = runTest {
        val userId = "user2"

        coEvery { mockRemote.watchUserRecent(userId, 50) } returns flowOf(emptyList())

        val result = mutableListOf<List<RecentlyPlayed>>()
        repository.watchUserRecent(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(0, result[0].size)
    }

    @Test
    fun addPlayed_differentUsers() = runTest {
        val record1 = RecentlyPlayed(userId = "user1", songId = "song1")
        val record2 = RecentlyPlayed(userId = "user2", songId = "song1")

        coEvery { mockRemote.addRecord(any()) } returns Unit

        repository.addPlayed(record1)
        repository.addPlayed(record2)

        coVerify(exactly = 2) { mockRemote.addRecord(any()) }
    }

    @Test
    fun watchUserRecent_multipleEmissions() = runTest {
        val userId = "user1"
        val records1 = listOf(RecentlyPlayed(userId = userId, songId = "song1"))
        val records2 = listOf(
            RecentlyPlayed(userId = userId, songId = "song1"),
            RecentlyPlayed(userId = userId, songId = "song2")
        )

        coEvery { mockRemote.watchUserRecent(userId, 50) } returns flowOf(records1, records2)

        val result = mutableListOf<List<RecentlyPlayed>>()
        repository.watchUserRecent(userId).collect { result.add(it) }

        assertEquals(2, result.size)
        assertEquals(1, result[0].size)
        assertEquals(2, result[1].size)
    }

    @Test
    fun addPlayed_sameSongDifferentTime() = runTest {
        val record1 = RecentlyPlayed(userId = "user1", songId = "song1", playedAt = 1000)
        val record2 = RecentlyPlayed(userId = "user1", songId = "song1", playedAt = 2000)

        coEvery { mockRemote.addRecord(any()) } returns Unit

        repository.addPlayed(record1)
        repository.addPlayed(record2)

        coVerify(exactly = 2) { mockRemote.addRecord(any()) }
    }
}
