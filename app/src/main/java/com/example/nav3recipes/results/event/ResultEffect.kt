package com.example.nav3recipes.results.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * An Effect to provide a result even between different screens
 *
 * The trailing lambda provides the result from a flow of results.
 *
 * @param resultEventBus the ResultEventBus to retrieve the result from
 * @param resultKey the key that should be associated with this effect
 * @param onResult the callback to invoke when a result is received
 */
@Composable
inline fun <reified T> ResultEffect(
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    resultKey: String = T::class.toString(),
    crossinline onResult: suspend (T) -> Unit
) {
    LaunchedEffect(resultKey, resultEventBus.channelMap[resultKey]) {
        resultEventBus.getResultFlow<T>()?.collect { result ->
            onResult.invoke(result as T)
        }
    }
}