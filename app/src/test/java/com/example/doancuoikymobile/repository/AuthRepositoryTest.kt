package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.FirebaseAuthSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue

class AuthRepositoryTest {
    private lateinit var repository: AuthRepository
    private val mockAuthSource: FirebaseAuthSource = mockk()

    @Before
    fun setup() {
        repository = AuthRepository()
    }

    @Test
    fun signIn_success_returnsSuccessWithUser() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val mockUser = mockk<FirebaseUser>()
        val mockAuthResult = mockk<AuthResult> {
            every { user } returns mockUser
        }

        coEvery { mockAuthSource.signIn(email, password) } returns mockAuthResult

        val result = repository.signIn(email, password)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun signIn_failure_returnsFailure() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = Exception("Invalid credentials")

        coEvery { mockAuthSource.signIn(email, password) } throws exception

        val result = repository.signIn(email, password)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun signUp_success_returnsSuccessWithUser() = runTest {
        val email = "newuser@example.com"
        val password = "password123"
        val mockUser = mockk<FirebaseUser>()
        val mockAuthResult = mockk<AuthResult> {
            every { user } returns mockUser
        }

        coEvery { mockAuthSource.signUp(email, password) } returns mockAuthResult

        val result = repository.signUp(email, password)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun signUp_failure_returnsFailure() = runTest {
        val email = "invalid@email"
        val password = "pass"
        val exception = Exception("Invalid email format")

        coEvery { mockAuthSource.signUp(email, password) } throws exception

        val result = repository.signUp(email, password)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun getCurrentUser_returnsUser() {
        val mockUser = mockk<FirebaseUser>()
        every { mockAuthSource.getCurrentUser() } returns mockUser

        val result = repository.getCurrentUser()

        assertNotNull(result)
    }

    @Test
    fun getCurrentUser_returnsNull() {
        every { mockAuthSource.getCurrentUser() } returns null

        val result = repository.getCurrentUser()

        assertNull(result)
    }

    @Test
    fun signOut_callsAuthSource() {
        every { mockAuthSource.signOut() } returns Unit

        repository.signOut()

        verify { mockAuthSource.signOut() }
    }

    @Test
    fun signIn_emptyEmail_returnsFailure() = runTest {
        val email = ""
        val password = "password123"
        val exception = Exception("Email cannot be empty")

        coEvery { mockAuthSource.signIn(email, password) } throws exception

        val result = repository.signIn(email, password)

        assertTrue(result.isFailure)
    }

    @Test
    fun signUp_emptyPassword_returnsFailure() = runTest {
        val email = "user@example.com"
        val password = ""
        val exception = Exception("Password is too short")

        coEvery { mockAuthSource.signUp(email, password) } throws exception

        val result = repository.signUp(email, password)

        assertTrue(result.isFailure)
    }
}
