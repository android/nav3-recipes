package com.example.nav3recipes.results.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Local for storing results in a [ResultStore]
 */
object LocalResultStore {
    private val LocalResultStore: ProvidableCompositionLocal<ResultStore?> =
        compositionLocalOf { null }

    /**
     * The current [ResultStore]
     */
    val current: ResultStore
        @Composable
        get() = LocalResultStore.current ?: error("No ResultStore has been provided")

    /**
     * Provides a [ResultStore] to the composition
     */
    infix fun provides(
        store: ResultStore
    ): ProvidedValue<ResultStore?> {
        return LocalResultStore.provides(store)
    }
}

/**
 * Provides a [ResultStore] that will be remembered across configuration changes.
 */
@Composable
fun rememberResultStore() : ResultStore {
    return rememberSaveable(saver = ResultStoreSaver()) {
        ResultStore()
    }
}

/**
 * A store for passing results between multiple sets of screens.
 *
 * It provides a solution for state based results.
 */
class ResultStore {

    /**
     * Map from the result key to a mutable state of the result.
     */
    val resultStateMap: MutableMap<String, MutableState<Any?>> = mutableMapOf()

    /**
     * Retrieves the current result of the given resultKey.
     */
    inline fun <reified T> getResultState(resultKey: String = T::class.toString()) =
        resultStateMap[resultKey]?.value as T

    /**
     * Sets the result for the given resultKey.
     */
    inline fun <reified T> setResult(resultKey: String = T::class.toString(), result: T) {
        resultStateMap.put(resultKey, mutableStateOf(result))
    }

    /**
     * Removes all results associated with the given key from the store.
     */
    inline fun <reified T> removeResult(resultKey: String = T::class.toString()) {
        resultStateMap.remove(resultKey)
    }
}

/** Saver to save and restore the NavController across config change and process death. */
private fun ResultStoreSaver(): Saver<ResultStore, *> =
    Saver(
        save = { it.resultStateMap },
        restore = { ResultStore().apply { resultStateMap.putAll(it)  } },
    )