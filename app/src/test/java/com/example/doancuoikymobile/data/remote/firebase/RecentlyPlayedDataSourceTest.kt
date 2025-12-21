package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.RecentlyPlayed
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals

class RecentlyPlayedDataSourceTest {
    private lateinit var source: RecentlyPlayedDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = RecentlyPlayedDataSource(mockFirestore)
    }

    @Test
    fun addRecord_success() = runTest {
        val record = RecentlyPlayed(userId = "user1", songId = "song1")
        val mockTask = mockk<Task<DocumentReference>>(relaxed = true)

        every { mockFirestore.collection("recently_played") } returns mockk {
            coEvery { add(record) } returns mockTask
        }
        coEvery { mockTask.await() } returns mockk<DocumentReference>()

        source.addRecord(record)

        coVerify { mockFirestore.collection("recently_played").add(record) }
    }

    @Test
    fun watchUserRecent_returnsList() = runTest {
        val userId = "user1"
        val records = listOf(
            RecentlyPlayed(userId = userId, songId = "song1", playedAt = 2000),
            RecentlyPlayed(userId = userId, songId = "song2", playedAt = 1000)
        )

        every { mockFirestore.collection("recently_played") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { orderBy("playedAt", Query.Direction.DESCENDING) } returns mockk {
                    every { limit(50) } returns mockk {
                        every { addSnapshotListener(any()) } answers {
                            val callback =
                                it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                            val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                                every { documents } returns records.map { record ->
                                    mockk {
                                        every { toObject(RecentlyPlayed::class.java) } returns record
                                    }
                                }
                            }
                            callback.onEvent(mockSnapshot, null)
                            mockk()
                        }
                    }
                }
            }
        }

        val result = source.watchUserRecent(userId, 50).first()

        assertEquals(2, result.size)
    }

    @Test
    fun watchUserRecent_emptyList() = runTest {
        val userId = "user999"

        every { mockFirestore.collection("recently_played") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { orderBy("playedAt", Query.Direction.DESCENDING) } returns mockk {
                    every { limit(50) } returns mockk {
                        every { addSnapshotListener(any()) } answers {
                            val callback =
                                it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                            val mockSnapshot = mockk<com.google.firebase.firestore.QuerySnapshot> {
                                every { documents } returns emptyList()
                            }
                            callback.onEvent(mockSnapshot, null)
                            mockk()
                        }
                    }
                }
            }
        }

        val result = source.watchUserRecent(userId).first()

        assertEquals(0, result.size)
    }

    @Test
    fun addRecord_multipleCall() = runTest {
        val records = listOf(
            RecentlyPlayed(userId = "user1", songId = "song1"),
            RecentlyPlayed(userId = "user1", songId = "song2"),
            RecentlyPlayed(userId = "user1", songId = "song3")
        )
        val mockTask = mockk<Task<DocumentReference>>(relaxed = true)

        every { mockFirestore.collection("recently_played") } returns mockk {
            coEvery { add(any<RecentlyPlayed>()) } returns mockTask
        }
        coEvery { mockTask.await() } returns mockk<DocumentReference>()

        records.forEach { record ->
            source.addRecord(record)
        }

        coVerify(exactly = 3) { mockFirestore.collection("recently_played").add(any<RecentlyPlayed>()) }
    }

    @Test
    fun addRecord_exception() = runTest {
        val record = RecentlyPlayed(userId = "user1", songId = "song1")

        every { mockFirestore.collection("recently_played") } returns mockk {
            coEvery { add(record) } throws Exception("Add failed")
        }

        try {
            source.addRecord(record)
        } catch (e: Exception) {
            assertEquals("Add failed", e.message)
        }
    }

    @Test
    fun watchUserRecent_error_closesFlow() = runTest {
        val userId = "user1"
        val firestoreException =
            mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        every { mockFirestore.collection("recently_played") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { orderBy("playedAt", Query.Direction.DESCENDING) } returns mockk {
                    every { limit(50) } returns mockk {
                        every { addSnapshotListener(any()) } answers {
                            val callback =
                                it.invocation.args[0] as com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>
                            callback.onEvent(null, firestoreException)
                            mockk()
                        }
                    }
                }
            }
        }

        try {
            source.watchUserRecent(userId).first()
        } catch (e: Exception) {
            assertEquals(firestoreException, e)
        }
    }
}
