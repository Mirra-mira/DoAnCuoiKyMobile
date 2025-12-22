package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.PlaylistSong
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import com.google.android.gms.tasks.Task
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import junit.framework.TestCase.assertFalse

class PlaylistSongDataSourceTest {
    private lateinit var source: PlaylistSongDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = PlaylistSongDataSource(mockFirestore)
    }

    @Test
    fun addSongToPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val orderIndex = 0
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { set(any<PlaylistSong>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.addSongToPlaylist(playlistId, songId, orderIndex)

        coVerify { mockFirestore.collection("playlist_songs").document("${playlistId}_$songId").set(any<PlaylistSong>()) }
    }

    @Test
    fun removeSongFromPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.removeSongFromPlaylist(playlistId, songId)

        coVerify { mockFirestore.collection("playlist_songs").document("${playlistId}_$songId").delete() }
    }

    @Test
    fun watchPlaylistSongs_returnsList() = runTest {
        val playlistId = "pl1"
        val songs = listOf(
            PlaylistSong(playlistId = playlistId, songId = "song1", orderIndex = 0),
            PlaylistSong(playlistId = playlistId, songId = "song2", orderIndex = 1)
        )

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { whereEqualTo("playlistId", playlistId) } returns mockk {
                every { orderBy("orderIndex", Query.Direction.ASCENDING) } returns mockk {
                    every { addSnapshotListener(any()) } answers {
                        val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                        val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                            every { documents } returns songs.map { ps ->
                                mockk<DocumentSnapshot> {
                                    every { toObject(PlaylistSong::class.java) } returns ps
                                }
                            }
                        }
                        callback.onEvent(mockSnapshot, null)
                        mockk()
                    }
                }
            }
        }

        val result = source.watchPlaylistSongs(playlistId).first()

        assertEquals(2, result.size)
    }

    @Test
    fun updateSongOrderInPlaylist_success() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val newOrderIndex = 5
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { update("orderIndex", newOrderIndex) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.updateSongOrderInPlaylist(playlistId, songId, newOrderIndex)

        coVerify { mockFirestore.collection("playlist_songs").document("${playlistId}_$songId").update("orderIndex", newOrderIndex) }
    }

    @Test
    fun isSongInPlaylist_true() = runTest {
        val playlistId = "pl1"
        val songId = "song1"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.isSongInPlaylist(playlistId, songId)

        assertTrue(result)
    }

    @Test
    fun isSongInPlaylist_false() = runTest {
        val playlistId = "pl1"
        val songId = "song999"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns false
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.isSongInPlaylist(playlistId, songId)

        assertFalse(result)
    }

    @Test
    fun addSongToPlaylist_multipleCall() = runTest {
        val playlistId = "pl1"
        val songIds = listOf("song1", "song2", "song3")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { set(any<PlaylistSong>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        songIds.forEachIndexed { index, songId ->
            source.addSongToPlaylist(playlistId, songId, index)
        }

        coVerify(exactly = 3) { mockFirestore.collection("playlist_songs").document(any()).set(any<PlaylistSong>()) }
    }

    @Test
    fun watchPlaylistSongs_emptyList() = runTest {
        val playlistId = "pl_empty"

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { whereEqualTo("playlistId", playlistId) } returns mockk {
                every { orderBy("orderIndex", Query.Direction.ASCENDING) } returns mockk {
                    every { addSnapshotListener(any()) } answers {
                        val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                        val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                            every { documents } returns emptyList()
                        }
                        callback.onEvent(mockSnapshot, null)
                        mockk()
                    }
                }
            }
        }

        val result = source.watchPlaylistSongs(playlistId).first()

        assertEquals(0, result.size)
    }

    @Test
    fun updateSongOrderInPlaylist_multiple() = runTest {
        val playlistId = "pl1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { update(any<String>(), any()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.updateSongOrderInPlaylist(playlistId, "song1", 0)
        source.updateSongOrderInPlaylist(playlistId, "song2", 1)

        coVerify(exactly = 2) { mockFirestore.collection("playlist_songs").document(any()).update(any<String>(), any()) }
    }

    @Test
    fun removeSongFromPlaylist_multiple() = runTest {
        val playlistId = "pl1"
        val songIds = listOf("song1", "song2")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        songIds.forEach { songId ->
            source.removeSongFromPlaylist(playlistId, songId)
        }

        coVerify(exactly = 2) { mockFirestore.collection("playlist_songs").document(any()).delete() }
    }

    @Test
    fun isSongInPlaylist_exception() = runTest {
        val playlistId = "pl1"
        val songId = "song1"

        every { mockFirestore.collection("playlist_songs") } returns mockk {
            every { document("${playlistId}_$songId") } returns mockk {
                coEvery { get() } throws Exception("Error")
            }
        }

        try {
            source.isSongInPlaylist(playlistId, songId)
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Error") == true)
        }
    }
}
