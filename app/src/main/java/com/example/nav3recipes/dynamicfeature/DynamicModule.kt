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

/**
 * Base class for defining a dynamic feature module.
 *
 * Caches the [DynamicModuleEntryBuilder] instance to guarantee that the reflection instantiation
 * via [Class.forName] and [Class.newInstance] occurs exactly once per application process lifetime,
 * optimizing performance during recompositions when the `entryProvider` block is re-evaluated.
 */
abstract class DynamicModule(
    val entryBuilderClassName: String,
    val moduleName: String,
) {
    private var dynamicModuleEntryBuilder: DynamicModuleEntryBuilder? = null

    internal fun getDynamicModuleEntryBuilder(): DynamicModuleEntryBuilder {
        return dynamicModuleEntryBuilder ?: (Class.forName(entryBuilderClassName)
            .getConstructor()
            .newInstance() as DynamicModuleEntryBuilder).also { dynamicModuleEntryBuilder = it }
    }
}

/**
 * Interface that every dynamic feature module must implement to register its navigation entries.
 *
 * It is invoked by [buildDynamicEntries] to add the module's routes into the base [EntryProviderScope].
 *
 * **Example:**
 * ```kotlin
 * class ExampleEntryBuilder : DynamicModuleEntryBuilder {
 *     override fun EntryProviderScope<NavKey>.build() {
 *         appEntry<ExampleModule.Screen> {
 *             ExampleScreen()
 *         }
 *     }
 * }
 * ```
 */
fun interface DynamicModuleEntryBuilder {
    fun EntryProviderScope<NavKey>.build()
}

fun EntryProviderScope<NavKey>.buildDynamicEntries(
    module: DynamicModule,
) {
    with(module.getDynamicModuleEntryBuilder()) {
        build()
    }
}
