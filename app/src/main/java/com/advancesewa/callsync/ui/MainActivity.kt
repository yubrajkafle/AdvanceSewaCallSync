package com.advancesewa.callsync.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.advancesewa.callsync.databinding.ActivityMainBinding
import java.text.DateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = CallLogAdapter()

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val ok = grants.all { it.value }
        if (ok) loadLogs() else Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        ensurePermissionsAndLoad()
    }

    private fun ensurePermissionsAndLoad() {
        val needs = mutableListOf<String>()
        fun need(p: String) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) needs += p
        }
        need(Manifest.permission.READ_CALL_LOG)
        need(Manifest.permission.READ_MEDIA_AUDIO)
        if (android.os.Build.VERSION.SDK_INT <= 32) {
            need(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (needs.isNotEmpty()) {
            requestPermissions.launch(needs.toTypedArray())
        } else {
            loadLogs()
        }
    }

    private fun loadLogs() {
        val list = mutableListOf<CallItem>()
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME
            ),
            null, null,
            CallLog.Calls.DATE + " DESC"
        )?.use { c ->
            val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            while (c.moveToNext()) {
                val number = c.getString(0)
                val type = c.getInt(1)
                val date = c.getLong(2)
                val duration = c.getLong(3)
                val name = c.getString(4) ?: (number ?: "Unknown")
                val hasRec = RecordingFinder.hasRecording(number, date, duration)
                val whenText = df.format(Date(date))
                val durText = if (duration > 0) " • ${duration}s" else ""
                list += CallItem(
                    primary = name,
                    secondary = "$whenText$durText",
                    type = type,
                    hasRecording = hasRec
                )
            }
        }
        adapter.submit(list)
    }
}
