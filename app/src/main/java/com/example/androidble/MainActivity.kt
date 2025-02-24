package com.example.androidble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androidble.databinding.ActivityMainBinding
import com.example.common.event.Event
import com.example.common.event.EventListener
import com.example.common.event.EventManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}


    private val eventListener: EventListener<Event> = object : EventListener<Event> {
        override fun handle(event: Event) {
            Log.d(javaClass.simpleName, "${event.name}: ${event.data}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }.apply {
            btnStartBleScan.setOnClickListener {
                val hasValidLocationPermission = hasValidLocationPermission()
                if (hasValidLocationPermission.not()) {
                    return@setOnClickListener
                }

                checkBluetoothPermission()
            }
        }

        EventManager.INSTANCE.register(eventListener)
    }

    /**
     * The method that triggers requesting a runtime location permission
     *
     * @return Return true if permission already granted; Otherwise, false.
     */
    private fun hasValidLocationPermission(): Boolean {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        return if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            true
        } else {
            requestMultiplePermissions.launch(permissions)
            false
        }
    }

    /**
     * The method that initiates trigger of required bluetooth permission
     * based on android OS installed on the device
     *
     */
    private fun checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (requiredPermissions.all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }) {
                // Permission already granted, proceed with BLE scanning
                runCommand(Command.SCAN_BLE_DEVICES)
            } else {
                // Request the necessary permissions
                requestMultiplePermissions.launch(requiredPermissions)
            }
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }
}
