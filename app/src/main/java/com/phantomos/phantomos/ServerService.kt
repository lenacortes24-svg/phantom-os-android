package com.phantomos.phantomos

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ServerService : Service() {
    private var serverSocket: ServerSocket? = null
    private val TAG = "PhantomServer"
    private var isRunning = true

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servidor Fantasma iniciado en puerto 8080")
        startListening()
    }

    private fun startListening() {
        Thread {
            try {
                // Escucha en todas las interfaces disponibles (WiFi, Data, BT Tethering)
                serverSocket = ServerSocket(8080)
                while (isRunning) {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        Log.i(TAG, "Conexión entrante detectada desde: ${clientSocket.inetAddress}")
                        handleClient(clientSocket)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error en servidor: ${e.message}")
            }
        }.start()
    }

    private fun handleClient(socket: Socket) {
        // Aquí procesamos comandos remotos (ej: activar micrófono, leer GPS)
        socket.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        serverSocket?.close()
        super.onDestroy()
    }
}
