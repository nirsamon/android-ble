package com.example.common.event

/**
 * Enum class for events concerning event updates
 *
 * @property strName name in camel case form
 */
enum class EventName(val strName: String) {
    BLE_SCAN_EVENT("onBleScan")
}
