package com.example.common

import com.example.controller.Response
import org.json.JSONObject

/**
 * The class responsible for mapping the response object.
 *
 * @return instance of [Response] which assigns the values of interface members
 */
class ProcessResultImpl(private val response: JSONObject) : Response {

    override fun isSuccess(): Boolean {
        return response.getInt(Constants.RESULT_CODE) == 0
    }

    override fun getResponse(): String {
        return response.toString()
    }

    override fun getErrorCode(): Int {
        return if (isSuccess()) {
            0
        } else {
            response.getInt(
                Constants.RESULT_CODE
            )
        }
    }

    override fun getErrorMessage(): String {
        return response.getString(Constants.RESULT_MESSAGE)
    }
}