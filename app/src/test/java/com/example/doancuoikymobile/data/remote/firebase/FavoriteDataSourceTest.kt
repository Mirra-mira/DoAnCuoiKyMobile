package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.FavoriteSong
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import com.google.android.gms.tasks.Task
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await

class FavoriteDataSourceTest {
    private lateinit var source: FavoriteDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class) // mock static method
        val mockCollection = mockk<com.google.firebase.firestore.CollectionReference>(relaxed = true)
        every { mockFirestore.collection("favorite_songs") } returns mockCollection
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = FavoriteDataSource() // bây giờ mới tạo instance
    }

    @Test
    fun addFavorite_success() = runTest {
        val userId = "user1"
        val songId = "song1"
        val mockTask: Task<Void> = mockk(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { set(any<FavoriteSong>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.addFavorite(userId, songId)

        coVerify { mockFirestore.collection("favorite_songs").document("$userId-$songId").set(any<FavoriteSong>()) }
    }

    @Test
    fun removeFavorite_success() = runTest {
        val userId = "user1"
        val songId = "song1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.removeFavorite(userId, songId)

        coVerify { mockFirestore.collection("favorite_songs").document("$userId-$songId").delete() }
    }

    @Test
    fun isFavorite_true_returnTrue() = runTest {
        val userId = "user1"
        val songId = "song1"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns true
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.isFavorite(userId, songId)

        assertTrue(result)
    }

    @Test
    fun isFavorite_false_returnFalse() = runTest {
        val userId = "user1"
        val songId = "song999"
        val mockDoc = mockk<DocumentSnapshot> {
            every { exists() } returns false
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.isFavorite(userId, songId)

        assertFalse(result)
    }

    @Test
    fun isFavorite_exception_returnFalse() = runTest {
        val userId = "user1"
        val songId = "song1"

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { get() } throws Exception("Error")
            }
        }

        val result = source.isFavorite(userId, songId)

        assertFalse(result)
    }

    @Test
    fun addFavorite_multipleCall() = runTest {
        val userId = "user1"
        val songIds = listOf("song1", "song2", "song3")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { set(any<FavoriteSong>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        songIds.forEach { songId ->
            source.addFavorite(userId, songId)
        }

        coVerify(exactly = 3) { mockFirestore.collection("favorite_songs").document(any()).set(any<FavoriteSong>()) }
    }

    @Test
    fun removeFavorite_multipleCall() = runTest {
        val userId = "user1"
        val songIds = listOf("song1", "song2")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        songIds.forEach { songId ->
            source.removeFavorite(userId, songId)
        }

        coVerify(exactly = 2) { mockFirestore.collection("favorite_songs").document(any()).delete() }
    }

    @Test
    fun addFavorite_sameSongTwice() = runTest {
        val userId = "user1"
        val songId = "song1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { set(any<FavoriteSong>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.addFavorite(userId, songId)
        source.addFavorite(userId, songId)

        coVerify(exactly = 2) { mockFirestore.collection("favorite_songs").document("$userId-$songId").set(any<FavoriteSong>()) }
    }

    @Test
    fun removeFavorite_exception() = runTest {
        val userId = "user1"
        val songId = "song1"

        every { mockFirestore.collection("favorite_songs") } returns mockk {
            every { document("$userId-$songId") } returns mockk {
                coEvery { delete() } throws Exception("Delete failed")
            }
        }

        try {
            source.removeFavorite(userId, songId)
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Delete failed") == true)
        }
    }
}
