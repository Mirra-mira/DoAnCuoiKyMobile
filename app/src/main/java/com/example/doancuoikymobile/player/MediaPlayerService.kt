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

        // 1. Khởi tạo PlayerManager
        PlayerManager.init(this)

        // 2. Lấy ExoPlayer từ PlayerManager (Sửa tên hàm cho khớp)
        val player = PlayerManager.getPlayer()

        // 3. Khởi tạo MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .build()

        // 4. Hiển thị thông báo (Notification)
        val playerNotification = PlayerNotification(this)
        startForeground(Constants.NOTIFICATION_ID, playerNotification.createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Media3 dùng token để Activity/Fragment có thể điều khiển nhạc qua MediaController
        return mediaSession?.token?.let {
            // Nếu bạn dùng MediaSessionService thì return super.onBind(intent)
            // Còn nếu dùng Service thông thường, onBind thường trả về null hoặc Binder riêng
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