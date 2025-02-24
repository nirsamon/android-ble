package com.example.controller

/**
 * The interface class that manages the Command response
 * through JSONObject data.
 *
 */
interface Response {

    /**
     * Checks if operation is successful or not
     *
     * @return value if successful: true; failed: false
     */
    fun isSuccess(): Boolean = false

    /**
     * Gets the error message
     *
     * @return error message
     */
    fun getErrorMessage(): String = ""

    /**
     * Gets the error code
     *
     * @return error code
     */
    fun getErrorCode(): Int = 0

    /**
     * Gets [Response] in string format
     *
     * @return response in string format
     */
    fun getResponse(): String = ""
}
