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

package com.example.nav3recipes.multiplestacks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer

/**
 * Remember a [MutableState] holding a [NavKey] that persists config changes and process death.
 *
 * This is similar to `rememberNavBackStack`, but for a single key. It's useful when you need to
 * track a key alongside your back stacks, such as the current top level route when using
 * multiple back stacks:
 *
 * ```kotlin
 * var topLevelRoute by rememberNavKey(RouteA)
 * val backStacks = mapOf(
 *     RouteA to rememberNavBackStack(RouteA),
 *     RouteB to rememberNavBackStack(RouteB)
 * )
 * ```
 *
 * The key is saved using [NavKeySerializer], so it must be annotated with `@Serializable`.
 *
 * Note that the state is typed as [NavKey] rather than the type of the key you pass in. This means
 * you can set it to any other key, e.g. `topLevelRoute = RouteB`.
 *
 * @param initialKey The key to use when the state is first created.
 * @return A [MutableState] whose value is saved and restored automatically.
 */
@Composable
fun rememberNavKey(initialKey: NavKey): MutableState<NavKey> {
    return rememberSerializable(stateSerializer = NavKeySerializer()) {
        mutableStateOf(initialKey)
    }
}
