package com.phantom.os.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.phantom.os.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.tv_status)
        findViewById<Button>(R.id.btn_local_check).setOnClickListener {
            statusText.setText(R.string.local_check_complete)
        }
    }
}