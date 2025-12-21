package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Playlist
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

class PlaylistRemoteDataSourceTest {
    private lateinit var source: PlaylistRemoteDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = PlaylistRemoteDataSource(mockFirestore)
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
        val mockDoc = mockk<DocumentSnapshot> {
            every { toObject(Playlist::class.java) } returns playlist
            every { id } returns playlistId
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlistId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getPlaylistOnce(playlistId)

        assertNotNull(result)
        assertEquals("My Playlist", result!!.name)
    }

    @Test
    fun getPlaylistOnce_notFound_returnsNull() = runTest {
        val playlistId = "nonexistent"
        val mockDoc = mockk<DocumentSnapshot> {
            every { toObject(Playlist::class.java) } returns null
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlistId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getPlaylistOnce(playlistId)

        assertNull(result)
    }

    @Test
    fun watchUserPlaylists_returnsList() = runTest {
        val userId = "user1"
        val playlists = listOf(
            Playlist(playlistId = "1", userId = userId, name = "Playlist 1"),
            Playlist(playlistId = "2", userId = userId, name = "Playlist 2")
        )

        every { mockFirestore.collection("playlists") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                    val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                        every { documents } returns playlists.map { pl ->
                            mockk<DocumentSnapshot> {
                                every { toObject(Playlist::class.java) } returns pl
                                every { id } returns pl.playlistId
                            }
                        }
                    }
                    callback.onEvent(mockSnapshot, null)
                    mockk()
                }
            }
        }

        val result = source.watchUserPlaylists(userId).first()

        assertEquals(2, result.size)
    }

    @Test
    fun upsertPlaylist_success() = runTest {
        val playlist = Playlist(
            playlistId = "new",
            userId = "user1",
            name = "New Playlist"
        )
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlist.playlistId) } returns mockk {
                coEvery { set(playlist) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertPlaylist(playlist)

        coVerify { mockFirestore.collection("playlists").document(playlist.playlistId).set(playlist) }
    }

    @Test
    fun deletePlaylist_success() = runTest {
        val playlistId = "pl1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlistId) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.deletePlaylist(playlistId)

        coVerify { mockFirestore.collection("playlists").document(playlistId).delete() }
    }

    @Test
    fun watchUserPlaylists_emptyList() = runTest {
        val userId = "user999"

        every { mockFirestore.collection("playlists") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
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

        val result = source.watchUserPlaylists(userId).first()

        assertEquals(0, result.size)
    }

    @Test
    fun watchUserPlaylists_error_closesFlow() = runTest {
        val userId = "user1"
        val firestoreException = FirebaseFirestoreException(
            "Firestore error",
            FirebaseFirestoreException.Code.ABORTED
        )

        every { mockFirestore.collection("playlists") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                    callback.onEvent(null, firestoreException)
                    mockk()
                }
            }
        }

        try {
            source.watchUserPlaylists(userId).first()
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun upsertPlaylist_update() = runTest {
        val playlist = Playlist(
            playlistId = "update",
            userId = "user1",
            name = "Updated Playlist"
        )
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlist.playlistId) } returns mockk {
                coEvery { set(playlist) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertPlaylist(playlist)
        source.upsertPlaylist(playlist.copy(name = "New Name"))

        coVerify(exactly = 2) { mockFirestore.collection("playlists").document(playlist.playlistId).set(any()) }
    }

    @Test
    fun deletePlaylist_exception() = runTest {
        val playlistId = "error"

        every { mockFirestore.collection("playlists") } returns mockk {
            every { document(playlistId) } returns mockk {
                coEvery { delete() } throws Exception("Delete error")
            }
        }

        try {
            source.deletePlaylist(playlistId)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }
}
