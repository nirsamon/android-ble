package com.example.common

import com.example.common.event.Event
import com.example.common.event.EventManager
import org.json.JSONObject

/**
 * Checks if the object returns a success response
 *
 * @return true if compared values are equal, otherwise false
 */
fun JSONObject.isSuccessful(): Boolean {
    return this.optInt(
        Constants.RESULT_CODE,
        Constants.ERROR_CODE
    ) == Constants.SUCCESS_CODE
}

/**
 * Checks if the JSONObject is not empty/valid
 *
 * @return true if valid, otherwise false
 */
fun JSONObject.isValid(): Boolean {
    return this.length() != 0
}

/**
 * Checks if the String is a valid JSONObject format
 *
 * @return true if String provided matches JSONObject format, otherwise false
 */
fun String.isValidJSON(): Boolean {
    return try {
        JSONObject(this)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * The purpose of this function is to convert String to JSONObject format
 *
 * @return The JSONObject format equivalent of the String
 */
fun String.toJSON(): JSONObject {
    return JSONObject(this)
}

/**
 * Function that sends out an event notification
 *
 * @param eventName The name of the event
 * @param data The data to send out
 */
fun notifyEvent(eventName: String, data: JSONObject) {
    Event(
        name = eventName,
        data = data
    ).also {
        EventManager.INSTANCE.notify(it)
    }
}