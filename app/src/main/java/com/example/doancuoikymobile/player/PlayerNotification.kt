package com.example.doancuoikymobile.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.activity.MainActivity
import com.example.doancuoikymobile.utils.Constants

class PlayerNotification(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotification(): Notification {
        createChannel()

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note) // Bạn cần tạo icon này hoặc dùng ic_play_arrow tạm
            .setContentTitle(PlayerManager.getCurrentSong()?.title ?: "Đang phát")
            .setContentText("Nghệ sĩ") // Có thể mở rộng sau
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification cho trình phát nhạc"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}