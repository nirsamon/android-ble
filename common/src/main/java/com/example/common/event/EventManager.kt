package com.example.common.event

import kotlin.reflect.KClass

/**
 * Class for managing events
 */
class EventManager {

    val listeners = mutableMapOf<KClass<*>, MutableList<EventListener<out Event>>>()

    /**
     * Registers an event
     *
     * @param T type of event
     * @param listener listener for handling event notifications
     */
    inline fun <reified T : Event> register(listener: EventListener<T>) {
        val eventClass = T::class
        if (listeners.contains(eventClass)) return
        val eventListeners = listeners.getOrPut(eventClass) { mutableListOf() }
        eventListeners.add(listener)
    }

    /**
     * Unregisters an event
     *
     * @param T type of event
     * @param listener listener for handling event notifications
     */
    inline fun <reified T : Event> remove(listener: EventListener<T>) {
        val eventClass = T::class
        val eventListeners = listeners.remove(eventClass) ?: mutableListOf()
        eventListeners.add(listener)
    }

    /**
     * Notifies app of an event
     *
     * @param T type of event
     * @param event event to be notified
     */
    inline fun <reified T : Event> notify(event: T) {
        listeners[event::class]?.asSequence()
            ?.filterIsInstance<EventListener<T>>()
            ?.forEach { it.handle(event) }
    }

    companion object {
        val INSTANCE = EventManager()
    }
}
