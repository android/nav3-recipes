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

package com.example.nav3recipes.multiplestacksandauthentication

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {

    /**
     * Navigate to a route. Handles navigation within both Authentication and Connected graphs,
     * as well as transitions between them.
     */
    fun navigate(route: NavKey) {
        when (route) {
            is Root -> {
                // If transitioning from Authentication, update root backstack first
                if (state.currentRootDestination == AuthenticationRoute) {
                    state.rootBackStack.removeLastOrNull()
                    state.rootBackStack.add(ConnectedRoute)
                }
            }

            // Navigation within Authentication graph
            is Authentication -> {
                state.authenticationBackStack.add(route)
            }

            // Navigation within Connected graph (or transitioning to it from Authentication)
            is Connected -> {
                if (route in state.backStacks.keys) {
                    // This is a top level route, just switch to it
                    state.topLevelRoute = route
                } else {
                    state.backStacks[state.topLevelRoute]?.add(route)
                }
            }
        }
    }

    /**
     * Go back. Handles back navigation within both Authentication and Connected graphs.
     */
    fun goBack() {
        when (state.currentRootDestination) {
            AuthenticationRoute -> {
                state.authenticationBackStack.removeLastOrNull()
            }

            ConnectedRoute -> {
                val currentStack = state.backStacks[state.topLevelRoute]
                    ?: error("Stack for ${state.topLevelRoute} not found")
                val currentRoute = currentStack.last()

                // If we're at the base of the current route, go back to the start route stack.
                if (currentRoute == state.topLevelRoute) {
                    state.topLevelRoute = state.topLevelStartRoute
                } else {
                    currentStack.removeLastOrNull()
                }
            }
        }
    }
}
