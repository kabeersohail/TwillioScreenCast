package com.example.twillioscreencast.services

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.twillioscreencast.R
import com.example.twillioscreencast.utils.Constants.CHANNEL_ID
import com.example.twillioscreencast.utils.Constants.CHANNEL_NAME

@TargetApi(29)
class ScreenCapturerService : Service() {

    private val binder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): ScreenCapturerService = this@ScreenCapturerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    fun startForeground() {
        val notificationChannel: NotificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(notificationChannel)

        val notificationID: Int = System.currentTimeMillis().toInt()

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)

        val notification: Notification = notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_screen_share_white_24dp)
            .setContentTitle("ScreenCapturerService is running in the foreground")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(notificationID, notification)

    }

    fun endForeground() = stopForeground(true)

    override fun onBind(intent: Intent?): IBinder = binder
}