// MyApplication.kt
package com.example.doancuoikymobile

import android.app.Application
import android.content.Intent
import com.example.doancuoikymobile.player.MediaPlayerService
import com.example.doancuoikymobile.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        testBackendOnly()
    }

    private fun testBackendOnly() {
        val songRepository = SongRepository()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val song = songRepository.getSongById("8wvv3B9c")

                if (song != null) {
                    println("=== BE TEST OK ===")
                    println("Title: ${song.title}")
                    println("AudioUrl: ${song.audioUrl}")
                } else {
                    println("=== BE TEST FAIL: Song null ===")
                }
            } catch (e: Exception) {
                println("=== BE TEST ERROR: ${e.message} ===")
            }
        }
    }
}
