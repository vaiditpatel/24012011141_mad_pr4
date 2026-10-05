package com.example.a24012011141_mad_pr4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    private var mp: MediaPlayer? = null

    companion object {
        private const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Ringing")
            .setContentText("Tap to open app and dismiss alarm")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        if (intent != null) {
            if (mp == null) {
                mp = MediaPlayer.create(this, R.raw.alarm)
                mp?.isLooping = true
            }
            mp?.start()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel",
                NotificationManager.IMPORTANCE_HIGH,
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        try {
            if (mp?.isPlaying == true) {
                mp?.stop()
            }
            mp?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mp = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}