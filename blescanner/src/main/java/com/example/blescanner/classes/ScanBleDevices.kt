package com.example.blescanner.classes

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
import com.example.common.Constants
import com.example.common.ProcessResultImpl
import com.example.common.event.Event
import com.example.common.event.EventName
import com.example.common.notifyEvent
import com.example.controller.Command
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

/**
 * Command to start BLE scan
 */
class ScanBleDevices: Command {
    private lateinit var context: Context
    private lateinit var payload: JSONObject

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices: StateFlow<List<BleDevice>> get() = _devices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> get() = _isScanning

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner

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
                put(Event.EVENT_CODE, Constants.EVENT_SCAN_ERROR_CODE)
                put(Event.EVENT_MESSAGE, "Scan failed with error code: $errorCode")
            })
        }
    }

    override suspend fun execute(): ProcessResultImpl {
        val jsonResponse = JSONObject()

        if (!::context.isInitialized) {
            jsonResponse.put(Constants.RESULT_CODE, "MISSING_CONTEXT")
            jsonResponse.put(Constants.RESULT_MESSAGE, "Context is required")
            return ProcessResultImpl(jsonResponse)
        }

        val scanTimeout = if (::payload.isInitialized) payload.optInt(Constants.SCAN_TIMEOUT).toLong() else 10000L

        // Start scanning
        startScan()

        // Wait for 10 seconds or until scanning is stopped
        try {
            withTimeout(scanTimeout) {
                _isScanning.collect { isScanning ->
                    if (!isScanning) {
                        throw CancellationException("Scanning stopped")
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.d("BLE_SCAN", "Scanning completed after timeout.")
        } catch (e: CancellationException) {
            Log.d("BLE_SCAN", "Scanning stopped manually.")
        } finally {
            stopScan()
        }

        // Prepare the JSON response
        jsonResponse.put(Constants.RESULT_CODE, Constants.SUCCESS_CODE)
        jsonResponse.put(Constants.RESULT_MESSAGE, Constants.SUCCESS_MESSAGE)
        jsonResponse.put(Constants.DATA, getScannedDevicesAsJson())
        return ProcessResultImpl(jsonResponse)
    }

    private fun startScan() {
        if (_isScanning.value) return

        // Check Bluetooth enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BLE_SCAN", "Bluetooth is OFF")
            return
        }

        // Check BLE Permissions
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

        // Check Location Services
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("BLE_SCAN", "Location Services are OFF. Enable them for BLE scanning.")
            return
        }

        // Start scanning
        _isScanning.value = true
        scanner?.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
        Log.d("BLE_SCAN", "Scanning started...")
        notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
            put(Event.EVENT_CODE, Constants.EVENT_SCAN_START_CODE)
            put(Event.EVENT_MESSAGE, "Scanning started...")
        })
    }

    private fun stopScan() {
        if (!_isScanning.value) return
        _isScanning.value = false
        scanner?.stopScan(scanCallback)
        Log.d("BLE_SCAN", "Scanning ended.")
        notifyEvent(EventName.BLE_SCAN_EVENT.strName, JSONObject().apply {
            put(Event.EVENT_CODE, Constants.EVENT_SCAN_STOP_CODE)
            put(Event.EVENT_MESSAGE, "Scanning ended.")
        })
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

    override fun setParameters(parameters: Any?) {
        parameters?.let {
            payload = JSONObject("$parameters")
        }
    }

    override fun setContext(context: Context) {
        this.context = context
    }
}