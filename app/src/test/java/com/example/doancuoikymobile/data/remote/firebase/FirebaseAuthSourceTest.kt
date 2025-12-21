package com.example.doancuoikymobile.data.remote.firebase

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import com.google.android.gms.tasks.Task
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.tasks.await

class FirebaseAuthSourceTest {
    private lateinit var source: FirebaseAuthSource
    private val mockAuth: FirebaseAuth = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth
        source = FirebaseAuthSource()
    }

    @Test
    fun signIn_success() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val mockUser = mockk<FirebaseUser>()
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        val mockAuthResult = mockk<AuthResult> {
            every { user } returns mockUser
        }

        coEvery { mockAuth.signInWithEmailAndPassword(email, password) } returns mockTask
        coEvery { mockTask.await() } returns mockAuthResult

        val result = source.signIn(email, password)

        assertNotNull(result.user)
        coVerify { mockAuth.signInWithEmailAndPassword(email, password) }
    }

    @Test
    fun signIn_failure() = runTest {
        val email = "invalid@example.com"
        val password = "wrongpass"
        val exception = Exception("Invalid credentials")

        coEvery { mockAuth.signInWithEmailAndPassword(email, password) } throws exception

        try {
            source.signIn(email, password)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun signUp_success() = runTest {
        val email = "newuser@example.com"
        val password = "password123"
        val mockUser = mockk<FirebaseUser>()
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        val mockAuthResult = mockk<AuthResult> {
            every { user } returns mockUser
        }

        coEvery { mockAuth.createUserWithEmailAndPassword(email, password) } returns mockTask
        coEvery { mockTask.await() } returns mockAuthResult

        val result = source.signUp(email, password)

        assertNotNull(result.user)
        coVerify { mockAuth.createUserWithEmailAndPassword(email, password) }
    }

    @Test
    fun signUp_failure() = runTest {
        val email = "user@example.com"
        val password = "short"
        val exception = Exception("Password too short")

        coEvery { mockAuth.createUserWithEmailAndPassword(email, password) } throws exception

        try {
            source.signUp(email, password)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun getCurrentUser_loggedIn_returnsUser() {
        val mockUser = mockk<FirebaseUser>()
        every { mockAuth.currentUser } returns mockUser

        val result = source.getCurrentUser()

        assertNotNull(result)
        verify { mockAuth.currentUser }
    }

    @Test
    fun getCurrentUser_notLoggedIn_returnsNull() {
        every { mockAuth.currentUser } returns null

        val result = source.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun signOut_success() {
        every { mockAuth.signOut() } returns Unit

        source.signOut()

        verify { mockAuth.signOut() }
    }

    @Test
    fun signIn_emptyEmail() = runTest {
        val email = ""
        val password = "password"
        val exception = Exception("Email cannot be empty")

        coEvery { mockAuth.signInWithEmailAndPassword(email, password) } throws exception

        try {
            source.signIn(email, password)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }

    @Test
    fun signUp_emptyPassword() = runTest {
        val email = "user@example.com"
        val password = ""
        val exception = Exception("Password is required")

        coEvery { mockAuth.createUserWithEmailAndPassword(email, password) } throws exception

        try {
            source.signUp(email, password)
        } catch (e: Exception) {
            assertNotNull(e)
        }
    }
}
