package com.phantom.os.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.BatteryManager
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import java.io.BufferedReader
import java.io.InputStreamReader

object DeviceInfoCollector {

    fun getFullDeviceInfo(context: Context): String {
        return buildString {
            append("=== PHANTOM DEVICE INFO ===\n")
            
            // Modelo y Versión de Android
            append("Model: ${android.os.Build.MODEL}\n")
            append("Android Version: ${android.os.Build.VERSION.RELEASE}\n")
            append("Manufacturer: ${android.os.Build.MANUFACTURER}\n")

            // ID Único (IMEI para móviles)
            @SuppressLint("HardwareIds")
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val imei = if (telephonyManager.deviceId.isNotEmpty()) telephonyManager.deviceId else "No IMEI"
            append("IMEI: $imei\n")

            // Ubicación
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val lat = location?.latitude ?: 0.0
                val lon = location?.longitude ?: 0.0
                append("Location: $lat, $lon\n")
            } catch (e: Exception) {
                append("Location: Error obtaining location\n")
            }

            // Nivel de Batería
            val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (scale > 0 && level >= 0) (level * 100f / scale).toInt() else -1
            append("Battery: $batteryPct%\n")

            // ID de Anuncio de Publicidad (para tracking cruzado)
            val advId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
            append("Advertising ID: $advId\n")

            append("===========================")
        }
    }
}
