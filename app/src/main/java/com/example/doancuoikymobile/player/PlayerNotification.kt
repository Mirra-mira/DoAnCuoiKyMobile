package com.example.doancuoikymobile.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
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

        val currentSong = PlayerManager.getCurrentSong()

        // Create RemoteViews for collapsed notification
        val collapsedView = RemoteViews(context.packageName, R.layout.layout_notification_collapsed)
        collapsedView.setTextViewText(R.id.notification_song_title, currentSong?.title ?: "Unknown Song")
        collapsedView.setTextViewText(R.id.notification_artist_name, currentSong?.artistName ?: "Unknown Artist")

        // Create RemoteViews for expanded notification
        val expandedView = RemoteViews(context.packageName, R.layout.layout_notification_expanded)
        expandedView.setTextViewText(R.id.notification_expanded_song_title, currentSong?.title ?: "Unknown Song")
        expandedView.setTextViewText(R.id.notification_expanded_artist_name, currentSong?.artistName ?: "Unknown Artist")

        // Load album art if available
        currentSong?.coverUrl?.let { coverUrl ->
            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(coverUrl)
                    .submit()
                    .get()
                
                collapsedView.setImageViewBitmap(R.id.notification_album_art, bitmap)
                expandedView.setImageViewBitmap(R.id.notification_expanded_album_art, bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
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

    fun updateNotification(notification: Notification) {
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }
}