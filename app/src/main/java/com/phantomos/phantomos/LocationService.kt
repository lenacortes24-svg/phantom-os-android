package com.phantomos.phantomos

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log

class LocationService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private val TAG = "LocService"

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        // Usa WiFi y GPS para mayor precisión con menor consumo
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1f, this)
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            Log.d(TAG, "Nueva ubicación: ${location.latitude}, ${location.longitude}")
            // Aquí podrías enviar las coordenadas al servidor central
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
