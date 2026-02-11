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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentOrange
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed

fun EntryProviderScope<NavKey>.rootSection(
    navigator: Navigator,
) {
    entry<AuthenticationRoute> {
        NavDisplay(
            backStack = navigator.state.authenticationBackStack,
            entryProvider = entryProvider {
                authenticationSection(
                    navigateToLogin = {
                        navigator.navigate(AuthenticationRoute.LoginRoute)
                    },
                    navigateToRegister = {
                        navigator.navigate(AuthenticationRoute.RegisterRoute)
                    },
                    onLoginClick = {
                        navigator.navigate(ConnectedRoute)
                    }
                )
            }
        )
    }

    entry<ConnectedRoute> {
        NavDisplay(
            entries = navigator.state.toDecoratedEntries(
                entryProvider = entryProvider {
                    featureASection(onSubRouteClick = { navigator.navigate(ConnectedRoute.RouteA1) })
                    featureBSection(onSubRouteClick = { navigator.navigate(ConnectedRoute.RouteB1) })
                    featureCSection(onSubRouteClick = { navigator.navigate(ConnectedRoute.RouteC1) })
                }
            ),
            onBack = { navigator.goBack() }
        )
    }
}

fun EntryProviderScope<NavKey>.authenticationSection(
    navigateToLogin: () -> Unit,
    navigateToRegister: () -> Unit,
    onLoginClick: () -> Unit
) {
    entry<AuthenticationRoute.WelcomeRoute> {
        ContentGreen("Welcome") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = navigateToLogin) {
                    Text("Go to Login")
                }
                Button(onClick = navigateToRegister) {
                    Text("Go to Register")
                }
            }
        }
    }
    entry<AuthenticationRoute.LoginRoute> {
        ContentPink("Login") {
            Button(onClick = onLoginClick) {
                Text("Login")
            }
        }
    }
    entry<AuthenticationRoute.RegisterRoute> {
        ContentPurple("Register") {
            Button(onClick = navigateToLogin) {
                Text("Go to Login after registration")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureASection(
    onSubRouteClick: () -> Unit,
) {
    entry<ConnectedRoute.RouteA> {
        ContentRed("Route A") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onSubRouteClick) {
                    Text("Go to A1")
                }
            }
        }
    }
    entry<ConnectedRoute.RouteA1> {
        ContentPink("Route A1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureBSection(
    onSubRouteClick: () -> Unit,
) {
    entry<ConnectedRoute.RouteB> {
        ContentGreen("Route B") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onSubRouteClick) {
                    Text("Go to B1")
                }
            }
        }
    }
    entry<ConnectedRoute.RouteB1> {
        ContentPurple("Route B1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }
            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureCSection(
    onSubRouteClick: () -> Unit,
) {
    entry<ConnectedRoute.RouteC> {
        ContentMauve("Route C") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onSubRouteClick) {
                    Text("Go to C1")
                }
            }
        }
    }
    entry<ConnectedRoute.RouteC1> {
        ContentOrange("Route C1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = { count++ }) {
                Text("Value: $count")
            }
        }
    }
}