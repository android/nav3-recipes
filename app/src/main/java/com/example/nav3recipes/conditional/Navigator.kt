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

package com.example.nav3recipes.conditional

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack

/**
 * Manages application navigation with built-in support for conditional access.
 *
 * This class wraps a [NavBackStack] to intercept navigation events. If a user attempts to navigate
 * to a [Route] that requires login (via [Route.requiresLogin]) but is not currently authenticated,
 * this Navigator will:
 * 1. Save the intended destination.
 * 2. Redirect the user to the [loginRoute].
 *
 * @property backStack The underlying Navigation 3 back stack that holds the history of [Route]s.
 * @property loginRoute The specific [Route] representing the Login screen.
 * @property isLoggedInState A [MutableState] representing the source of truth for the user's
 * authentication status.
 */
class Navigator(
    private val backStack: NavBackStack<Route>,
    private val loginRoute: Route,
    isLoggedInState: MutableState<Boolean>,
) {

    private var isLoggedIn by isLoggedInState

    // The route that we will navigate to after successful login.
    private var onLoginSuccessRoute: Route? = null

    fun navigate(route: Route) {
        if (route.requiresLogin && !isLoggedIn) {
            // Store the intended destination and redirect to login
            onLoginSuccessRoute = route
            backStack.add(loginRoute)
        } else {
            backStack.add(route)
        }

        // If the user explicitly requested the login route, don't redirect them after login
        if (route == loginRoute) {
            onLoginSuccessRoute = null
        }
    }

    fun goBack() = backStack.removeLastOrNull()

    fun login() {
        isLoggedIn = true
        onLoginSuccessRoute?.let {
            backStack.add(it)
            backStack.remove(loginRoute)
        }
    }

    fun logout() {
        isLoggedIn = false
        backStack.removeAll { it.requiresLogin }
    }
}