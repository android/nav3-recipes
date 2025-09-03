package com.example.nav3recipes.results.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Local for receiving results in a [ResultEventBus]
 */
object LocalResultEventBus {
    private val LocalResultEventBus: ProvidableCompositionLocal<ResultEventBus?> =
        compositionLocalOf { null }

    /**
     * The current [ResultEventBus]
     */
    val current: ResultEventBus
        @Composable
        get() = LocalResultEventBus.current ?: error("No ResultStore has been provided")

    /**
     * Provides a [ResultEventBus] to the composition
     */
    infix fun provides(
        bus: ResultEventBus
    ): ProvidedValue<ResultEventBus?> {
        return LocalResultEventBus.provides(bus)
    }
}
/**
 * An EventBus for passing results between multiple sets of screens.
 *
 * It provides a solution for event based results.
 */
class ResultEventBus {
    /**
     * Map from the result key to a channel of results.
     */
    val channelMap: MutableMap<String, Channel<Any?>> = mutableMapOf()

    /**
     * Provides a flow for the given resultKey.
     */
    inline fun <reified T> getResultFlow(resultKey: String = T::class.toString()) =
        channelMap[resultKey]?.receiveAsFlow()

    /**
     * Sends a result into the channel associated with the given resultKey.
     */
    inline fun <reified T> sendResult(resultKey: String = T::class.toString(), result: T) {
        if (!channelMap.contains(resultKey)) {
            channelMap.put(resultKey, Channel(capacity = BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND))
        }
        channelMap[resultKey]?.trySend(result)
    }

    /**
     * Removes all results associated with the given key from the store.
     */
    inline fun <reified T> removeResult(resultKey: String = T::class.toString()) {
        channelMap.remove(resultKey)
    }
}