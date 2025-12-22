package com.example.doancuoikymobile.player

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.content.getSystemService
import com.example.doancuoikymobile.utils.Constants

class MediaPlayerService : Service() {

    private lateinit var playerNotification: PlayerNotification
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        PlayerManager.init(this)

        notificationManager = getSystemService()!!

        playerNotification = PlayerNotification(this)
        val notification = playerNotification.createNotification()

        startForeground(Constants.NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = playerNotification.createNotification()
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        PlayerManager.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
