package com.example.androidble

import android.app.Activity
import com.example.common.toJSON
import com.example.controller.CommandManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * The method that run commands and accept required parameters
 *
 * @param command The class name of the command to be executed
 * @param params Parameter required for the command
 * @param responseCallback Callback invoker for command response if provided
 *
 */
fun Activity.runCommand(
    command: String,
    params: JSONObject? = null,
    responseCallback: ((JSONObject) -> Unit)? = null
) {
    CoroutineScope(Dispatchers.IO).launch {
        CommandManager()
            .setCommand(command)
            .setContext(this@runCommand)
            .setParameters(params)
            .execute()
            .let { response ->
                withContext(Dispatchers.Main) {
                    response?.getResponse()?.toJSON()?.let { jsonResponse ->
                        responseCallback?.invoke(jsonResponse)
                    }
                }
            }
    }
}

/**
 * Overload method of runCommand
 *
 * @param command The class name of the command to be executed
 * @param params Parameter required for the command
 * @param responseCallback Callback invoker for command response if provided
 * @param useStringBuilder [Boolean] to use or not use StringBuilder as callback
 *
 */
fun Activity.runCommand(
    command: String,
    params: JSONObject? = null,
    responseCallback: ((StringBuilder) -> Unit)? = null,
    useStringBuilder: Boolean = false
) {
    if (useStringBuilder) {
        this.runCommand(command, params) { jsonResponse ->
            val stringBuilder = StringBuilder(jsonResponse.toString())
            responseCallback?.invoke(stringBuilder)
        }
    } else {
        this.runCommand(command, params)
    }
}
