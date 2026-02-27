/*
 * Copyright 2025 The Android Open Source Project
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import kotlin.collections.buildList

/**
 * Create a navigation state that persists config changes and process death.
 *
 * @param startRoute - The top level route to start on. This should also be in `topLevelRoutes`.
 * @param topLevelRoutes - The top level routes in the app.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    // Create a back stack for each top level route.
    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

/**
 * State holder for navigation state. This class does not modify its own state. It is designed
 * to be modified using the `Navigator` class.
 *
 * @param startRoute - the start route. The user will exit the app through this route.
 * @param topLevelRoute - the state object that backs the top level route.
 * @param backStacks - the back stacks for each top level route.
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {

    /**
     * The top level route.
     */
    var topLevelRoute: NavKey by topLevelRoute

    /**
     * Convert the navigation state into `NavEntry`s that have been decorated with a
     * `SaveableStateHolder`.
     *
     * @param entryProvider - the entry provider used to convert the keys in the
     * back stacks to `NavEntry`s.
     */
    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): List<NavEntry<NavKey>> {
        // Identify the top level routes that are currently in use.
        // The start route is always active to ensure the "exit through home" pattern.
        val activeRoutes by remember {
            derivedStateOf {
                listOf(startRoute, topLevelRoute).distinct()
            }
        }

        // Combine all back stacks into a single list in the order of [inactive..., active...].
        val allBackStack by remember {
            derivedStateOf {
                buildList {
                    // add inactive back stacks
                    backStacks.forEach { (route, backStack) ->
                        if (route !in activeRoutes) {
                            addAll(backStack)
                        }
                    }

                    // add active back stacks
                    activeRoutes.forEach { route ->
                        addAll(backStacks.getValue(route))
                    }
                }
            }
        }

        // Decorate all entries across all back stacks.
        // This ensures that all entries are known to the `SaveableStateHolder` decorator,
        // allowing state to be preserved even when a back stack is not currently active.
        val allEntries = rememberDecoratedNavEntries(
            backStack = allBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
            ),
            entryProvider = entryProvider
        )

        // Only return the entries for the active back stacks.
        return remember(allEntries, activeRoutes) {
            val activeEntriesSize = activeRoutes.sumOf { route -> backStacks.getValue(route).size }
            allEntries.takeLast(activeEntriesSize)
        }
    }
}
