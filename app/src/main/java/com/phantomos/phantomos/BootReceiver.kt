package com.phantomos.phantomos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("PhantomBoot", "Dispositivo reiniciado. Iniciando servicios fantasma...")
            context?.startService(Intent(context, ServerService::class.java))
            context?.startService(Intent(context, LocationService::class.java))
        }
    }
}
