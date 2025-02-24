package com.example.blescanner.classes

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blescanner.BleScanner
import com.example.common.Constants
import com.example.common.ProcessResultImpl
import com.example.controller.Command
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Command to start BLE scan
 */
class ScanBleDevices: Command {
    private lateinit var context: Context

    override suspend fun execute(): ProcessResultImpl {
        val jsonResponse = JSONObject()

        if (!::context.isInitialized) {
            jsonResponse.put(Constants.RESULT_CODE, "MISSING_CONTEXT")
            jsonResponse.put(Constants.RESULT_MESSAGE, "Context is required")
            return ProcessResultImpl(jsonResponse)
        }

        BleScanner.startScan(context)

        return suspendCancellableCoroutine { continutation ->
            var scannedDevices = ""
            Handler(Looper.getMainLooper()).postDelayed({
                scannedDevices = BleScanner.stopScan()
                Log.d("BLE_JSON_RESPONSE", scannedDevices) // Return JSON here
                jsonResponse.put(Constants.RESULT_CODE, Constants.SUCCESS_CODE)
                jsonResponse.put(Constants.RESULT_MESSAGE, Constants.SUCCESS_MESSAGE)
                jsonResponse.put(Constants.DATA, scannedDevices)
                continutation.resume(ProcessResultImpl(jsonResponse))
            }, 10000) // Scan for 5 seconds then stop
        }
    }

    override fun setParameters(parameters: Any?) {

    }

    override fun setContext(context: Context) {
        this.context = context
    }
}