package com.example.blescanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.blescanner.model.BleDevice
import com.example.common.event.Event
import com.example.common.event.EventName
import com.example.common.notifyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject


/**
 * BLE Scanner Singleton
 */
object BleScanner {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner
    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices: StateFlow<List<BleDevice>> get() = _devices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> get() = _isScanning

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = BleDevice(
                name = result.device.name ?: "Unknown",
                address = result.device.address,
                rssi = result.rssi,
                scanRecord = result.scanRecord
            )

            Log.d("BLE_SCAN", "Device found: ${device.name} - ${device.address} - RSSI: ${device.rssi}")

            // Avoid duplicates
            if (_devices.value.none { it.address == device.address }) {
                _devices.value += device
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE_SCAN", "Scan failed with error code: $errorCode")
            _isScanning.value = false
            notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
                put(Event.EVENT_MESSAGE, "Scan failed with error code: $errorCode")
            })
        }
    }

    fun startScan(context: Context) {
        if (_isScanning.value) return

        // ✅ 1️⃣ Check Bluetooth enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BLE_SCAN", "Bluetooth is OFF")
            notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
                put(Event.EVENT_MESSAGE, "Bluetooth is OFF")
            })
            return
        }

        // ✅ 2️⃣ Check BLE Permissions (Android 12+ requires BLUETOOTH_SCAN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BLE_SCAN", "Missing BLUETOOTH_SCAN permission")
                return
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BLE_SCAN", "Missing ACCESS_FINE_LOCATION permission")
                return
            }
        }

        // ✅ 3️⃣ Check if Location Services are enabled
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("BLE_SCAN", "Location Services are OFF. Enable them for BLE scanning.")
            notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
                put(Event.EVENT_MESSAGE, "Location Services are OFF. Enable them for BLE scanning.")
            })
            return
        }

        // ✅ 4️⃣ Start scanning
        _isScanning.value = true
        scanner?.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
        Log.d("BLE_SCAN", "Scanning started...")
        notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
            put(Event.EVENT_MESSAGE, "Scanning started...")
        })

        // ✅ 5️⃣ Stop scanning after 10 seconds (to save battery)
        Handler(Looper.getMainLooper()).postDelayed({
            stopScan()
        }, 10000)
    }

    fun stopScan(): String {
        if (!_isScanning.value) return "[]"
        _isScanning.value = false
        scanner?.stopScan(scanCallback)
        Log.d("BLE_SCAN", "Scanning ended.")
        notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
            put(Event.EVENT_MESSAGE, "Scanning ended.")
        })

        return getScannedDevicesAsJson()
    }

    private fun getScannedDevicesAsJson(): String {
        val jsonArray = JSONArray()
        _devices.value.forEach { device ->
            val jsonObject = JSONObject().apply {
                put("name", device.name)
                put("address", device.address)
                put("rssi", device.rssi)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}