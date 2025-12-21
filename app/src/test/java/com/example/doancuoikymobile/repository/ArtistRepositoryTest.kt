package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.ArtistRemoteDataSource
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.utils.SearchKeywordGenerator
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

class ArtistRepositoryTest {
    private lateinit var repository: ArtistRepository
    private val mockRemote: ArtistRemoteDataSource = mockk()

    @Before
    fun setup() {
        repository = ArtistRepository(mockRemote)
    }

    @Test
    fun getArtistOnce_success_returnsArtist() = runTest {
        val artistId = "artist1"
        val artist = Artist(
            artistId = artistId,
            name = "Test Artist",
            pictureUrl = "http://example.com/pic.jpg",
            searchKeywords = listOf("test", "artist")
        )

        coEvery { mockRemote.getArtistOnce(artistId) } returns artist

        val result = repository.getArtistOnce(artistId)

        assertNotNull(result)
        assertEquals("Test Artist", result!!.name)
        coVerify { mockRemote.getArtistOnce(artistId) }
    }

    @Test
    fun getArtistOnce_notFound_returnsNull() = runTest {
        val artistId = "nonexistent"

        coEvery { mockRemote.getArtistOnce(artistId) } returns null

        val result = repository.getArtistOnce(artistId)

        assertNull(result)
    }

    @Test
    fun watchAll_returnsList() = runTest {
        val artists = listOf(
            Artist(artistId = "1", name = "Artist 1", searchKeywords = listOf("artist", "1")),
            Artist(artistId = "2", name = "Artist 2", searchKeywords = listOf("artist", "2"))
        )

        coEvery { mockRemote.watchAll() } returns flowOf(artists)

        val result = mutableListOf<List<Artist>>()
        repository.watchAll().collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
    }

    @Test
    fun searchArtists_success_returnsList() = runTest {
        val query = "rock"
        val artists = listOf(
            Artist(artistId = "1", name = "Rock Band", searchKeywords = listOf("rock", "band"))
        )

        coEvery { mockRemote.searchArtists(query) } returns flowOf(artists)

        val result = mutableListOf<List<Artist>>()
        repository.searchArtists(query).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals("Rock Band", result[0][0].name)
    }

    @Test
    fun upsertArtist_withoutKeywords_generatesKeywords() = runTest {
        val artist = Artist(artistId = "new", name = "New Artist", searchKeywords = emptyList())

        coEvery { mockRemote.upsertArtist(any()) } returns Unit

        repository.upsertArtist(artist)

        coVerify {
            mockRemote.upsertArtist(match {
                it.searchKeywords.isNotEmpty()
            })
        }
    }

    @Test
    fun upsertArtist_withKeywords_usesExisting() = runTest {
        val keywords = listOf("jazz", "music")
        val artist = Artist(
            artistId = "update",
            name = "Jazz Artist",
            searchKeywords = keywords
        )

        coEvery { mockRemote.upsertArtist(any()) } returns Unit

        repository.upsertArtist(artist)

        coVerify {
            mockRemote.upsertArtist(match {
                it.searchKeywords == keywords
            })
        }
    }

    @Test
    fun deleteArtist_success() = runTest {
        val artistId = "delete1"

        coEvery { mockRemote.deleteArtist(artistId) } returns Unit

        repository.deleteArtist(artistId)

        coVerify { mockRemote.deleteArtist(artistId) }
    }

    @Test
    fun getArtistOnce_empty_returnsNull() = runTest {
        coEvery { mockRemote.getArtistOnce(any()) } returns null

        val result = repository.getArtistOnce("")

        assertNull(result)
    }
}
