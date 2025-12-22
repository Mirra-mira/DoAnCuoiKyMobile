package com.example.doancuoikymobile.data.remote.firebase

import com.example.doancuoikymobile.model.User
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

class UserRemoteDataSourceTest {
    private lateinit var source: UserRemoteDataSource
    private val mockFirestore: FirebaseFirestore = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
        source = UserRemoteDataSource(mockFirestore)
    }

    @Test
    fun getUserOnce_success_returnsUser() = runTest {
        val userId = "user1"
        val user = User(
            userId = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User"
        )
        val mockDoc = mockk<DocumentSnapshot> {
            every { toObject(User::class.java) } returns user
            every { id } returns userId
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getUserOnce(userId)

        assertNotNull(result)
        assertEquals("testuser", result!!.username)
    }

    @Test
    fun getUserOnce_notFound_returnsNull() = runTest {
        val userId = "nonexistent"
        val mockDoc = mockk<DocumentSnapshot> {
            every { toObject(User::class.java) } returns null
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getUserOnce(userId)

        assertNull(result)
    }

    @Test
    fun watchUser_returnUser() = runTest {
        val userId = "user1"
        val user = User(
            userId = userId,
            username = "testuser",
            email = "test@example.com"
        )

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<DocumentSnapshot>
                    val mockDoc = mockk<DocumentSnapshot> {
                        every { toObject(User::class.java) } returns user
                        every { id } returns userId
                    }
                    callback.onEvent(mockDoc, null)
                    mockk()
                }
            }
        }

        val result = source.watchUser(userId).first()

        assertNotNull(result)
        assertEquals("testuser", result!!.username)
    }

    @Test
    fun watchUser_returnNull() = runTest {
        val userId = "user999"

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<DocumentSnapshot>
                    val mockDoc = mockk<DocumentSnapshot> {
                        every { toObject(User::class.java) } returns null
                    }
                    callback.onEvent(mockDoc, null)
                    mockk()
                }
            }
        }

        val result = source.watchUser(userId).first()

        assertNull(result)
    }

    @Test
    fun upsertUser_success() = runTest {
        val user = User(
            userId = "user1",
            username = "newuser",
            email = "new@example.com"
        )
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(user.userId) } returns mockk {
                coEvery { set(user) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertUser(user)

        coVerify { mockFirestore.collection("users").document(user.userId).set(user) }
    }

    @Test
    fun deleteUser_success() = runTest {
        val userId = "user1"
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.deleteUser(userId)

        coVerify { mockFirestore.collection("users").document(userId).delete() }
    }

    @Test
    fun watchUser_multipleEmissions() = runTest {
        val userId = "user1"
        val user1 = User(userId = userId, username = "user1", email = "old@example.com")
        val user2 = User(userId = userId, username = "user1", email = "new@example.com")

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<DocumentSnapshot>
                    val mockDoc1 = mockk<DocumentSnapshot> {
                        every { toObject(User::class.java) } returns user1
                        every { id } returns userId
                    }
                    val mockDoc2 = mockk<DocumentSnapshot> {
                        every { toObject(User::class.java) } returns user2
                        every { id } returns userId
                    }
                    callback.onEvent(mockDoc1, null)
                    callback.onEvent(mockDoc2, null)
                    mockk()
                }
            }
        }

        val result = mutableListOf<User?>()
        source.watchUser(userId).collect { result.add(it) }

        assertEquals(2, result.size)
        assertEquals("old@example.com", result[0]?.email)
        assertEquals("new@example.com", result[1]?.email)
    }

    @Test
    fun upsertUser_update() = runTest {
        val user = User(userId = "user1", username = "updateduser", email = "updated@example.com")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(user.userId) } returns mockk {
                coEvery { set(any<User>()) } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        source.upsertUser(user)
        source.upsertUser(user.copy(username = "newname"))

        coVerify(exactly = 2) { mockFirestore.collection("users").document(user.userId).set(any<User>()) }
    }

    @Test
    fun deleteUser_multiple() = runTest {
        val userIds = listOf("user1", "user2", "user3")
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document(any<String>()) } returns mockk {
                coEvery { delete() } returns mockTask
            }
        }
        coEvery { mockTask.await() as Unit } returns Unit

        userIds.forEach { userId ->
            source.deleteUser(userId)
        }

        coVerify(exactly = 3) { mockFirestore.collection("users").document(any()).delete() }
    }

    @Test
    fun getUserOnce_emptyId_returnsNull() = runTest {
        val mockDoc = mockk<DocumentSnapshot> {
            every { toObject(User::class.java) } returns null
        }
        val mockTask = mockk<Task<DocumentSnapshot>>(relaxed = true)

        every { mockFirestore.collection("users") } returns mockk {
            every { document("") } returns mockk {
                coEvery { get() } returns mockTask
            }
        }
        coEvery { mockTask.await() } returns mockDoc

        val result = source.getUserOnce("")

        assertNull(result)
    }

    @Test
    fun watchUser_error_closesFlow() = runTest {
        val userId = "user1"

        every { mockFirestore.collection("users") } returns mockk {
            every { document(userId) } returns mockk {
                every { addSnapshotListener(any()) } answers {
                    val callback = it.invocation.args[0] as com.google.firebase.firestore.EventListener<DocumentSnapshot>
                    callback.onEvent(
                        null,
                        FirebaseFirestoreException(
                            "Firestore error",
                            FirebaseFirestoreException.Code.UNKNOWN
                        )
                    )
                    mockk()
                }
            }
        }

        try {
            source.watchUser(userId).first()
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }
}
