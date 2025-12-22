package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.model.PlaylistSong
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
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertFalse

class PlaylistRepositoryTest {
    private lateinit var repository: PlaylistRepository
    private val mockPlaylistRemote: PlaylistRemoteDataSource = mockk()
    private val mockPlaylistSongRemote: PlaylistSongDataSource = mockk()

    @Before
    fun setup() {
        repository = PlaylistRepository(mockPlaylistRemote, mockPlaylistSongRemote)
    }

    @Test
    fun getPlaylistOnce_success_returnsPlaylist() = runTest {
        val playlistId = "pl1"
        val playlist = Playlist(
            playlistId = playlistId,
            userId = "user1",
            name = "My Playlist",
            createdAt = System.currentTimeMillis()
        )

        coEvery { mockPlaylistRemote.getPlaylistOnce(playlistId) } returns playlist

        val result = repository.getPlaylistOnce(playlistId)

        assertNotNull(result)
        assertEquals("My Playlist", result!!.name)
        coVerify { mockPlaylistRemote.getPlaylistOnce(playlistId) }
    }

    @Test
    fun getPlaylistOnce_notFound_returnsNull() = runTest {
        val playlistId = "nonexistent"

        coEvery { mockPlaylistRemote.getPlaylistOnce(playlistId) } returns null

        val result = repository.getPlaylistOnce(playlistId)

        assertNull(result)
    }

    @Test
    fun watchUserPlaylists_returnsList() = runTest {
        val userId = "user1"
        val playlists = listOf(
            Playlist(playlistId = "1", userId = userId, name = "Playlist 1"),
            Playlist(playlistId = "2", userId = userId, name = "Playlist 2")
        )

        coEvery { mockPlaylistRemote.watchUserPlaylists(userId) } returns flowOf(playlists)

        val result = mutableListOf<List<Playlist>>()
        repository.watchUserPlaylists(userId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
    }

    @Test
    fun upsertPlaylist_success() = runTest {
        val playlist = Playlist(
            playlistId = "new",
            userId = "user1",
            name = "New Playlist"
        )

        coEvery { mockPlaylistRemote.upsertPlaylist(playlist) } returns Unit

        repository.upsertPlaylist(playlist)

        coVerify { mockPlaylistRemote.upsertPlaylist(playlist) }
    }

    @Test
    fun deletePlaylist_success() = runTest {
        val playlistId = "pl1"

        coEvery { mockPlaylistRemote.deletePlaylist(playlistId) } returns Unit

        repository.deletePlaylist(playlistId)

        coVerify { mockPlaylistRemote.deletePlaylist(playlistId) }
    }

    @Test
    fun addSongToPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val orderIndex = 0

        coEvery { mockPlaylistSongRemote.addSongToPlaylist(playlistId, songId, orderIndex) } returns Unit

        repository.addSongToPlaylist(playlistId, songId, orderIndex)

        coVerify { mockPlaylistSongRemote.addSongToPlaylist(playlistId, songId, orderIndex) }
    }

    @Test
    fun removeSongFromPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"

        coEvery { mockPlaylistSongRemote.removeSongFromPlaylist(playlistId, songId) } returns Unit

        repository.removeSongFromPlaylist(playlistId, songId)

        coVerify { mockPlaylistSongRemote.removeSongFromPlaylist(playlistId, songId) }
    }

    @Test
    fun watchPlaylistSongs_returnsList() = runTest {
        val playlistId = "pl1"
        val songs = listOf(
            PlaylistSong(playlistId = playlistId, songId = "song1", orderIndex = 0),
            PlaylistSong(playlistId = playlistId, songId = "song2", orderIndex = 1)
        )

        coEvery { mockPlaylistSongRemote.watchPlaylistSongs(playlistId) } returns flowOf(songs)

        val result = mutableListOf<List<PlaylistSong>>()
        repository.watchPlaylistSongs(playlistId).collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
    }

    @Test
    fun updateSongOrderInPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val newIndex = 5

        coEvery { mockPlaylistSongRemote.updateSongOrderInPlaylist(playlistId, songId, newIndex) } returns Unit

        repository.updateSongOrderInPlaylist(playlistId, songId, newIndex)

        coVerify { mockPlaylistSongRemote.updateSongOrderInPlaylist(playlistId, songId, newIndex) }
    }

    @Test
    fun isSongInPlaylist_true() = runTest {
        val playlistId = "pl1"
        val songId = "song1"

        coEvery { mockPlaylistSongRemote.isSongInPlaylist(playlistId, songId) } returns true

        val result = repository.isSongInPlaylist(playlistId, songId)

        assertTrue(result)
    }

    @Test
    fun isSongInPlaylist_false() = runTest {
        val playlistId = "pl1"
        val songId = "song999"

        coEvery { mockPlaylistSongRemote.isSongInPlaylist(playlistId, songId) } returns false

        val result = repository.isSongInPlaylist(playlistId, songId)

        assertFalse(result)
    }

    @Test
    fun addSongToPlaylist_defaultOrderIndex() = runTest {
        val playlistId = "pl1"
        val songId = "song1"

        coEvery { mockPlaylistSongRemote.addSongToPlaylist(playlistId, songId, 0) } returns Unit

        repository.addSongToPlaylist(playlistId, songId)

        coVerify { mockPlaylistSongRemote.addSongToPlaylist(playlistId, songId, 0) }
    }
}
