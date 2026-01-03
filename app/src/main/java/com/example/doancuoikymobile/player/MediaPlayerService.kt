package com.example.doancuoikymobile.player

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media3.session.MediaSession
import com.example.doancuoikymobile.utils.Constants

class MediaPlayerService : Service() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo PlayerManager
        PlayerManager.init(this)

        // Lấy ExoPlayer từ PlayerManager
        val player = PlayerManager.player ?: throw IllegalStateException("Player chưa được khởi tạo!")

        // Khởi tạo MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .build()

        // Hiển thị thông báo (Notification)
        val playerNotification = PlayerNotification(this)
        startForeground(Constants.NOTIFICATION_ID, playerNotification.createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Media3 dùng token để Activity/Fragment có thể điều khiển nhạc qua MediaController
        return mediaSession?.token?.let {
            null
        }
    }

    override fun onDestroy() {
        // Giải phóng MediaSession trước, sau đó mới đến Player
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        PlayerManager.release()
        super.onDestroy()
    }
}