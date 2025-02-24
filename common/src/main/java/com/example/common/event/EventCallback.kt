package com.example.common.event

import org.json.JSONObject

/**
 * Interface for [Event]
 */
interface EventCallback {

    /**
     * Callback for [Event]
     *
     * @param params response object
     */
    fun onCallback(params: JSONObject)
}
