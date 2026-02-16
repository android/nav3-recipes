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

        // For each back stack, create a `SaveableStateHolder` decorator and use it to decorate
        // the entries from that stack. When backStacks changes, `rememberDecoratedNavEntries` will
        // be recomposed and a new list of decorated entries is returned.
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider
            )
        }

        // Only return the entries for the stacks that are currently in use.
        return getTopLevelRoutesInUse()
            .flatMap { decoratedEntries[it] ?: emptyList() }
    }

    /**
     * Get the top level routes that are currently in use. The start route is always the first route
     * in the list. This means the user will always exit the app through the starting route
     * ("exit through home" pattern). The list will contain a maximum of one other route. This is a
     * design decision. In your app, you may wish to allow more than two top level routes to be
     * active.
     *
     * Note that even if a top level route is not in use its state is still retained.
     *
     * @return the current top level routes that are in use.
     */
    private fun getTopLevelRoutesInUse() : List<NavKey> =
		if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}
