package com.example.controller

import android.content.Context
import android.util.Log

/**
 * The purpose of CommandManager is to build and gather the required
 * parameters of the class implementing Command interface and execute
 * the instructions provided.
 */
class CommandManager {

    private var command: Command? = null

    private val modules: List<String> = listOf(
        Modules.MODULE_BLE_SCANNER
    )

    /**
     * Return an instance of [CommandManager] if the [commandClassName]
     * value does exists from package and module lookups.
     *
     * @param commandClassName The class's command name to look for/
     *
     * @return Return instance of [CommandManager] for valid command name
     **/
    fun setCommand(commandClassName: String): CommandManager {
        var command: Command? = null

        /* Iterate through service layers first */
        for (module in modules) {
            /* Command Class lookup from modules inside package  */
            val completeClassPath = "com.example.$module.classes.$commandClassName"
            try {
                val commandLookup: Any? = Class.forName(completeClassPath).newInstance()
                if (commandLookup != null) {
                    command = commandLookup as Command
                    break
                }
            } catch (e: Exception) {
                /* Do nothing here */
            }
        }
        if (command == null) {
            Log.e(CommandManager::class.java.simpleName, "$LOG_RECEIVED_CMD_ERROR $commandClassName")
        } else {
            Log.e(CommandManager::class.java.simpleName, "$LOG_RECEIVED_CMD_OK $commandClassName")
        }

        this.command = command
        return this
    }

    /**
     * Sets the required parameters for the command
     *
     * @param parameters any parameters
     * @return current instance of this class
     */
    fun setParameters(parameters: Any?): CommandManager {
        this.command?.setParameters(parameters)
        return this
    }

    /**
     * Sets the context for the command
     *
     * @param context context
     * @return current instance of this class
     */
    fun setContext(context: Context): CommandManager {
        this.command?.setContext(context)
        return this
    }

    /**
     * The method that triggers the execution of the Command interface
     * and return an instance of [Response] interface that manage the
     * result of Command interface execution whether it is successful or not.
     *
     * @return Return instance of [Response]
     */
    suspend fun execute(): Response? {
        return this.command?.execute()
    }

    private companion object {
        const val LOG_RECEIVED_CMD_OK: String = "Received command: "
        const val LOG_RECEIVED_PARAMS_OK: String = "Received params: "
        const val LOG_RECEIVED_CMD_ERROR: String = "Received command does not exist:"
    }
}
