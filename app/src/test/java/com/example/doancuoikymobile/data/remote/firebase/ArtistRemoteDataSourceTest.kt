package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.Artist
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

class ArtistRemoteDataSourceTest {
    private lateinit var source: ArtistRemoteDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        // Thêm stub collection trước
        every { mockFirestore.collection("artists") } returns mockk(relaxed = true)

        source = ArtistRemoteDataSource(mockFirestore)
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
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { toObject(Artist::class.java) } returns artist
            every { id } returns artistId
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("artists") } returns mockk {
            every { document(artistId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getArtistOnce(artistId)

        assertNotNull(result)
        assertEquals("Test Artist", result!!.name)
    }

    @Test
    fun getArtistOnce_notFound_returnsNull() = runTest {
        val artistId = "nonexistent"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns false
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("artists") } returns mockk {
            every { document(artistId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getArtistOnce(artistId)

        assertNull(result)
    }

    @Test
    fun watchAll_returnsList() = runTest {
        val artists = listOf(
            Artist(artistId = "1", name = "Artist 1", searchKeywords = listOf("artist", "1")),
            Artist(artistId = "2", name = "Artist 2", searchKeywords = listOf("artist", "2"))
        )

        every { mockFirestore.collection("artists") } returns mockk {
            every { addSnapshotListener(any()) } answers {
                val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                    every { documents } returns artists.map { artist ->
                        mockk<DocumentSnapshot> {
                            every { toObject(Artist::class.java) } returns artist
                            every { id } returns artist.artistId
                        }
                    }
                }
                callback.onEvent(mockSnapshot, null)
                mockk()
            }
        }

        val result = source.watchAll().first()

        assertEquals(2, result.size)
    }

    @Test
    fun searchArtists_success_returnsFlow() = runTest {
        val query = "rock"
        val artists = listOf(
            Artist(artistId = "1", name = "Rock Band", searchKeywords = listOf("rock", "band"))
        )

        every { mockFirestore.collection("artists") } returns mockk {
            every { whereArrayContains("searchKeywords", query.lowercase()) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                    val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                        every { documents } returns artists.map { artist ->
                            mockk<DocumentSnapshot> {
                                every { toObject(Artist::class.java) } returns artist
                                every { id } returns artist.artistId
                            }
                        }
                    }
                    callback.onEvent(mockSnapshot, null)
                    mockk()
                }
            }
        }

        val result = source.searchArtists(query).first()

        assertEquals(1, result.size)
        assertEquals("Rock Band", result[0].name)
    }

    @Test
    fun upsertArtist_success() = runTest {
        val artist = Artist(
            artistId = "new",
            name = "New Artist",
            searchKeywords = listOf("new", "artist")
        )
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("artists") } returns mockk {
            every { document(artist.artistId) } returns mockk {
                coEvery { set(artist) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertArtist(artist)

        coVerify { mockFirestore.collection("artists").document(artist.artistId).set(artist) }
    }

    @Test
    fun deleteArtist_success() = runTest {
        val artistId = "delete1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("artists") } returns mockk {
            every { document(artistId) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.deleteArtist(artistId)

        coVerify { mockFirestore.collection("artists").document(artistId).delete() }
    }

    @Test
    fun watchAll_emptyList() = runTest {
        every { mockFirestore.collection("artists") } returns mockk {
            every { addSnapshotListener(any()) } answers {
                val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                    every { documents } returns emptyList()
                }
                callback.onEvent(mockSnapshot, null)
                mockk()
            }
        }

        val result = source.watchAll().first()

        assertEquals(0, result.size)
    }

    @Test
    fun searchArtists_noResults() = runTest {
        val query = "nonexistent"

        every { mockFirestore.collection("artists") } returns mockk {
            every { whereArrayContains("searchKeywords", query.lowercase()) } returns mockk {
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

        val result = source.searchArtists(query).first()

        assertEquals(0, result.size)
    }

    @Test
    fun getArtistOnce_multipleIds() = runTest {
        val artistIds = listOf("artist1", "artist2")
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
            every { toObject(Artist::class.java) } returns Artist(artistId = "artist1", name = "Test")
            every { id } returns "artist1"
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("artists") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        artistIds.forEach { id ->
            source.getArtistOnce(id)
        }

        coVerify(atLeast = 1) { mockTask.await() }
    }
}
