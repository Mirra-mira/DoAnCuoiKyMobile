package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.StorageDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.tasks.await

class FileRepositoryTest {
    private lateinit var repository: FileRepository
    private val mockStorage: StorageDataSource = mockk()

    @Before
    fun setup() {
        repository = FileRepository(mockStorage)
    }

    @Test
    fun uploadSongFile_success_returnsUrl() = runTest {
        val file = mockk<File>()
        val remotePath = "songs/song1.mp3"
        val expectedUrl = "http://example.com/songs/song1.mp3"

        coEvery { mockStorage.uploadSongFile(file, remotePath) } returns expectedUrl

        val result = repository.uploadSongFile(file, remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockStorage.uploadSongFile(file, remotePath) }
    }

    @Test
    fun uploadCover_success_returnsUrl() = runTest {
        val file = mockk<File>()
        val remotePath = "covers/cover1.jpg"
        val expectedUrl = "http://example.com/covers/cover1.jpg"

        coEvery { mockStorage.uploadCover(file, remotePath) } returns expectedUrl

        val result = repository.uploadCover(file, remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockStorage.uploadCover(file, remotePath) }
    }

    @Test
    fun delete_success() = runTest {
        val remotePath = "songs/song1.mp3"

        coEvery { mockStorage.delete(remotePath) } returns Unit

        repository.delete(remotePath)

        coVerify { mockStorage.delete(remotePath) }
    }

    @Test
    fun getDownloadUrl_success_returnsUrl() = runTest {
        val remotePath = "songs/song1.mp3"
        val expectedUrl = "http://example.com/signed-url"

        coEvery { mockStorage.getDownloadUrl(remotePath) } returns expectedUrl

        val result = repository.getDownloadUrl(remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockStorage.getDownloadUrl(remotePath) }
    }

    @Test
    fun uploadSongFile_multipleFiles() = runTest {
        val files = listOf(
            mockk<File>() to "songs/song1.mp3",
            mockk<File>() to "songs/song2.mp3"
        )

        coEvery { mockStorage.uploadSongFile(any(), any()) } returns "http://example.com/uploaded.mp3"

        files.forEach { (file, path) ->
            repository.uploadSongFile(file, path)
        }

        coVerify(exactly = 2) { mockStorage.uploadSongFile(any(), any()) }
    }

    @Test
    fun uploadCover_multipleFiles() = runTest {
        val files = listOf(
            mockk<File>() to "covers/cover1.jpg",
            mockk<File>() to "covers/cover2.jpg"
        )

        coEvery { mockStorage.uploadCover(any(), any()) } returns "http://example.com/cover.jpg"

        files.forEach { (file, path) ->
            repository.uploadCover(file, path)
        }

        coVerify(exactly = 2) { mockStorage.uploadCover(any(), any()) }
    }

    @Test
    fun delete_multipleFiles() = runTest {
        val paths = listOf("songs/song1.mp3", "songs/song2.mp3", "covers/cover1.jpg")

        coEvery { mockStorage.delete(any()) } returns Unit

        paths.forEach { path ->
            repository.delete(path)
        }

        coVerify(exactly = 3) { mockStorage.delete(any()) }
    }

    @Test
    fun getDownloadUrl_multipleFiles() = runTest {
        val paths = listOf("songs/song1.mp3", "covers/cover1.jpg")

        coEvery { mockStorage.getDownloadUrl(any()) } returns "http://example.com/url"

        paths.forEach { path ->
            repository.getDownloadUrl(path)
        }

        coVerify(exactly = 2) { mockStorage.getDownloadUrl(any()) }
    }

    @Test
    fun uploadSongFile_exception() = runTest {
        val file = mockk<File>()
        val remotePath = "songs/error.mp3"
        val exception = Exception("Upload failed")

        coEvery { mockStorage.uploadSongFile(file, remotePath) } throws exception

        try {
            repository.uploadSongFile(file, remotePath)
        } catch (e: Exception) {
            assertEquals("Upload failed", e.message)
        }
    }

    @Test
    fun delete_exception() = runTest {
        val remotePath = "songs/notfound.mp3"
        val exception = Exception("File not found")

        coEvery { mockStorage.delete(remotePath) } throws exception

        try {
            repository.delete(remotePath)
        } catch (e: Exception) {
            assertEquals("File not found", e.message)
        }
    }
}
