package com.example.doancuoikymobile.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import com.google.android.gms.tasks.Task
import org.junit.Before
import org.junit.Test
import java.io.File
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.tasks.await

class StorageDataSourceTest {
    private lateinit var source: StorageDataSource
    private val mockStorage: FirebaseStorage = mockk()
    private val mockRoot: StorageReference = mockk()

    @Before
    fun setup() {
        mockkStatic(FirebaseStorage::class)
        every { FirebaseStorage.getInstance() } returns mockStorage
        every { mockStorage.reference } returns mockRoot
        source = StorageDataSource(mockStorage)
    }

    @Test
    fun uploadSongFile_success_returnsUrl() = runTest {
        val localFile = mockk<File>()
        val remotePath = "songs/song1.mp3"
        val expectedUrl = "http://example.com/songs/song1.mp3"
        val mockRef = mockk<StorageReference>()
        val mockUploadTask = mockk<UploadTask>(relaxed = true)
        val mockUrlTask = mockk<Task<Uri>>(relaxed = true)

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.putFile(any<Uri>()) } returns mockUploadTask
        coEvery { mockUploadTask.await() } returns mockk<UploadTask.TaskSnapshot>()
        coEvery { mockRef.downloadUrl } returns mockUrlTask
        coEvery { mockUrlTask.await() } returns Uri.parse(expectedUrl)

        val result = source.uploadSongFile(localFile, remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockRef.putFile(any<Uri>()) }
    }

    @Test
    fun uploadCover_success_returnsUrl() = runTest {
        val localFile = mockk<File>()
        val remotePath = "covers/cover1.jpg"
        val expectedUrl = "http://example.com/covers/cover1.jpg"
        val mockRef = mockk<StorageReference>()
        val mockUploadTask = mockk<UploadTask>(relaxed = true)
        val mockUrlTask = mockk<Task<Uri>>(relaxed = true)

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.putFile(any<Uri>()) } returns mockUploadTask
        coEvery { mockUploadTask.await() } returns mockk<UploadTask.TaskSnapshot>()
        coEvery { mockRef.downloadUrl } returns mockUrlTask
        coEvery { mockUrlTask.await() } returns Uri.parse(expectedUrl)

        val result = source.uploadCover(localFile, remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockRef.putFile(any<Uri>()) }
    }

    @Test
    fun getDownloadUrl_success_returnsUrl() = runTest {
        val remotePath = "songs/song1.mp3"
        val expectedUrl = "http://example.com/signed-url"
        val mockRef = mockk<StorageReference>()
        val mockTask = mockk<Task<Uri>>(relaxed = true)

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.downloadUrl } returns mockTask
        coEvery { mockTask.await() } returns Uri.parse(expectedUrl)

        val result = source.getDownloadUrl(remotePath)

        assertEquals(expectedUrl, result)
        coVerify { mockRef.downloadUrl }
    }

    @Test
    fun delete_success() = runTest {
        val remotePath = "songs/song1.mp3"
        val mockRef = mockk<StorageReference>()
        val mockTask = mockk<Task<Void>>(relaxed = true)

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.delete() } returns mockTask
        coEvery { mockTask.await() as Unit } returns Unit

        source.delete(remotePath)

        coVerify { mockRef.delete() }
    }

    @Test
    fun uploadSongFile_exception() = runTest {
        val localFile = mockk<File>()
        val remotePath = "songs/error.mp3"
        val exception = Exception("Upload failed")
        val mockRef = mockk<StorageReference>()

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.putFile(any<Uri>()) } throws exception

        try {
            source.uploadSongFile(localFile, remotePath)
        } catch (e: Exception) {
            assertEquals("Upload failed", e.message)
        }
    }

    @Test
    fun delete_exception() = runTest {
        val remotePath = "songs/notfound.mp3"
        val exception = Exception("File not found")
        val mockRef = mockk<StorageReference>()

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.delete() } throws exception

        try {
            source.delete(remotePath)
        } catch (e: Exception) {
            assertEquals("File not found", e.message)
        }
    }

    @Test
    fun getDownloadUrl_exception() = runTest {
        val remotePath = "songs/notfound.mp3"
        val exception = Exception("File not found")
        val mockRef = mockk<StorageReference>()

        every { mockRoot.child(remotePath) } returns mockRef
        coEvery { mockRef.downloadUrl } throws exception

        try {
            source.getDownloadUrl(remotePath)
        } catch (e: Exception) {
            assertEquals("File not found", e.message)
        }
    }
}
