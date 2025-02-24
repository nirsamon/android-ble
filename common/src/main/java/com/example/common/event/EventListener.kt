package com.example.common.event

/**
 * Interface for [Event] calls
 */
interface EventListener<T : Event> {

    /**
     * Callback for [Event] call
     *
     * @param event any [Event]
     */
    fun handle(event: T)
}
