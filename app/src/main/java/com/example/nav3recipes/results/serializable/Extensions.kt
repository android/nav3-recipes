/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.results.serializable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
private data class NullableWrapper<T>(val value: T)

/**
 * Reusable extension function on [ResultEventBus] to provide a single [State] that preserves
 * its value across configuration changes and process death using [rememberSerializable].
 *
 * @param T The type of the result value.
 * @param resultKey The unique key associated with this result.
 * @param defaultValue The default initial value to be remembered and saved when no previously saved state exists.
 * @param inputs A set of inputs such that, when any of them have changed, the state will reset and re-initialize with [defaultValue].
 * @param stateSerializer A [KSerializer] used to serialize and deserialize the state value.
 * @param configuration Optional [SavedStateConfiguration] to customize how the serialization is
 *   handled, such as specifying a custom format (e.g., JSON).
 * @return A [State] containing the current value of the result, which updates dynamically when new results are received.
 */
@Composable
fun <T> ResultEventBus.conflateAsSerializableState(
    resultKey: String,
    defaultValue: T,
    vararg inputs: Any?,
    stateSerializer: KSerializer<T>,
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT,
): State<T> {
    val wrapperSerializer =
        remember(stateSerializer) { NullableWrapper.serializer(stateSerializer) }

    val wrapper = rememberSerializable(
        inputs = inputs,
        stateSerializer = wrapperSerializer,
        configuration = configuration,
    ) {
        mutableStateOf(NullableWrapper(defaultValue))
    }

    // ResultEffect's internal LaunchedEffect does not restart when the onResult lambda changes.
    // If inputs change, rememberSerializable recreates the savedState instance. Using
    // rememberUpdatedState ensures that the ongoing collection coroutine inside ResultEffect
    // always writes to the latest savedState instance without needing to cancel and restart.
    // https://issuetracker.google.com/531709234
    val currentWrapper = rememberUpdatedState(wrapper)
    ResultEffect<T>(resultKey = resultKey, resultEventBus = this) { result ->
        currentWrapper.value.value = NullableWrapper(result)
    }

    // Return a custom State wrapper rather than using derivedStateOf. Since unwrapping NullableWrapper
    // is O(1), an anonymous State avoids the snapshot read-tracking overhead and extra allocations
    // of derivedStateOf while maintaining a stable reference for the caller.
    return remember(wrapper) {
        object : State<T> {
            override val value: T
                get() = wrapper.value.value
        }
    }
}

/**
 * Reified version of [conflateAsSerializableState] using the class name as the key.
 *
 * @warning Do not use this overload for generic types (e.g., [List], [Map]) because
 * JVM type erasure will cause key collisions. Instead, use the version with an explicit `resultKey`.
 */
@Composable
inline fun <reified T> ResultEventBus.conflateAsSerializableState(
    defaultValue: T,
    vararg inputs: Any?,
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT,
): State<T> = conflateAsSerializableState(
    resultKey = T::class.toString(),
    defaultValue = defaultValue,
    inputs = inputs,
    stateSerializer = serializer<T>(),
    configuration = configuration,
)
