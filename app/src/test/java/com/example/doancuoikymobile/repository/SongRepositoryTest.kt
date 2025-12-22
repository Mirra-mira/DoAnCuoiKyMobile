package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.api.SongApiService
import com.example.doancuoikymobile.data.remote.api.SaavnResponse
import com.example.doancuoikymobile.data.remote.api.SaavnSong
import com.example.doancuoikymobile.data.remote.api.DownloadLink
import com.example.doancuoikymobile.data.remote.api.ImageLink
import com.example.doancuoikymobile.model.Song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.tasks.await

class SongRepositoryTest {
    private lateinit var repository: SongRepository
    private val mockApiService: SongApiService = mockk()

    @Before
    fun setup() {
        repository = SongRepository()
    }

    @Test
    fun searchSongs_success_returnsListOfSongs() {
        val query = "test song"
        val saavnSongs = listOf(
            SaavnSong(
                id = "1",
                name = "Test Song",
                duration = 180,
                downloadUrl = listOf(DownloadLink(link = "http://example.com/song1.mp3")),
                image = listOf(ImageLink(link = "http://example.com/cover1.jpg")),
                primaryArtists = "Artist 1"
            )
        )
        val response = SaavnResponse(data = saavnSongs)

        coEvery { mockApiService.searchSongs(query) } returns response

        val result = runBlocking {
            repository.searchSongs(query)
        }

        assertEquals(1, result.size)
        assertEquals("1", result[0].songId)
        assertEquals("Test Song", result[0].title)
    }

    @Test
    fun searchSongs_emptyResponse_returnsEmptyList() {
        val query = "nonexistent"
        val response = SaavnResponse(data = null)

        coEvery { mockApiService.searchSongs(query) } returns response

        val result = runBlocking {
            repository.searchSongs(query)
        }

        assertEquals(0, result.size)
    }

    @Test
    fun searchSongs_exception_returnsEmptyList() {
        val query = "error"

        coEvery { mockApiService.searchSongs(query) } throws Exception("Network error")

        val result = runBlocking {
            repository.searchSongs(query)
        }

        assertEquals(0, result.size)
    }

    @Test
    fun getSongById_success_returnsSong() {
        val songId = "123"
        val saavnSong = SaavnSong(
            id = songId,
            name = "Sample Song",
            duration = 240,
            downloadUrl = listOf(DownloadLink(link = "http://example.com/song.mp3")),
            image = listOf(ImageLink(link = "http://example.com/cover.jpg")),
            primaryArtists = "Sample Artist"
        )
        val response = SaavnResponse(data = listOf(saavnSong))

        coEvery { mockApiService.getSongDetail(songId) } returns response

        val result = runBlocking {
            repository.getSongById(songId)
        }

        assertEquals(songId, result?.songId)
        assertEquals("Sample Song", result?.title)
    }

    @Test
    fun getSongById_noData_returnsNull() {
        val songId = "999"
        val response = SaavnResponse(data = emptyList())

        coEvery { mockApiService.getSongDetail(songId) } returns response

        val result = runBlocking {
            repository.getSongById(songId)
        }

        assertNull(result)
    }

    @Test
    fun getSongById_exception_returnsNull() {
        val songId = "error"

        coEvery { mockApiService.getSongDetail(songId) } throws Exception("API error")

        val result = runBlocking {
            repository.getSongById(songId)
        }

        assertNull(result)
    }

    @Test
    fun searchSongs_multipleResults_returnsAllSongs() {
        val query = "popular"
        val songs = (1..3).map {
            SaavnSong(
                id = "song$it",
                name = "Popular Song $it",
                duration = 200 + it * 10,
                downloadUrl = listOf(DownloadLink(link = "http://example.com/song$it.mp3")),
                image = listOf(ImageLink(link = "http://example.com/cover$it.jpg")),
                primaryArtists = "Artist $it"
            )
        }
        val response = SaavnResponse(data = songs)

        coEvery { mockApiService.searchSongs(query) } returns response

        val result = runBlocking {
            repository.searchSongs(query)
        }

        assertEquals(3, result.size)
    }
}

private fun <T> runBlocking(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking { block() }
}
