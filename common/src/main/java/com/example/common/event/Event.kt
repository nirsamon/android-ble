package com.example.common.event

import org.json.JSONObject

/**
 * Class for events
 *
 * @property name event name
 * @property data any data to be sent
 * @property callback event handling
 */
open class Event(
    val name: String,
    val data: JSONObject,
    val callback: EventCallback? = null
) {

    companion object {
        const val EVENT_CODE = "eventCode"
        const val EVENT_MESSAGE = "eventMessage"
        const val EVENT_TIMESTAMP = "timestamp"
    }
}
