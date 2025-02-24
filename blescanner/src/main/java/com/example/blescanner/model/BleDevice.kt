package com.example.blescanner.model

import android.bluetooth.le.ScanRecord

/**
 * Data class to store discovered BLE peripheral details
 */
data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val scanRecord: ScanRecord?
)