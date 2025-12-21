package com.example.doancuoikymobile.data.remote.api

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test

class SongApiServiceTest {

    private val apiService = mockk<SongApiService>()

    @Test
    fun searchSongs_called_returnsResponse() = runTest {
        val response = SaavnResponse(data = emptyList())

        coEvery { apiService.searchSongs(any()) } returns response

        val result = apiService.searchSongs("test")

        assertNotNull(result)
        coVerify(exactly = 1) { apiService.searchSongs("test") }
    }

    @Test
    fun getSongDetail_called_returnsResponse() = runTest {
        val response = SaavnResponse(data = emptyList())

        coEvery { apiService.getSongDetail(any()) } returns response

        val result = apiService.getSongDetail("123")

        assertNotNull(result)
        coVerify(exactly = 1) { apiService.getSongDetail("123") }
    }
}
