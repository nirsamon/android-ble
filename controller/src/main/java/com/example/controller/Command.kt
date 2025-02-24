package com.example.controller

import android.content.Context

/**
 * Interface for implementing commands
 */
interface Command {

    /**
     * Execute the logic
     *
     * @return [Response] object
     */
    suspend fun execute(): Response

    /**
     * Set the parameters
     *
     * @param parameters any parameters
     */
    fun setParameters(parameters: Any? = null)

    /**
     * Set the context
     *
     * @param context context
     */
    fun setContext(context: Context)
}
