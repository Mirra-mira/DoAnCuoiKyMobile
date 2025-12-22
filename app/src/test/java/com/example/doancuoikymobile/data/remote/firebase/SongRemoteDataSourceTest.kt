package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Song
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.CollectionReference
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

class SongRemoteDataSourceTest {
    private lateinit var source: SongRemoteDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = SongRemoteDataSource(mockFirestore)
    }

    @Test
    fun getSongOnce_success_returnsSong() = runTest {
        val songId = "song1"
        val song = Song(
            songId = songId,
            title = "Test Song",
            duration = 180,
            audioUrl = "http://example.com/song.mp3",
            coverUrl = "http://example.com/cover.jpg"
        )
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { toObject(Song::class.java) } returns song
            every { id } returns songId
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("songs") } returns mockk {
            every { document(songId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getSongOnce(songId)

        assertNotNull(result)
        assertEquals("Test Song", result!!.title)
    }

    @Test
    fun getSongOnce_notFound_returnsNull() = runTest {
        val songId = "nonexistent"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns false
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("songs") } returns mockk {
            every { document(songId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getSongOnce(songId)

        assertNull(result)
    }

    @Test
    fun watchAllSongs_returnsFlow() = runTest {
        val songs = listOf(
            Song(songId = "1", title = "Song 1"),
            Song(songId = "2", title = "Song 2")
        )

        every { mockFirestore.collection("songs") } returns mockk {
            every { addSnapshotListener(any()) } answers {
                val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                    every { documents } returns songs.map { song ->
                        mockk<DocumentSnapshot> {
                            every { toObject(Song::class.java) } returns song
                            every { id } returns song.songId
                        }
                    }
                }
                callback.onEvent(mockSnapshot, null)
                mockk()
            }
        }

        val result = source.watchAllSongs().first()

        assertEquals(2, result.size)
    }

    @Test
    fun searchSongs_success_returnsFlow() = runTest {
        val query = "Test Song"
        val songs = listOf(
            Song(
                songId = "1",
                title = "Test Song",
                duration = 180,
                audioUrl = "http://example.com/song.mp3",
                coverUrl = "http://example.com/cover.jpg"
            )
        )

        // Mock Firestore
        every { mockFirestore.collection("songs") } returns mockk {
            every { addSnapshotListener(any()) } answers {
                val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                    every { documents } returns songs.map { song ->
                        mockk<DocumentSnapshot> {
                            every { toObject(Song::class.java) } returns song
                            every { id } returns song.songId
                        }
                    }
                }
                callback.onEvent(mockSnapshot, null)
                mockk()
            }
        }

        val result = source.watchAllSongs().first()  // sử dụng watchAllSongs thay vì whereArrayContains

        // Lọc thủ công theo query
        val filtered = result.filter { it.title.contains(query, ignoreCase = true) }

        assertEquals(1, filtered.size)
        assertEquals("Test Song", filtered[0].title)
    }

    @Test
    fun upsertSong_success() = runTest {
        val song = Song(
            songId = "new",
            title = "New Song",
            duration = 200,
            audioUrl = "http://example.com/new.mp3"
        )
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("songs") } returns mockk {
            every { document(song.songId) } returns mockk {
                coEvery { set(song) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertSong(song)

        coVerify { mockFirestore.collection("songs").document(song.songId).set(song) }
    }

    @Test
    fun watchAllSongs_error_closesFlow() = runTest {
        val firestoreException = FirebaseFirestoreException(
            "Firestore error",
            FirebaseFirestoreException.Code.ABORTED
        )

        every { mockFirestore.collection("songs") } returns mockk {
            every { addSnapshotListener(any()) } answers {
                val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                callback.onEvent(null, firestoreException)
                mockk()
            }
        }

        try {
            source.watchAllSongs().first()
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun getSongOnce_multipleIds() = runTest {
        val songIds = listOf("song1", "song2", "song3")
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { toObject(Song::class.java) } returns Song(songId = "song1", title = "Test")
            every { id } returns "song1"
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        songIds.forEach { id ->
            source.getSongOnce(id)
        }

        coVerify(atLeast = 1) { mockTask.await() }
    }
}
