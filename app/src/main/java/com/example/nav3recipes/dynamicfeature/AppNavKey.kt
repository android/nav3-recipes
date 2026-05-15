/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.dynamicfeature

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface AppNavKey : NavKey {
    // By default, `toString()` is used to generate the `contentKey` for NavEntries.
    // Instead of overriding `toString()`, we provide a dedicated `toContentKey()` function to make
    // it possible to uniquely identify keys for nested objects with the same simple name
    // (e.g., `ModuleA.Home` vs `ModuleB.Home`), without polluting the `toString()` representation.
    fun toContentKey(): Any = this.toString()
}

/**
 * An extension on [EntryProviderScope] specifically for [AppNavKey]s that overrides the default
 * `contentKey` resolution. It explicitly resolves the key via `it.toContentKey()` instead of the default
 * string representation.
 */
inline fun <reified K : AppNavKey> EntryProviderScope<NavKey>.appEntry(
    noinline clazzContentKey: (key: @JvmSuppressWildcards K) -> Any = { it.toContentKey() },
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @androidx.compose.runtime.Composable (K) -> Unit,
) {
    addEntryProvider(K::class, clazzContentKey, { metadata }, content)
}